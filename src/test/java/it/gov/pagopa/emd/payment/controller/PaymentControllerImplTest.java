package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchResponseDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchRequestDTO;
import it.gov.pagopa.emd.payment.dto.PaymentSearchItemDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.verify;

import static it.gov.pagopa.emd.payment.faker.TestUtils.*;
import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest(PaymentControllerImpl.class)
class PaymentControllerImplTest {


    @MockitoBean
    private PaymentServiceImpl paymentServiceImpl;
    @Autowired
    private WebTestClient webTestClient;



    @Test
    void testRetrievalTokens() {
        Mockito.when(paymentServiceImpl.saveRetrieval("tppId", RETRIEVAL_REQUEST_DTO)).thenReturn(Mono.just(RETRIEVAL_RESPONSE_DTO));

        webTestClient.post()
                .uri("/emd/payment/retrievalTokens/{tppId}","tppId")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(RETRIEVAL_REQUEST_DTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RetrievalResponseDTO.class)
                .consumeWith(response -> {
                    RetrievalResponseDTO resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                });
    }

    @Test
    void testGetRetrieval() {
        Mockito.when(paymentServiceImpl.getRetrievalByRetrievalId("retrievalId")).thenReturn(Mono.just(RETRIEVAL_RESPONSE_DTO));

        webTestClient.get()
                .uri("/emd/payment/retrievalTokens/{retrievalId}","retrievalId")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RetrievalResponseDTO.class)
                .consumeWith(response -> {
                    RetrievalResponseDTO resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                });
    }

    @Test
    void testGenerateDeepLink() {
        Mockito.when(paymentServiceImpl.getRedirect("retrievalId","fiscalCode","noticeNumber", "amount")).thenReturn(Mono.just("string"));

        webTestClient.get()
                .uri("/emd/payment/token?retrievalId={retrievalId}&fiscalCode={fiscalCode}&noticeNumber={noticeNumber}&amount={amount}","retrievalId","fiscalCode","noticeNumber","amount")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().exists("Location");
    }


  @Test
  void testGenerateDeepLinkWithoutAmount() {
    Mockito.when(paymentServiceImpl.getRedirect("retrievalId","fiscalCode","noticeNumber",null)).thenReturn(Mono.just("string"));

    webTestClient.get()
        .uri("/emd/payment/token?retrievalId={retrievalId}&fiscalCode={fiscalCode}&noticeNumber={noticeNumber}","retrievalId","fiscalCode","noticeNumber")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isFound()
        .expectHeader().exists("Location");
  }

    @Test
    void testGetAllPaymentAttemptsByTppId() {
        Mockito.when(paymentServiceImpl.getAllPaymentAttemptsByTppId(anyString())).thenReturn(Mono.just(List.of(PAYMENT_ATTEMPT_RESPONSE_DTO)));

        webTestClient.get()
                .uri("/emd/payment/paymentAttempts/{tppId}","tppId")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaymentAttemptResponseDTO.class)
                .consumeWith(response -> {
                    List<PaymentAttemptResponseDTO> resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                });
    }


    @Test
    void testGetAllAttemptDetailsByTppIdAndFiscalCode() {
        Mockito.when(paymentServiceImpl.getPaymentAttemptByTppIdAndOriginId(anyString(),anyString())).thenReturn(Mono.just((PAYMENT_ATTEMPT_RESPONSE_DTO)));

        webTestClient.get()
                .uri("/emd/payment/paymentAttempts/{tppId}/fiscalCode","tppId","fisclaCode")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaymentAttemptResponseDTO.class)
                .consumeWith(response -> {
                    List<PaymentAttemptResponseDTO> resultResponse = response.getResponseBody();
                    Assertions.assertNotNull(resultResponse);
                });
    }

    @Test
    void testPaymentSearchWithoutFiltersReturnsBadRequest() {
        webTestClient.get()
                .uri("/emd/payment/attempts/search")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message -> Assertions.assertTrue(
                        message.toString().contains("Specificare almeno un criterio")));
    }

    @Test
    void testPaymentSearchReturnsCursorPage() {
        Mockito.when(paymentServiceImpl.searchPayments(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Mono.just(PaymentSearchResponseDTO.builder()
                        .payments(List.of(PaymentSearchItemDTO.builder()
                                .iun("IUN-1")
                                .tppId("tpp-1")
                                .attemptsCount(2)
                                .build()))
                        .returnedCount(1)
                        .attemptsCount(2)
                        .hasMore(true)
                        .nextCursor("507f1f77bcf86cd799439011")
                        .build()));

        webTestClient.get()
                .uri("/emd/payment/attempts/search?tppId=tpp-1&limit=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.hasMore").isEqualTo(true)
                .jsonPath("$.nextCursor").isEqualTo("507f1f77bcf86cd799439011")
                .jsonPath("$.payments[0].tppId").isEqualTo("tpp-1")
                .jsonPath("$.payments[0].pspId").doesNotExist();
    }

    @Test
    void testPaymentSearchRejectsInvalidCursorAndLimits() {
        webTestClient.get()
                .uri("/emd/payment/attempts/search?iun=IUN-1&cursor=not-an-object-id")
                .exchange()
                .expectStatus().isBadRequest();

        webTestClient.get()
                .uri("/emd/payment/attempts/search?tppId=tpp-1&limit=0")
                .exchange()
                .expectStatus().isBadRequest();

        webTestClient.get()
                .uri("/emd/payment/attempts/search?tppId=tpp-1&limit=101")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testPaymentSearchBindsCursorAndFiltersToRequest() {
        String cursor = "507f1f77bcf86cd799439011";
        Mockito.when(paymentServiceImpl.searchPayments(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Mono.just(PaymentSearchResponseDTO.builder()
                        .payments(List.of())
                        .returnedCount(0)
                        .attemptsCount(0)
                        .hasMore(false)
                        .build()));

        webTestClient.get()
                .uri("/emd/payment/attempts/search?iun=IUN-1&tppId=tpp-1&cursor={cursor}&limit=7", cursor)
                .exchange()
                .expectStatus().isOk();

        org.mockito.ArgumentCaptor<PaymentSearchRequestDTO> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(PaymentSearchRequestDTO.class);
        verify(paymentServiceImpl).searchPayments(requestCaptor.capture());
        Assertions.assertEquals("IUN-1", requestCaptor.getValue().getIun());
        Assertions.assertEquals("tpp-1", requestCaptor.getValue().getTppId());
        Assertions.assertEquals(cursor, requestCaptor.getValue().getCursor());
        Assertions.assertEquals(7, requestCaptor.getValue().getLimit());
    }

}

