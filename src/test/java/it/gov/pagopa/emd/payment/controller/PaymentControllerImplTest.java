package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.gov.pagopa.emd.payment.faker.TestUtils.*;
import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest(PaymentControllerImpl.class)
class PaymentControllerImplTest {


    @MockBean
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
        Mockito.when(paymentServiceImpl.getAllPaymentAttemptsByTppIdAndFiscalCode(anyString(),anyString())).thenReturn(Mono.just(List.of(PAYMENT_ATTEMPT_RESPONSE_DTO)));

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

}

