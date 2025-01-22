package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.constant.PaymentConstants;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.dto.TppDTO;
import it.gov.pagopa.emd.payment.model.Retrieval;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final RetrievalRepository repository;
    private final TppConnectorImpl tppControllerImpl;
    private final ExceptionMap exceptionMap;
    private static final String DEEP_LINK = "<deepLink>?fiscalCode=<payee fiscal code>&noticeNumber=<notice number>";

    public PaymentServiceImpl(RetrievalRepository repository,
                              TppConnectorImpl tppControllerImpl,
                              ExceptionMap exceptionMap){
        this.repository = repository;
        this.tppControllerImpl = tppControllerImpl;
        this.exceptionMap = exceptionMap;
    }

    @Override
    public Mono<RetrievalResponseDTO> saveRetrieval(String tppId, RetrievalRequestDTO retrievalRequestDTO) {
        return tppControllerImpl.getTpp(tppId)
                .flatMap(tppDTO ->
                        repository.save(createRetrievalByTppAndRequest(tppDTO, retrievalRequestDTO))
                                .map(this::createResponseByRetrieval))
                .onErrorMap(error -> exceptionMap.throwException(PaymentConstants.ExceptionName.TPP_NOT_FOUND, PaymentConstants.ExceptionMessage.TPP_NOT_FOUND));
    }

    @Override
    public Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId) {
        return repository.findByRetrievalId(retrievalId)
                .switchIfEmpty(Mono.error(exceptionMap.throwException(PaymentConstants.ExceptionName.RETRIEVAL_NOT_FOUND,
                        PaymentConstants.ExceptionMessage.RETRIEVAL_NOT_FOUND)))
                .map(this::createResponseByModel);
    }

    @Override
    public Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber) {
        return getRetrievalByRetrievalId(retrievalId)
                .map(retrievalResponseDTO -> buildDeepLink(retrievalResponseDTO.getDeeplink(), fiscalCode, noticeNumber));
    }


    private Retrieval createRetrievalByTppAndRequest(TppDTO tppDTO, RetrievalRequestDTO retrievalRequestDTO){
        Retrieval retrieval = new Retrieval();
        HashMap<String, String> agentDeepLinks = tppDTO.getAgentDeepLinks();
        retrieval.setRetrievalId(String.format("%s-%d", UUID.randomUUID(), System.currentTimeMillis()));
        retrieval.setDeeplink(agentDeepLinks.get(retrievalRequestDTO.getAgent()));
        retrieval.setPaymentButton(tppDTO.getPaymentButton());
        retrieval.setOriginId(retrievalRequestDTO.getOriginId());
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
        return retrievalResponseDTO;
    }

    private String buildDeepLink(String deepLink, String fiscalCode, String noticeNumber){
        return DEEP_LINK
                .replace("<deepLink>",deepLink)
                .replace("<payee fiscal code>",fiscalCode)
                .replace("<notice number>",noticeNumber);
    }

}
