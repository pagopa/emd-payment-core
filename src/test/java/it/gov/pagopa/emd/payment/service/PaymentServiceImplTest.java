package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchRequestDTO;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.Retrieval;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import org.junit.jupiter.api.Test;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static it.gov.pagopa.emd.payment.faker.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    void testPaymentSearchUsesNextCursorForFollowingPage() {
        ObjectId firstId = new ObjectId("507f1f77bcf86cd799439011");
        ObjectId secondId = new ObjectId("507f1f77bcf86cd799439012");
        PaymentAttempt first = paymentAttempt(firstId, "IUN-1", "psp-1", 2);
        PaymentAttempt second = paymentAttempt(secondId, "IUN-1", "psp-2", 1);
        when(paymentAttemptRepository.searchPayments(eq("IUN-1"), isNull(), isNull(), eq(1)))
                .thenReturn(Flux.just(first, second));
        when(paymentAttemptRepository.searchPayments(eq("IUN-1"), isNull(), eq(firstId), eq(1)))
                .thenReturn(Flux.just(second));
        Retrieval retrieval = new Retrieval();
        retrieval.setEntityId("entity-1");
        retrieval.setPspDenomination("PSP Example");
        when(retrievalRepository.findFirstByTppIdAndOriginIdOrderByCreatedAtDesc("psp-1", "IUN-1"))
                .thenReturn(Mono.just(retrieval));
        when(retrievalRepository.findFirstByTppIdAndOriginIdOrderByCreatedAtDesc("psp-2", "IUN-1"))
                .thenReturn(Mono.just(retrieval));
        when(tppConnectorImpl.getTppByEntityId("entity-1")).thenReturn(Mono.just(TPP_DTO));

        PaymentSearchRequestDTO firstRequest = new PaymentSearchRequestDTO();
        firstRequest.setIun("IUN-1");
        firstRequest.setLimit(1);
        StepVerifier.create(paymentServiceImpl.searchPayments(firstRequest))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertTrue(response.isHasMore());
                    org.junit.jupiter.api.Assertions.assertEquals(firstId.toHexString(), response.getNextCursor());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getReturnedCount());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.getAttemptsCount());
                    org.junit.jupiter.api.Assertions.assertEquals(TPP_DTO.getPspDenomination(), response.getPayments().getFirst().getPspName());
                })
                .verifyComplete();

        PaymentSearchRequestDTO secondRequest = new PaymentSearchRequestDTO();
        secondRequest.setIun("IUN-1");
        secondRequest.setLimit(1);
        secondRequest.setCursor(firstId.toHexString());
        StepVerifier.create(paymentServiceImpl.searchPayments(secondRequest))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertFalse(response.isHasMore());
                    org.junit.jupiter.api.Assertions.assertNull(response.getNextCursor());
                    org.junit.jupiter.api.Assertions.assertEquals("IUN-1", response.getPayments().getFirst().getIun());
                })
                .verifyComplete();
    }

    @Test
    void testPaymentSearchReturnsEmptyPage() {
        when(paymentAttemptRepository.searchPayments(eq(null), eq("psp-1"), isNull(), eq(20)))
                .thenReturn(Flux.empty());
        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setPsp("psp-1");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertTrue(response.getPayments().isEmpty());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getReturnedCount());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getAttemptsCount());
                    org.junit.jupiter.api.Assertions.assertFalse(response.isHasMore());
                    org.junit.jupiter.api.Assertions.assertNull(response.getNextCursor());
                })
                .verifyComplete();
    }

    private PaymentAttempt paymentAttempt(ObjectId id, String iun, String tppId, int attemptsCount) {
        List<AttemptDetails> details = java.util.stream.IntStream.range(0, attemptsCount)
                .mapToObj(index -> new AttemptDetails())
                .toList();
        return PaymentAttempt.builder()
                .id(id)
                .originId(iun)
                .tppId(tppId)
                .attemptDetails(details)
                .build();
    }
}