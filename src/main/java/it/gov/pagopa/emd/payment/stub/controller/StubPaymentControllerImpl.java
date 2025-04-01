package it.gov.pagopa.emd.payment.stub.controller;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.stub.model.PaymentInfo;
import it.gov.pagopa.emd.payment.stub.service.PaymentService;
import it.gov.pagopa.emd.payment.stub.service.StubPaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(origins = "*")
public class StubPaymentControllerImpl implements StubPaymentController {

    private final StubPaymentServiceImpl stubPaymentCoreService;
    private  final PaymentService service;

    public StubPaymentControllerImpl(StubPaymentServiceImpl stubPaymentCoreService, PaymentService service) {
        this.stubPaymentCoreService = stubPaymentCoreService;
        this.service = service;
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

    @Override
    public Mono<ResponseEntity<String>> createPayment(String retrievalId, String fiscalCode, String noticeNumber) {
        return service.sendSoapRequest(fiscalCode, noticeNumber)
                .map(xml -> {
                    try {
                        PaymentInfo info = service.parseSoapResponse(xml);
                        String html = service.generateHtmlResponse(info);
                        return ResponseEntity.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .body(html);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body("<h1>Errore durante il parsing della risposta SOAP</h1>");
                    }
                });
    }

}
