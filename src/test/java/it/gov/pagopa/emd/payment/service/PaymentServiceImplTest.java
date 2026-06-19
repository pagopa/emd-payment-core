package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.emd.payment.faker.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {PaymentServiceImpl.class, ExceptionMap.class})
@ExtendWith(SpringExtension.class)
class PaymentServiceImplTest {
    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @MockitoBean
    private RetrievalRepository retrievalRepository;

    @MockitoBean
    private PaymentAttemptRepository paymentAttemptRepository;

    @MockitoBean
    private TppConnectorImpl tppConnectorImpl;

    /**
     * Method under test: {@link PaymentServiceImpl#saveRetrieval(String, RetrievalRequestDTO)}
     */
    @Test
    void testSaveRetrieval() {
        when(tppConnectorImpl.getTppByEntityId("tppId")).thenReturn(Mono.just(TPP_DTO));
        when(retrievalRepository.save(any())).thenReturn(Mono.just(RETRIEVAL));
        RetrievalResponseDTO retrievalResponseDTO = new RetrievalResponseDTO();
        retrievalResponseDTO.setRetrievalId("retrievalId");

        StepVerifier.create(paymentServiceImpl.saveRetrieval("tppId", RETRIEVAL_REQUEST_DTO))
                .expectNext(retrievalResponseDTO)
                .verifyComplete();
    }

    @Test
    void testGetRetrievalByRetrievalId() {
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));

        StepVerifier.create(paymentServiceImpl.getRetrievalByRetrievalId("tppId"))
                .expectNext(RETRIEVAL_RESPONSE_DTO)
                .verifyComplete();
    }

    @Test
    void testGetRedirect() {
        // Stub del caricamento iniziale
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.upsertAttemptDetails(any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentServiceImpl.getRedirect("retrievalId", "fiscalCode", "noticeNumber", "amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }

    @Test
    void testGetRedirectEmpty() {
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.upsertAttemptDetails(any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentServiceImpl.getRedirect("retrievalId", "fiscalCode", "noticeNumber", "amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }

    @Test
    void testGetAllPaymentAttemptsByTppId() {
        when(paymentAttemptRepository.findByTppId(anyString())).thenReturn(Flux.just(PAYMENT_ATTEMPT));

        StepVerifier.create(paymentServiceImpl.getAllPaymentAttemptsByTppId("tppId"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetPaymentAttemptByTppIdAndOriginId() {
        when(paymentAttemptRepository.findByTppIdAndOriginId(anyString(), anyString())).thenReturn(Mono.just(PAYMENT_ATTEMPT));

        StepVerifier.create(paymentServiceImpl.getPaymentAttemptByTppIdAndOriginId("tppId", "originId"))
                .expectNextCount(1)
                .verifyComplete();
    }
}