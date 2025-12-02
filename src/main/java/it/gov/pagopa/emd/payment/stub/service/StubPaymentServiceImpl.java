package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.constant.PaymentConstants;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.dto.TppDTO;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import static it.gov.pagopa.emd.payment.utils.Utils.inputSanify;

/**
 * Implementation of the {@link StubPaymentService} interface.
 */
@Slf4j
@Service
public class StubPaymentServiceImpl implements StubPaymentService {

    private final RetrievalRepository retrievalRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TppConnectorImpl tppControllerImpl;
    private final ExceptionMap exceptionMap;
    private static final int TTL = 1;
    public StubPaymentServiceImpl(RetrievalRepository retrievalRepository,
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
     */
    @Override
    public Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber, String amount) {
        log.info("[EMD][PAYMENT][GET-REDIRECT] Get redirect for retrievalId: {}, fiscalCode: {}, noticeNumber: {} and amount:{}",inputSanify(retrievalId), Utils.createSHA256(fiscalCode),noticeNumber, amount);

        return getRetrievalByRetrievalId(retrievalId)
                .flatMap(retrievalResponseDTO ->
                        paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(retrievalResponseDTO.getTppId(), retrievalResponseDTO.getOriginId(), fiscalCode)
                                .flatMap(paymentAttempt ->
                                        paymentAttemptRepository.save(addNewAttemptDetails(paymentAttempt,noticeNumber))
                                )
                                .switchIfEmpty(Mono.defer(() ->
                                        paymentAttemptRepository.save(createNewPaymentAttempt(retrievalResponseDTO, fiscalCode, noticeNumber, amount))
                                ))
                                .map(paymentAttempt ->
                                        buildDeepLink(retrievalResponseDTO.getDeeplink(), fiscalCode, noticeNumber, amount)
                                )
                );
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
     * @param fiscalCode the taxpayer's fiscal code
     * @param noticeNumber the notice number for the first attempt
     * @param amount amount of the payment
     * @return new PaymentAttempt entity with initial attempt details
     */
    private PaymentAttempt createNewPaymentAttempt(RetrievalResponseDTO retrievalResponseDTO, String fiscalCode, String noticeNumber, String amount){
        PaymentAttempt paymentAttempt = new PaymentAttempt();
        paymentAttempt.setFiscalCode(fiscalCode);
        paymentAttempt.setTppId(retrievalResponseDTO.getTppId());
        paymentAttempt.setOriginId(retrievalResponseDTO.getOriginId());
        paymentAttempt.setAmount(amount);
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
        retrievalResponseDTO.setPspDenomination(retrieval.getPspDenomination());
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
     * @param amount amount of the payment
     * @return complete deep link URL with query parameters
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
