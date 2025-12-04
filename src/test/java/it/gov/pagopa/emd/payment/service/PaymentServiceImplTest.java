package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private RetrievalRepository retrievalRepository;

    @MockBean
    private PaymentAttemptRepository paymentAttemptRepository;

    @MockBean
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

        StepVerifier.create(paymentServiceImpl.saveRetrieval("tppId","1.0.0",RETRIEVAL_REQUEST_DTO)).expectNext(retrievalResponseDTO)
                .verifyComplete();
    }

    @Test
    void testGetRetrievalByRetrievalId(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));

        StepVerifier.create(paymentServiceImpl.getRetrievalByRetrievalId("tppId")).expectNext(RETRIEVAL_RESPONSE_DTO)
                .verifyComplete();
    }

    @Test
    void testGetRedirect(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(RETRIEVAL.getTppId(),RETRIEVAL.getOriginId(),"fiscalCode")).thenReturn(Mono.just(PAYMENT_ATTEMPT));
        when(paymentAttemptRepository.save(any())).thenReturn(Mono.just(PAYMENT_ATTEMPT));

        StepVerifier.create(paymentServiceImpl.getRedirect("retrievalId","fiscalCode","noticeNumber","amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }

    @Test
    void testGetRedirectEmpty(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(RETRIEVAL.getTppId(),RETRIEVAL.getOriginId(),"fiscalCode")).thenReturn(Mono.empty());
        when(paymentAttemptRepository.save(any())).thenReturn(Mono.just(new PaymentAttempt()));

        StepVerifier.create(paymentServiceImpl.getRedirect("retrievalId","fiscalCode","noticeNumber","amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }

    @Test
    void testGetAllPaymentAttemptsByTppId(){
        when(paymentAttemptRepository.findByTppId(anyString())).thenReturn(Flux.just(PAYMENT_ATTEMPT));

        StepVerifier.create(paymentServiceImpl.getAllPaymentAttemptsByTppId("tppId")).expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetAllPaymentAttemptsByTppIdAndFiscalCode(){
        when(paymentAttemptRepository.findByTppIdAndFiscalCode(anyString(),anyString())).thenReturn(Flux.just(PAYMENT_ATTEMPT));

        StepVerifier.create(paymentServiceImpl.getAllPaymentAttemptsByTppIdAndFiscalCode("tppId","fiscalCode")).expectNextCount(1)
                .verifyComplete();
    }

}

