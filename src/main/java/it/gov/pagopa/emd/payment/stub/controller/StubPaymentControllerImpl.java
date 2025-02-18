package it.gov.pagopa.emd.payment.stub.controller;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.stub.service.StubPaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(origins = "*")
public class StubPaymentControllerImpl implements StubPaymentController {

    private final StubPaymentServiceImpl stubPaymentCoreService;

    public StubPaymentControllerImpl(StubPaymentServiceImpl stubPaymentCoreService) {
        this.stubPaymentCoreService = stubPaymentCoreService;
    }

    @Override
    public Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(String entityId, RetrievalRequestDTO retrievalRequestDTO) {
        return stubPaymentCoreService.saveRetrieval(entityId, retrievalRequestDTO)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(String retrievalId) {
        return stubPaymentCoreService.getRetrievalByRetrievalId(retrievalId)
                .map(ResponseEntity::ok);
    }


    @Override
    public Mono<ResponseEntity<Void>> generateDeepLink(String retrievalId, String fiscalCode, String noticeNumber){
        return stubPaymentCoreService.getRedirect(retrievalId,fiscalCode,noticeNumber)
                .map(deepLink ->ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", deepLink)
                        .build()
                );
    }

}
