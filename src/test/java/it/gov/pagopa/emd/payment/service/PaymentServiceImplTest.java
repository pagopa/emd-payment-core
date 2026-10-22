package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnector;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchRequestDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchItemDTO;
import it.gov.pagopa.emd.payment.dto.TppBatchRequestDTO;
import it.gov.pagopa.emd.payment.dto.TppBatchResponseDTO;
import it.gov.pagopa.emd.payment.dto.TppBatchResultDTO;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.Retrieval;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
    private TppConnector tppConnectorImpl;

    @BeforeEach
    void setUp() {
        when(tppConnectorImpl.getTppsByIds(any()))
                .thenReturn(Mono.just(new TppBatchResponseDTO(List.of(), List.of())));
    }

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
        PaymentAttempt first = paymentAttempt(firstId, "IUN-1", "tpp-1", 2);
        PaymentAttempt second = paymentAttempt(secondId, "IUN-1", "tpp-2", 1);
        when(paymentAttemptRepository.searchPayments(eq("IUN-1"), isNull(), isNull(), eq(1)))
                .thenReturn(Flux.just(first, second));
        when(paymentAttemptRepository.searchPayments(eq("IUN-1"), isNull(), eq(firstId), eq(1)))
                .thenReturn(Flux.just(second));
        when(paymentAttemptRepository.countSearchPayments("IUN-1", null)).thenReturn(Mono.just(2L));

        PaymentSearchRequestDTO firstRequest = new PaymentSearchRequestDTO();
        firstRequest.setIun("IUN-1");
        firstRequest.setLimit(1);
        StepVerifier.create(paymentServiceImpl.searchPayments(firstRequest))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertTrue(response.isHasNext());
                    org.junit.jupiter.api.Assertions.assertEquals(firstId.toHexString(), response.getNextCursor());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.getTotalPages());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getPageSize());
                    org.junit.jupiter.api.Assertions.assertEquals("tpp-1", response.getContent().getFirst().getTppId());
                })
                .verifyComplete();

        PaymentSearchRequestDTO secondRequest = new PaymentSearchRequestDTO();
        secondRequest.setIun("IUN-1");
        secondRequest.setLimit(1);
        secondRequest.setCursor(firstId.toHexString());
        StepVerifier.create(paymentServiceImpl.searchPayments(secondRequest))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertFalse(response.isHasNext());
                    org.junit.jupiter.api.Assertions.assertNull(response.getNextCursor());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals("IUN-1", response.getContent().getFirst().getIun());
                })
                .verifyComplete();
    }

    @Test
    void testPaymentSearchReturnsEmptyPage() {
        when(paymentAttemptRepository.searchPayments(eq(null), eq("tpp-1"), isNull(), eq(20)))
                .thenReturn(Flux.empty());
        when(paymentAttemptRepository.countSearchPayments(null, "tpp-1")).thenReturn(Mono.just(0L));
        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setTppId("tpp-1");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertTrue(response.getContent().isEmpty());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getTotalPages());
                    org.junit.jupiter.api.Assertions.assertEquals(20, response.getPageSize());
                    org.junit.jupiter.api.Assertions.assertFalse(response.isHasNext());
                    org.junit.jupiter.api.Assertions.assertNull(response.getNextCursor());
                })
                .verifyComplete();

        verify(tppConnectorImpl, never()).getTppsByIds(any());
    }

    @Test
    void testPaymentSearchNormalizesFiltersAndHandlesExactLimitWithoutNextPage() {
        PaymentAttempt first = paymentAttempt(new ObjectId("507f1f77bcf86cd799439021"), "IUN-2", "tpp-2", 1);
        PaymentAttempt second = PaymentAttempt.builder()
                .id(new ObjectId("507f1f77bcf86cd799439022"))
                .originId("IUN-3")
                .tppId("tpp-3")
                .attemptDetails(null)
                .build();
        when(paymentAttemptRepository.searchPayments("IUN-2", "tpp-2", null, 2))
                .thenReturn(Flux.just(first, second));
        when(paymentAttemptRepository.countSearchPayments("IUN-2", "tpp-2")).thenReturn(Mono.just(2L));

        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setIun(" IUN-2 ");
        request.setTppId(" tpp-2 ");
        request.setLimit(2);

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertFalse(response.isHasNext());
                    org.junit.jupiter.api.Assertions.assertNull(response.getNextCursor());
                    org.junit.jupiter.api.Assertions.assertEquals(2, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getTotalPages());
                    org.junit.jupiter.api.Assertions.assertEquals("tpp-2", response.getContent().get(0).getTppId());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.getContent().get(1).getAttemptsCount());
                })
                .verifyComplete();

        verify(paymentAttemptRepository).searchPayments("IUN-2", "tpp-2", null, 2);
        verify(retrievalRepository, never())
                .findFirstByTppIdAndOriginIdOrderByCreatedAtDesc(anyString(), anyString());
    }

    @Test
    void testPaymentSearchMapsFieldsDirectlyFromPaymentAttempt() {
        PaymentAttempt payment = paymentAttempt(
                new ObjectId("507f1f77bcf86cd799439031"), "IUN-4", "tpp-4", 3);
        when(paymentAttemptRepository.searchPayments("IUN-4", null, null, 20))
                .thenReturn(Flux.just(payment));
        when(paymentAttemptRepository.countSearchPayments("IUN-4", null)).thenReturn(Mono.just(1L));

        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setIun("IUN-4");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .assertNext(response -> {
                    PaymentSearchItemDTO item = response.getContent().getFirst();
                    org.junit.jupiter.api.Assertions.assertEquals("tpp-4", item.getTppId());
                    org.junit.jupiter.api.Assertions.assertEquals("IUN-4", item.getIun());
                    org.junit.jupiter.api.Assertions.assertEquals(3, item.getAttemptsCount());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getTotalElements());
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.getTotalPages());
                })
                .verifyComplete();

        verify(retrievalRepository, never())
                .findFirstByTppIdAndOriginIdOrderByCreatedAtDesc(anyString(), anyString());
        verify(tppConnectorImpl, never()).getTppByEntityId(anyString());
    }

    @Test
    void testPaymentSearchEnrichesPageWithSingleDeduplicatedTppBatchLookup() {
        PaymentAttempt first = paymentAttempt(new ObjectId("507f1f77bcf86cd799439051"), "IUN-6", "tpp-6", 1);
        PaymentAttempt second = paymentAttempt(new ObjectId("507f1f77bcf86cd799439052"), "IUN-6", "tpp-6", 2);
        PaymentAttempt third = paymentAttempt(new ObjectId("507f1f77bcf86cd799439053"), "IUN-7", "tpp-7", 1);
        when(paymentAttemptRepository.searchPayments(null, null, null, 20))
                .thenReturn(Flux.just(first, second, third));
        when(paymentAttemptRepository.countSearchPayments(null, null)).thenReturn(Mono.just(3L));
        when(tppConnectorImpl.getTppsByIds(any())).thenReturn(Mono.just(new TppBatchResponseDTO(
                List.of(new TppBatchResultDTO("tpp-6", "Business 6", "ENTITY-6")),
                List.of("tpp-7"))));

        StepVerifier.create(paymentServiceImpl.searchPayments(new PaymentSearchRequestDTO()))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(3, response.getContent().size());
                    org.junit.jupiter.api.Assertions.assertEquals("Business 6", response.getContent().get(0).getBusinessName());
                    org.junit.jupiter.api.Assertions.assertEquals("ENTITY-6", response.getContent().get(1).getEntityId());
                    org.junit.jupiter.api.Assertions.assertNull(response.getContent().get(2).getBusinessName());
                    org.junit.jupiter.api.Assertions.assertEquals(3, response.getTotalElements());
                })
                .verifyComplete();

        org.mockito.ArgumentCaptor<TppBatchRequestDTO> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(TppBatchRequestDTO.class);
        verify(tppConnectorImpl).getTppsByIds(requestCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("tpp-6", "tpp-7"), requestCaptor.getValue().getIds());
    }

    @Test
    void testPaymentSearchDoesNotReturnPartialPageWhenTppBatchLookupFails() {
        PaymentAttempt payment = paymentAttempt(new ObjectId("507f1f77bcf86cd799439061"), "IUN-8", "tpp-8", 1);
        when(paymentAttemptRepository.searchPayments("IUN-8", null, null, 20)).thenReturn(Flux.just(payment));
        when(paymentAttemptRepository.countSearchPayments("IUN-8", null)).thenReturn(Mono.just(1L));
        when(tppConnectorImpl.getTppsByIds(any())).thenReturn(Mono.error(new IllegalStateException("TPP unavailable")));
        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setIun("IUN-8");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .expectErrorMatches(error -> error instanceof it.gov.pagopa.emd.payment.exception.ClientExceptionWithBody)
                .verify();
    }

    @Test
    void testPaymentSearchPerformsTppLookupForReturnedPage() {
        PaymentAttempt payment = paymentAttempt(new ObjectId("507f1f77bcf86cd799439041"), "IUN-5", "tpp-5", 1);
        when(paymentAttemptRepository.searchPayments("IUN-5", null, null, 20))
                .thenReturn(Flux.just(payment));
        when(paymentAttemptRepository.countSearchPayments("IUN-5", null)).thenReturn(Mono.just(1L));

        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setIun("IUN-5");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("tpp-5", response.getContent().getFirst().getTppId());
                })
                .verifyComplete();

        verify(retrievalRepository, never())
                .findFirstByTppIdAndOriginIdOrderByCreatedAtDesc(anyString(), anyString());
        verify(tppConnectorImpl).getTppsByIds(any());
    }

    @Test
    void testPaymentSearchMapsRepositoryFailureToGenericError() {
        when(paymentAttemptRepository.searchPayments("IUN-error", null, null, 20))
                .thenReturn(Flux.error(new IllegalStateException("database unavailable")));
        when(paymentAttemptRepository.countSearchPayments("IUN-error", null)).thenReturn(Mono.just(0L));
        PaymentSearchRequestDTO request = new PaymentSearchRequestDTO();
        request.setIun("IUN-error");

        StepVerifier.create(paymentServiceImpl.searchPayments(request))
                .expectErrorMatches(error -> error instanceof it.gov.pagopa.emd.payment.exception.ClientExceptionWithBody)
                .verify();
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