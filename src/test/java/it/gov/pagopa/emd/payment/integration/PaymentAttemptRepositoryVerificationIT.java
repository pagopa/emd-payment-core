package it.gov.pagopa.emd.payment.integration;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.TestPropertySource;

import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.repository.PaymentAttemptRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


/**
 * Integration test verifying MongoDB aggregation queries.
 *
 * <p>Uses MongoDB driver debug logging to inspect generated pipelines.
 * Check console output for actual query structure.</p>
 */
@TestPropertySource(properties = {
    "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG",
})
public class PaymentAttemptRepositoryVerificationIT extends BaseIT {
    private static final Logger log = LoggerFactory.getLogger(RetrievalRepositoryQueryVerificationIT.class);

    private static final String TEST_CF = "fiscalCode1";
    private static final String TEST_TTP_ID = "tppId1";
    private static final String TEST_ORIGIN_ID = "origin-id-1";
    private static final String COLLECTION_NAME = "payment_attempt";

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @Autowired 
    PaymentAttemptRepository repository;


    @BeforeEach
    void setup() {
        // Drop collection
        StepVerifier.create(
            mongoTemplate.dropCollection(COLLECTION_NAME)
                .onErrorResume(e -> Mono.empty())
        ).verifyComplete();

        List<AttemptDetails> attemptDetailsList1 = List.of(
            AttemptDetails.builder()
                .noticeNumber("noticeNumber1")
                .build(),
            AttemptDetails.builder()
                .noticeNumber("noticeNumber2")
                .build()
        );
        PaymentAttempt testPayment1 = PaymentAttempt.builder()
            .fiscalCode(TEST_CF)
            .tppId(TEST_TTP_ID)
            .originId(TEST_ORIGIN_ID)
            .attemptDetails(attemptDetailsList1)
            .build();
        
        StepVerifier.create(
            mongoTemplate.save(testPayment1, COLLECTION_NAME)
        ).expectNextCount(1).verifyComplete();

    }

    @Test
    void testFindByTppIdAndOriginIdAndFiscalCode(){
        log.info("=== EXECUTING testFindByTppIdAndOriginIdAndFiscalCode ===");
        
        StepVerifier.create(
                repository.findByTppIdAndOriginIdAndFiscalCode(TEST_TTP_ID, TEST_ORIGIN_ID, TEST_CF)
            ).assertNext(paymentAttempt -> {
                log.info("Found payment attempt: {}", paymentAttempt);
                assert paymentAttempt.getFiscalCode().equals(TEST_CF);
            })
            .verifyComplete();
        
        log.info("=== TEST COMPLETED - CHECK LOGS ABOVE FOR QUERY DETAILS ===");
    }

    @Test
    void testFindByTppIdAndOriginIdAndFiscalCodeNotFound(){
        log.info("=== EXECUTING testFindByTppIdAndOriginIdAndFiscalCodeNotFound (not found) ===");
        
        StepVerifier.create(
                repository.findByTppIdAndOriginIdAndFiscalCode("Wrong_cf", TEST_TTP_ID, TEST_ORIGIN_ID)
            )
            .verifyComplete();
        
            log.info("=== TEST COMPLETED ===");
    }

    @Test
    void testFindByTppId(){
        log.info("=== EXECUTING testFindByTppId ===");
        
        StepVerifier.create(
                repository.findByTppId(TEST_TTP_ID)
            ).assertNext(paymentAttempt -> {
                log.info("Found payment attempt: {}", paymentAttempt);
                assert paymentAttempt.getFiscalCode().equals(TEST_CF);
            })
            .verifyComplete();

        log.info("=== TEST COMPLETED - CHECK LOGS ABOVE FOR QUERY DETAILS ===");
    }   

    @Test
    void testFindByTppIdNotFound(){
        log.info("=== EXECUTING testFindByTppIdNotFound (not found) ===");
        
        StepVerifier.create(
                repository.findByTppId("wrong_ttp_id")
            )
            .verifyComplete();
        
        log.info("=== TEST COMPLETED ===");
    }

    @Test
    void testFindByTppIdAndFiscalCode(){
        log.info("=== EXECUTING testFindByTppIdAndFiscalCode ===");
        
        StepVerifier.create(
                repository.findByTppIdAndFiscalCode(TEST_TTP_ID, TEST_CF)
            ).assertNext(paymentAttempt -> {
                log.info("Found payment attempt: {}", paymentAttempt);
                assert paymentAttempt.getFiscalCode().equals(TEST_CF);
            })
            .verifyComplete();
        
        log.info("=== TEST COMPLETED - CHECK LOGS ABOVE FOR QUERY DETAILS ===");
    }

    @Test
    void testFindByTppIdAndFiscalCodeNotFound(){
        log.info("=== EXECUTING testFindByTppIdAndFiscalCodeNotFound (not found) ===");

        StepVerifier.create(
                repository.findByTppIdAndFiscalCode(TEST_TTP_ID, "Wrong_cf")
            )
            .verifyComplete();

        log.info("=== TEST COMPLETED ===");
    }

}