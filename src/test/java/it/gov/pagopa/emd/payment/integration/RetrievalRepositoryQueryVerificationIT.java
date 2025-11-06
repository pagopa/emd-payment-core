package it.gov.pagopa.emd.payment.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.TestPropertySource;

import it.gov.pagopa.emd.payment.model.Retrieval;
import it.gov.pagopa.emd.payment.repository.RetrievalRepository;
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
public class RetrievalRepositoryQueryVerificationIT extends BaseIT {
    private static final Logger log = LoggerFactory.getLogger(RetrievalRepositoryQueryVerificationIT.class);

    private static final String TPP_RETRIEVAL_ID = "retrieval_id";
    private static final String COLLECTION_NAME = "retrieval";

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @Autowired 
    RetrievalRepository repository;


    @BeforeEach
    void setup() {
        // Drop collection
        StepVerifier.create(
            mongoTemplate.dropCollection(COLLECTION_NAME)
                .onErrorResume(e -> Mono.empty())
        ).verifyComplete();

        Retrieval testRetrieval = Retrieval.builder()
            .retrievalId(TPP_RETRIEVAL_ID)
            .deeplink("deeplink")
            .paymentButton("paymentButton")
            .originId("originId")
            .tppId("tppId")
        .build();
        
        StepVerifier.create(
            mongoTemplate.save(testRetrieval, COLLECTION_NAME)
                //.then(mongoTemplate.save(testConsent2, COLLECTION_NAME))
        ).expectNextCount(1).verifyComplete();

    }

    @Test
    void testFindByFiscalCode() {
        log.info("=== EXECUTING findByFiscalCode ===");

        StepVerifier.create(
                repository.findByRetrievalId(TPP_RETRIEVAL_ID)
            )
            .assertNext(retrieval -> {
                log.info("Found retrieval: {}", retrieval);
                assert retrieval.getRetrievalId().equals(TPP_RETRIEVAL_ID);

            })
            .verifyComplete();

        log.info("=== TEST COMPLETED - CHECK LOGS ABOVE FOR QUERY DETAILS ===");
    }

    @Test
    void testFindByFiscalCodeNotFound() {
        log.info("=== EXECUTING findByFiscalCode (not found) ===");

        StepVerifier.create(
                repository.findByRetrievalId("wrong_retrieval_id")
            )
            .verifyComplete();

        log.info("=== TEST COMPLETED ===");
    }

}