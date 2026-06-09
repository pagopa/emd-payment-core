package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.constant.PaymentConstants;
import it.gov.pagopa.emd.payment.dto.*;
import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.model.Retrieval;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import it.gov.pagopa.emd.payment.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;

import static it.gov.pagopa.emd.payment.utils.Utils.inputSanify;

/**
 * Implementation of the {@link PaymentService} interface providing payment operations and retrieval management.
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final RetrievalRepository retrievalRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TppConnectorImpl tppControllerImpl;
    private final ExceptionMap exceptionMap;
    private static final int TTL = 1;

    public PaymentServiceImpl(RetrievalRepository retrievalRepository,
                              PaymentAttemptRepository paymentAttemptRepository,
                              TppConnectorImpl tppControllerImpl,
                              ExceptionMap exceptionMap){
        this.retrievalRepository = retrievalRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.tppControllerImpl = tppControllerImpl;
        this.exceptionMap = exceptionMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<RetrievalResponseDTO> saveRetrieval(String entityId, RetrievalRequestDTO retrievalRequestDTO) {
        log.info("[EMD][PAYMENT][SAVE-RETRIEVAL] Save retrieval for entityId:{} and agent: {}, and linkVersion: {}", inputSanify(entityId), retrievalRequestDTO.getAgent(), retrievalRequestDTO.getLinkVersion());
        return tppControllerImpl.getTppByEntityId(entityId)
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.TPP_NOT_FOUND, PaymentConstants.ExceptionMessage.TPP_NOT_FOUND)))
                .flatMap(tppDTO ->
                        retrievalRepository.save(createRetrievalByTppAndRequest(tppDTO, retrievalRequestDTO))
                                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                                .map(this::createResponseByRetrieval))
                .doOnSuccess(retrievalResponseDTO -> log.info("[EMD][PAYMENT][SAVE-RETRIEVAL] Saved retrieval: {} for entityId:{} and agent: {}", inputSanify(retrievalResponseDTO.getRetrievalId()), inputSanify(entityId), retrievalRequestDTO.getAgent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId) {
        log.info("[EMD][PAYMENT][GET-RETRIEVAL] Get retrieval by retrievalId: {}", inputSanify(retrievalId));
        return retrievalRepository.findByRetrievalId(retrievalId)
                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.RETRIEVAL_NOT_FOUND, PaymentConstants.ExceptionMessage.RETRIEVAL_NOT_FOUND)))
                .map(this::createResponseByModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber, String amount) {
        log.info("[EMD][PAYMENT][GET-REDIRECT] Get redirect for retrievalId: {}, fiscalCode: {}, noticeNumber: {} and amount:{}", inputSanify(retrievalId), Utils.createSHA256(fiscalCode), noticeNumber, amount);
        return getRetrievalByRetrievalId(retrievalId)
                .flatMap(retrievalResponseDTO ->
                        paymentAttemptRepository.upsertAttemptDetails(
                                        retrievalResponseDTO.getTppId(),
                                        retrievalResponseDTO.getOriginId(),
                                        createNewAttemptDetails(noticeNumber, fiscalCode, amount)
                                )
                                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                                .then(Mono.fromCallable(() ->
                                        buildDeepLink(retrievalResponseDTO.getDeeplink(), fiscalCode, noticeNumber, amount)
                                ))
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppId(String tppId){
        log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Get payments by tppId: {}", inputSanify(tppId));
        return paymentAttemptRepository.findByTppId(tppId)
                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                .collectList()
                .map(this::convertPaymentAttemptModelToDTO)
                .doOnSuccess(paymentAttemptResponseDTOS -> log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Got {} payments by tppId: {}", paymentAttemptResponseDTOS.size(), inputSanify(tppId)))
                .doOnError(error -> log.error("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Error to get Payment Attempts by tppId: {}", inputSanify(tppId), error));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<PaymentAttemptResponseDTO> getPaymentAttemptByTppIdAndOriginId(String tppId, String originId){
        log.info("[EMD][PAYMENT][GET-PAYMENT-ATTEMPT-BY-TPP-ID-AND-ORIGIN-ID] Get payment attempt by tppId: {} and originId: {}", inputSanify(tppId), inputSanify(originId));
        return paymentAttemptRepository.findByTppIdAndOriginId(tppId, originId)
                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.PAYMENT_ATTEMPT_NOT_FOUND, PaymentConstants.ExceptionMessage.PAYMENT_ATTEMPT_NOT_FOUND)))
                .map(this::convertSinglePaymentAttemptModelToDTO)
                .doOnSuccess(paymentAttemptResponseDTO -> log.info("[EMD][PAYMENT][GET-PAYMENT-ATTEMPT-BY-TPP-ID-AND-ORIGIN-ID] Got payment attempt for tppId: {}", inputSanify(tppId)))
                .doOnError(error -> log.error("[EMD][PAYMENT][GET-PAYMENT-ATTEMPT-BY-TPP-ID-AND-ORIGIN-ID] Error to get Payment Attempt by tppId: {}", inputSanify(tppId), error));
    }

    /**
     * Converts a list of PaymentAttempt entities to PaymentAttemptResponseDTO objects.
     */
    private List<PaymentAttemptResponseDTO> convertPaymentAttemptModelToDTO(List<PaymentAttempt> paymentAttemptList){
        List<PaymentAttemptResponseDTO> paymentAttemptResponseDTOList = new ArrayList<>();
        for(PaymentAttempt paymentAttempt : paymentAttemptList){
            paymentAttemptResponseDTOList.add(convertSinglePaymentAttemptModelToDTO(paymentAttempt));
        }
        return paymentAttemptResponseDTOList;
    }

    /**
     * Converts a single PaymentAttempt entity to a PaymentAttemptResponseDTO object.
     */
    private PaymentAttemptResponseDTO convertSinglePaymentAttemptModelToDTO(PaymentAttempt paymentAttempt) {
        PaymentAttemptResponseDTO dto = new PaymentAttemptResponseDTO();
        dto.setTppId(paymentAttempt.getTppId());
        dto.setOriginId(paymentAttempt.getOriginId());
        dto.setAttemptDetails(convertAttemptDetailsModelToDTO(paymentAttempt.getAttemptDetails()));
        return dto;
    }

    /**
     * Converts a list of AttemptDetails entities to AttemptDetailsResponseDTO objects.
     */
    private List<AttemptDetailsResponseDTO> convertAttemptDetailsModelToDTO(List<AttemptDetails> attemptDetails){
        List<AttemptDetailsResponseDTO> attemptDetailsResponseDTOList = new ArrayList<>();
        if (attemptDetails == null) {
            return attemptDetailsResponseDTOList;
        }
        for(AttemptDetails attemptDetail : attemptDetails){
            AttemptDetailsResponseDTO attemptDetailsResponseDTO = new AttemptDetailsResponseDTO();
            attemptDetailsResponseDTO.setPaymentAttemptDate(attemptDetail.getPaymentAttemptDate());
            attemptDetailsResponseDTO.setNoticeNumber(attemptDetail.getNoticeNumber());
            attemptDetailsResponseDTO.setFiscalCode(attemptDetail.getFiscalCode());
            attemptDetailsResponseDTO.setAmount(attemptDetail.getAmount());
            attemptDetailsResponseDTOList.add(attemptDetailsResponseDTO);
        }
        return attemptDetailsResponseDTOList;
    }

    /**
     * Instantiates a new AttemptDetails object with current configuration settings.
     */
    private AttemptDetails createNewAttemptDetails(String noticeNumber, String fiscalCode, String amount){
        AttemptDetails attemptDetails = new AttemptDetails();
        attemptDetails.setPaymentAttemptDate(Calendar.getInstance().getTime());
        attemptDetails.setNoticeNumber(noticeNumber);
        attemptDetails.setFiscalCode(fiscalCode);
        attemptDetails.setAmount(amount);
        return attemptDetails;
    }

    /**
     * Creates a Retrieval entity from TPP information and retrieval request.
     */
    private Retrieval createRetrievalByTppAndRequest(TppDTO tppDTO, RetrievalRequestDTO retrievalRequestDTO){
        Retrieval retrieval = new Retrieval();
        HashMap<String, AgentLink> agentLinkMap = tppDTO.getAgentLinks();
        retrieval.setRetrievalId(String.format("%s-%d", UUID.randomUUID(), System.currentTimeMillis()));
        retrieval.setTppId(tppDTO.getTppId());

        if(ObjectUtils.isEmpty(agentLinkMap)){
            throw exceptionMap.throwException(PaymentConstants.ExceptionName.AGENT_DEEP_LINKS_EMPTY, PaymentConstants.ExceptionMessage.AGENT_DEEP_LINKS_EMPTY);
        }
        if(!agentLinkMap.containsKey(retrievalRequestDTO.getAgent())){
            throw exceptionMap.throwException(PaymentConstants.ExceptionName.AGENT_NOT_FOUND_IN_DEEP_LINKS, PaymentConstants.ExceptionMessage.AGENT_NOT_FOUND_IN_DEEP_LINKS);
        }

        String deepLink;
        AgentLink agentLink = agentLinkMap.get(retrievalRequestDTO.getAgent());
        HashMap<String, VersionDetails> versionMap = agentLink.getVersions();
        if(versionMap != null && versionMap.containsKey(retrievalRequestDTO.getLinkVersion())){
            deepLink = versionMap.get(retrievalRequestDTO.getLinkVersion()).getLink();
        }
        else{
            deepLink = agentLink.getFallBackLink();
        }
        retrieval.setDeeplink(deepLink);

        retrieval.setPspDenomination(tppDTO.getPspDenomination());
        retrieval.setOriginId(retrievalRequestDTO.getOriginId());
        retrieval.setIsPaymentEnabled(tppDTO.getIsPaymentEnabled());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, TTL);
        retrieval.setCreatedAt(calendar.getTime());
        return retrieval;
    }

    /**
     * Creates a minimal RetrievalResponseDTO from a Retrieval entity for save operations.
     */
    private RetrievalResponseDTO createResponseByRetrieval(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        return retrievalResponseDTO;
    }

    /**
     * Creates a complete RetrievalResponseDTO from a Retrieval entity.
     */
    private RetrievalResponseDTO createResponseByModel(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        retrievalResponseDTO.setDeeplink(retrieval.getDeeplink());
        retrievalResponseDTO.setPspDenomination(retrieval.getPspDenomination());
        retrievalResponseDTO.setOriginId(retrieval.getOriginId());
        retrievalResponseDTO.setTppId(retrieval.getTppId());
        retrievalResponseDTO.setIsPaymentEnabled(retrieval.getIsPaymentEnabled());
        return retrievalResponseDTO;
    }

    /**
     * Builds a complete deep link URL with fiscal code and notice number parameters.
     */
    private String buildDeepLink(String deepLink, String fiscalCode, String noticeNumber, String amount){
        return UriComponentsBuilder
                .fromUriString(deepLink)
                .queryParam("fiscalCode", fiscalCode)
                .queryParam("noticeNumber", noticeNumber)
                .queryParam("amount", amount)
                .build(true)
                .toUriString();
    }
}