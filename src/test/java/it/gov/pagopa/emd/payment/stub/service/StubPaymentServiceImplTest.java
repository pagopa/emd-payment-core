package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
import it.gov.pagopa.emd.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.emd.payment.faker.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {StubPaymentServiceImpl.class, ExceptionMap.class})
@ExtendWith(SpringExtension.class)
class StubPaymentServiceImplTest {
    @Autowired
    private StubPaymentServiceImpl stubPaymentService;

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

        StepVerifier.create(stubPaymentService.saveRetrieval("tppId",RETRIEVAL_REQUEST_DTO)).expectNext(retrievalResponseDTO)
                .verifyComplete();
    }

    @Test
    void testGetRetrievalByRetrievalId(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));

        StepVerifier.create(stubPaymentService.getRetrievalByRetrievalId("tppId")).expectNext(RETRIEVAL_RESPONSE_DTO)
                .verifyComplete();
    }

    @Test
    void testGetRedirect(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(RETRIEVAL.getTppId(),RETRIEVAL.getOriginId(),"fiscalCode")).thenReturn(Mono.just(PAYMENT_ATTEMPT));
        when(paymentAttemptRepository.save(any())).thenReturn(Mono.just(PAYMENT_ATTEMPT));

        StepVerifier.create(stubPaymentService.getRedirect("retrievalId","fiscalCode","noticeNumber","amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }

    @Test
    void testGetRedirectEmpty(){
        when(retrievalRepository.findByRetrievalId(any())).thenReturn(Mono.just(RETRIEVAL));
        when(paymentAttemptRepository.findByTppIdAndOriginIdAndFiscalCode(RETRIEVAL.getTppId(),RETRIEVAL.getOriginId(),"fiscalCode")).thenReturn(Mono.empty());
        when(paymentAttemptRepository.save(any())).thenReturn(Mono.just(new PaymentAttempt()));

        StepVerifier.create(stubPaymentService.getRedirect("retrievalId","fiscalCode","noticeNumber","amount"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber&amount=amount")
                .verifyComplete();
    }
}

