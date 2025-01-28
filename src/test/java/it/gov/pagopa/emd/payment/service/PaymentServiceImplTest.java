package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.configuration.ExceptionMap;
import it.gov.pagopa.emd.payment.connector.TppConnectorImpl;
import it.gov.pagopa.emd.payment.dto.NetworkResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
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

@ContextConfiguration(classes = {PaymentServiceImpl.class, ExceptionMap.class})
@ExtendWith(SpringExtension.class)
class PaymentServiceImplTest {
    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @MockBean
    private RetrievalRepository retrievalRepository;

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

        StepVerifier.create(paymentServiceImpl.saveRetrieval("tppId",RETRIEVAL_REQUEST_DTO)).expectNext(retrievalResponseDTO)
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

        StepVerifier.create(paymentServiceImpl.getRedirect("retrievalId","fiscalCode","noticeNumber"))
                .expectNext("deepLink?fiscalCode=fiscalCode&noticeNumber=noticeNumber")
                .verifyComplete();
    }

    @Test
    void testConnection(){
        NetworkResponseDTO networkResponseDTO = new NetworkResponseDTO();
        networkResponseDTO.setMessage("tppName ha raggiunto i nostri sistemi");
        networkResponseDTO.setCode("PAGOPA_NETWORK_TEST");
        StepVerifier.create(paymentServiceImpl.testConnection("tppName"))
                .expectNext(networkResponseDTO)
                .verifyComplete();
    }

}

