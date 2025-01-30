package it.gov.pagopa.emd.payment.service;

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
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import static it.gov.pagopa.emd.payment.utils.Utils.inputSanify;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final RetrievalRepository retrievalRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TppConnectorImpl tppControllerImpl;
    private final ExceptionMap exceptionMap;
    private static final String DEEP_LINK = "<deepLink>?fiscalCode=<payee fiscal code>&noticeNumber=<notice number>";
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

    @Override
    public Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId) {
        log.info("[EMD][PAYMENT][GET-RETRIEVAL] Get retrieval by retrievalId: {}",inputSanify(retrievalId));
        return retrievalRepository.findByRetrievalId(retrievalId)
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.RETRIEVAL_NOT_FOUND,
                        PaymentConstants.ExceptionMessage.RETRIEVAL_NOT_FOUND)))
                .map(this::createResponseByModel);
    }

    @Override
    public Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber) {
        log.info("[EMD][PAYMENT][GET-REDIRECT] Get redirect for retrievalId: {}, fiscalCode: {} and noticeNumber: {}",inputSanify(retrievalId), Utils.createSHA256(fiscalCode),noticeNumber);
        return getRetrievalByRetrievalId(retrievalId)
                .flatMap(retrievalResponseDTO -> paymentAttemptRepository.findByFiscalCodeAndTppIdAndOriginId(fiscalCode,retrievalResponseDTO.getTppId(), retrievalResponseDTO.getOriginId())
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


    private PaymentAttempt addNewAttemptDetails(PaymentAttempt paymentAttempt, String noticeNumber){
        AttemptDetails attemptDetails = new AttemptDetails();
        attemptDetails.setPaymentAttemptDate(Calendar.getInstance().getTime());
        attemptDetails.setNoticeNumber(noticeNumber);
        paymentAttempt.getAttemptDetails().add(attemptDetails);
        return paymentAttempt;
    }

    private PaymentAttempt createNewPaymentAttempt(RetrievalResponseDTO retrievalResponseDTO, String fiscalCode, String noticeNumber){
        PaymentAttempt paymentAttempt = new PaymentAttempt();
        paymentAttempt.setFiscalCode(fiscalCode);
        paymentAttempt.setTppId(retrievalResponseDTO.getTppId());
        paymentAttempt.setOriginId(retrievalResponseDTO.getOriginId());
        paymentAttempt.setAttemptDetails(new ArrayList<>());
        addNewAttemptDetails(paymentAttempt, noticeNumber);
        return paymentAttempt;
    }

    private Retrieval createRetrievalByTppAndRequest(TppDTO tppDTO, RetrievalRequestDTO retrievalRequestDTO){
        Retrieval retrieval = new Retrieval();
        HashMap<String, String> agentDeepLinks = tppDTO.getAgentDeepLinks();
        retrieval.setRetrievalId(String.format("%s-%d", UUID.randomUUID(), System.currentTimeMillis()));
        retrieval.setTppId(tppDTO.getTppId());
        if(ObjectUtils.isEmpty(agentDeepLinks)){
            throw exceptionMap.throwException(PaymentConstants.ExceptionName.AGENT_DEEP_LINKS_EMPTY, PaymentConstants.ExceptionMessage.AGENT_DEEP_LINKS_EMPTY);
        }
        retrieval.setDeeplink(agentDeepLinks.get(retrievalRequestDTO.getAgent()));
        retrieval.setPaymentButton(tppDTO.getPaymentButton());
        retrieval.setOriginId(retrievalRequestDTO.getOriginId());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, TTL);
        retrieval.setCreatedAt(calendar.getTime());
        return retrieval;
    }

    private RetrievalResponseDTO createResponseByRetrieval(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        return retrievalResponseDTO;
    }

    private RetrievalResponseDTO createResponseByModel(Retrieval retrieval){
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId(retrieval.getRetrievalId());
        retrievalResponseDTO.setDeeplink(retrieval.getDeeplink());
        retrievalResponseDTO.setPaymentButton(retrieval.getPaymentButton());
        retrievalResponseDTO.setOriginId(retrieval.getOriginId());
        retrievalResponseDTO.setTppId(retrieval.getTppId());
        return retrievalResponseDTO;
    }

    private String buildDeepLink(String deepLink, String fiscalCode, String noticeNumber){
        return DEEP_LINK
                .replace("<deepLink>",deepLink)
                .replace("<payee fiscal code>",fiscalCode)
                .replace("<notice number>",noticeNumber);
    }

}
