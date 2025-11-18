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
        log.info("[EMD][PAYMENT][SAVE-RETRIEVAL] Save retrieval for entityId:{} and agent: {}",inputSanify(entityId),retrievalRequestDTO.getAgent());
        return tppControllerImpl.getTppByEntityId(entityId)
                .switchIfEmpty(Mono.error(exceptionMap.throwException
                        (PaymentConstants.ExceptionName.TPP_NOT_FOUND, PaymentConstants.ExceptionMessage.TPP_NOT_FOUND)))
                .flatMap(tppDTO ->
                        //Crea un retrival con le info tipo payment button ecc e lo salva
                        retrievalRepository.save(createRetrievalByTppAndRequest(tppDTO, retrievalRequestDTO))
                                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.GENERIC_ERROR, PaymentConstants.ExceptionMessage.GENERIC_ERROR))
                                .map(this::createResponseByRetrieval))
                .doOnSuccess(retrievalResponseDTO -> log.info("[EMD][PAYMENT][SAVE-RETRIEVAL] Saved retrieval: {} for entityId:{} and agent: {}",inputSanify(retrievalResponseDTO.getRetrievalId()),inputSanify(entityId),retrievalRequestDTO.getAgent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId) {
        log.info("[EMD][PAYMENT][GET-RETRIEVAL] Get retrieval by retrievalId: {}",inputSanify(retrievalId));
        return retrievalRepository.findByRetrievalId(retrievalId)
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.RETRIEVAL_NOT_FOUND,
                        PaymentConstants.ExceptionMessage.RETRIEVAL_NOT_FOUND)))
                .map(this::createResponseByModel);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Get the retrival from the db by retrievalId and the payment attempt by tppId, originId and fiscalCode.
     * If the payment attempt is present update the attempt details with the new notice number and save, otherwise create a new payment attempt
     * with the notice number and save it. Finally build the deep link with fiscal code and notice number and return it.
     */
    @Override
    public Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber) {
        log.info("[EMD][PAYMENT][GET-REDIRECT] Get redirect for retrievalId: {}, fiscalCode: {} and noticeNumber: {}",inputSanify(retrievalId), Utils.createSHA256(fiscalCode),noticeNumber);
        return getRetrievalByRetrievalId(retrievalId)
                .flatMap(retrievalResponseDTO ->
                        paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(retrievalResponseDTO.getTppId(), retrievalResponseDTO.getOriginId(), fiscalCode)
                                .flatMap(paymentAttempt ->
                                        paymentAttemptRepository.save(addNewAttemptDetails(paymentAttempt,noticeNumber))
                                )
                                .switchIfEmpty(Mono.defer(() ->
                                        paymentAttemptRepository.save(createNewPaymentAttempt(retrievalResponseDTO, fiscalCode, noticeNumber))
                                ))
                                .map(paymentAttempt ->
                                        buildDeepLink(retrievalResponseDTO.getDeeplink(), fiscalCode, noticeNumber)
                                )
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppId(String tppId){
        log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Get payments by tppId: {}",inputSanify(tppId));
        return paymentAttemptRepository.findByTppId(tppId)
                .collectList()
                .map(this::convertPaymentAttemptModelToDTO)
                .doOnSuccess(paymentAttemptResponseDTOS -> log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Got {} payments by tppId: {}",paymentAttemptResponseDTOS.size(),inputSanify(tppId)))
                .doOnError(error -> log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID] Error {} to get Payment Attempts by tppId: {}",error.getMessage(),inputSanify(tppId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppIdAndFiscalCode(String tppId, String fiscalCode){
        log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID-AND-FISCAL-CODE] Get payments by tppId: {} and fiscalCode: {}",inputSanify(tppId),Utils.createSHA256(fiscalCode));
        return paymentAttemptRepository.findByTppIdAndFiscalCode(tppId,fiscalCode)
                .collectList()
                .map(this::convertPaymentAttemptModelToDTO)
                .doOnSuccess(paymentAttemptResponseDTOS -> log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID-AND-FISCAL-CODE] Got {} payments by tppId: {} and fiscalCode: {}",paymentAttemptResponseDTOS.size(),inputSanify(tppId),Utils.createSHA256(fiscalCode)))
                .doOnError(error -> log.info("[EMD][PAYMENT][GET-ALL-PAYMENT-ATTEMPTS-BY-TPP-ID-AND-FISCAL-CODE] Error {} to get Payment Attempts by tppId: {} and fiscalCode: {}",error.getMessage(),inputSanify(tppId),Utils.createSHA256(fiscalCode)));
    }

    /**
     * Converts a list of PaymentAttempt entities to PaymentAttemptResponseDTO objects.
     * 
     * @param paymentAttemptList list of PaymentAttempt entities to convert
     * @return list of converted PaymentAttemptResponseDTO objects
     */
    private List<PaymentAttemptResponseDTO> convertPaymentAttemptModelToDTO(List<PaymentAttempt> paymentAttemptList){
        List<PaymentAttemptResponseDTO> paymentAttemptResponseDTOList = new ArrayList<>();
        for(PaymentAttempt paymentAttempt : paymentAttemptList){
            PaymentAttemptResponseDTO paymentAttemptResponseDTO = new PaymentAttemptResponseDTO();
            paymentAttemptResponseDTO.setTppId(paymentAttempt.getTppId());
            paymentAttemptResponseDTO.setOriginId(paymentAttempt.getOriginId());
            paymentAttemptResponseDTO.setFiscalCode(paymentAttempt.getFiscalCode());
            paymentAttemptResponseDTO.setAttemptDetails(convertAttemptDetailsModelToDTO(paymentAttempt.getAttemptDetails()));
            paymentAttemptResponseDTOList.add(paymentAttemptResponseDTO);
        }
        return paymentAttemptResponseDTOList;
    }

    /**
     * Converts a list of AttemptDetails entities to AttemptDetailsResponseDTO objects.
     * 
     * @param attemptDetails list of AttemptDetails entities to convert
     * @return list of converted AttemptDetailsResponseDTO objects
     */
    private List<AttemptDetailsResponseDTO> convertAttemptDetailsModelToDTO(List<AttemptDetails> attemptDetails){
        List<AttemptDetailsResponseDTO> attemptDetailsResponseDTOList = new ArrayList<>();
        for(AttemptDetails attemptDetail : attemptDetails){
            AttemptDetailsResponseDTO attemptDetailsResponseDTO = new AttemptDetailsResponseDTO();
            attemptDetailsResponseDTO.setPaymentAttemptDate(attemptDetail.getPaymentAttemptDate());
            attemptDetailsResponseDTO.setNoticeNumber(attemptDetail.getNoticeNumber());
            attemptDetailsResponseDTOList.add(attemptDetailsResponseDTO);
        }
        return attemptDetailsResponseDTOList;
    }




    /**
     * Adds new attempt details to an existing payment attempt.
     *
     * @param paymentAttempt the existing payment attempt to update
     * @param noticeNumber the notice number for the new attempt
     * @return the updated PaymentAttempt with new attempt details added
     */
    private PaymentAttempt addNewAttemptDetails(PaymentAttempt paymentAttempt, String noticeNumber){
        AttemptDetails attemptDetails = new AttemptDetails();
        attemptDetails.setPaymentAttemptDate(Calendar.getInstance().getTime());
        attemptDetails.setNoticeNumber(noticeNumber);
        paymentAttempt.getAttemptDetails().add(attemptDetails);
        return paymentAttempt;
    }

    /**
     * Creates a new payment attempt with initial attempt details.
     * 
     * @param retrievalResponseDTO the retrieval response containing TPP and origin information
     * @param fiscalCode the fiscal code
     * @param noticeNumber the notice number for the first attempt
     * @return new PaymentAttempt entity with initial attempt details
     */
    private PaymentAttempt createNewPaymentAttempt(RetrievalResponseDTO retrievalResponseDTO, String fiscalCode, String noticeNumber){
        PaymentAttempt paymentAttempt = new PaymentAttempt();
        paymentAttempt.setFiscalCode(fiscalCode);
        paymentAttempt.setTppId(retrievalResponseDTO.getTppId());
        paymentAttempt.setOriginId(retrievalResponseDTO.getOriginId());
        paymentAttempt.setAttemptDetails(new ArrayList<>());
        addNewAttemptDetails(paymentAttempt, noticeNumber);
        return paymentAttempt;
    }

    /**
     * Creates a Retrieval entity from TPP information and retrieval request.
     * 
     * @param tppDTO the TPP information including deep links and payment button configuration
     * @param retrievalRequestDTO the retrieval request containing agent and origin information
     * @return new Retrieval entity with configured properties and unique ID
     */
    private Retrieval createRetrievalByTppAndRequest(TppDTO tppDTO, RetrievalRequestDTO retrievalRequestDTO){
        Retrieval retrieval = new Retrieval();
        HashMap<String, String> agentDeepLinks = tppDTO.getAgentDeepLinks();
        retrieval.setRetrievalId(String.format("%s-%d", UUID.randomUUID(), System.currentTimeMillis()));
        retrieval.setTppId(tppDTO.getTppId());
        if(ObjectUtils.isEmpty(agentDeepLinks)){
            throw exceptionMap.throwException(PaymentConstants.ExceptionName.AGENT_DEEP_LINKS_EMPTY, PaymentConstants.ExceptionMessage.AGENT_DEEP_LINKS_EMPTY);
        }
        if(!agentDeepLinks.containsKey(retrievalRequestDTO.getAgent())){
            throw exceptionMap.throwException(PaymentConstants.ExceptionName.AGENT_NOT_FOUND_IN_DEEP_LINKS, PaymentConstants.ExceptionMessage.AGENT_NOT_FOUND_IN_DEEP_LINKS);
        }
        retrieval.setDeeplink(agentDeepLinks.get(retrievalRequestDTO.getAgent()));
        retrieval.setPaymentButton(tppDTO.getPaymentButton());
        retrieval.setOriginId(retrievalRequestDTO.getOriginId());
        retrieval.setIsPaymentEnabled(tppDTO.getIsPaymentEnabled());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, TTL);
        retrieval.setCreatedAt(calendar.getTime());
        return retrieval;
    }

    /**
     * Creates a minimal RetrievalResponseDTO from a Retrieval entity for save operations.
     *
     * @param retrieval the Retrieval entity to convert
     * @return RetrievalResponseDTO containing only the retrieval ID
     */
    private RetrievalResponseDTO createResponseByRetrieval(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        return retrievalResponseDTO;
    }

    /**
     * Creates a complete RetrievalResponseDTO from a Retrieval entity.
     * 
     * @param retrieval the Retrieval entity to convert
     * @return RetrievalResponseDTO containing all retrieval information
     */
    private RetrievalResponseDTO createResponseByModel(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        retrievalResponseDTO.setDeeplink(retrieval.getDeeplink());
        retrievalResponseDTO.setPaymentButton(retrieval.getPaymentButton());
        retrievalResponseDTO.setOriginId(retrieval.getOriginId());
        retrievalResponseDTO.setTppId(retrieval.getTppId());
        retrievalResponseDTO.setIsPaymentEnabled(retrieval.getIsPaymentEnabled());
        return retrievalResponseDTO;
    }

    /**
     * Builds a complete deep link URL with fiscal code and notice number parameters.
     *
     * @param deepLink the base deep link URL
     * @param fiscalCode the fiscal code to append as query parameter
     * @param noticeNumber the notice number to append as query parameter
     * @return complete deep link URL with query parameters
     */
    private String buildDeepLink(String deepLink, String fiscalCode, String noticeNumber){
        return UriComponentsBuilder
                .fromUriString(deepLink)
                .queryParam("fiscalCode", fiscalCode)
                .queryParam("noticeNumber", noticeNumber)
                .build(true)
                .toUriString();

    }

}
