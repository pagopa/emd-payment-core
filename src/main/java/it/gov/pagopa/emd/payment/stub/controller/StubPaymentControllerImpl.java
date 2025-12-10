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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * REST controller implementation for stub payment operations and retrieval management.
 */
@RestController
@CrossOrigin(origins = "*")
public class StubPaymentControllerImpl implements StubPaymentController {

    private final StubPaymentServiceImpl stubPaymentCoreService;
    private  final PaymentService service;

    public StubPaymentControllerImpl(StubPaymentServiceImpl stubPaymentCoreService, PaymentService service) {
        this.stubPaymentCoreService = stubPaymentCoreService;
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(String entityId, RetrievalRequestDTO retrievalRequestDTO) {
        return stubPaymentCoreService.saveRetrieval(entityId, retrievalRequestDTO)
                .map(ResponseEntity::ok);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(String retrievalId) {
        return stubPaymentCoreService.getRetrievalByRetrievalId(retrievalId)
                .map(ResponseEntity::ok);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ResponseEntity<Void>> generateDeepLink(String retrievalId, String fiscalCode, String noticeNumber, String amount){
        return stubPaymentCoreService.getRedirect(retrievalId,fiscalCode,noticeNumber, amount)
                .map(deepLink ->ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", deepLink)
                        .build()
                );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sends a SOAP request to simulate payment processing, parses
     * the response to extract payment information, and generates an HTML response using
     * a template. Returns HTML content with
     * appropriate content type or an error page if processing fails.
     * </p>
     */
    @Override
    public Mono<ResponseEntity<String>> createPayment(String fiscalCode, String noticeNumber) {
        return service.sendSoapRequest(fiscalCode, noticeNumber)
                .map(xml -> {
                    try {
                        PaymentInfo info = service.parseSoapResponse(xml);
                        String executionDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        String htmlTemplate = service.readTemplateFromResources("payment-template.html");

                        String html = htmlTemplate
                                .replace("${amount}", info.getAmount())
                                .replace("${noticeNumber}", noticeNumber)
                                .replace("${fiscalCode}", fiscalCode)
                                .replace("${executionDate}", executionDate);

                        return ResponseEntity.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .body(html);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body("<h1>Errore durante il parsing della risposta SOAP</h1>");
                    }
                });
    }

}
