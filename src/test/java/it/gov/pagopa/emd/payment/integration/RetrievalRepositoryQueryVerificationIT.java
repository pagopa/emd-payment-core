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
 * Integration test verifying MongoDB queries for Retrieval repository.
 *
 * <p>Tests payment retrieval operations by retrieval ID.</p>
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
        // Clean up and insert test retrieval record
        StepVerifier.create(
            mongoTemplate.dropCollection(COLLECTION_NAME)
                .onErrorResume(e -> Mono.empty())
        ).verifyComplete();

        // Create retrieval record with payment UI components (deeplink, button)
        Retrieval testRetrieval = Retrieval.builder()
            .retrievalId(TPP_RETRIEVAL_ID)  // Unique identifier for payment retrieval
            .deeplink("deeplink")           // Mobile app deep link
            .pspDenomination("pspDenomination") // Payment UI button configuration
            .paymentBUtton("copyOfpspDenomination") // Payment UI button configuration
            .originId("originId")           // Original transaction ID
            .tppId("tppId")                // TPP that created this retrieval
        .build();
        
        StepVerifier.create(
            mongoTemplate.save(testRetrieval, COLLECTION_NAME)
        ).expectNextCount(1).verifyComplete();
    }

    /**
     * Test Case: Successful retrieval lookup by ID
     * Scenario: Find payment retrieval record using its unique ID
     * Expected: Returns the matching retrieval with all payment UI data
     */
    @Test
    void testFindByFiscalCode() {
        log.info("=== EXECUTING findByRetrievalId ===");

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

    /**
     * Test Case: Handle non-existent retrieval ID
     * Scenario: Query for retrieval that doesn't exist
     * Expected: Returns empty result gracefully
     */
    @Test
    void testFindByFiscalCodeNotFound() {
        log.info("=== EXECUTING findByRetrievalId (not found) ===");

        StepVerifier.create(
                repository.findByRetrievalId("wrong_retrieval_id")
            )
            .verifyComplete(); 

        log.info("=== TEST COMPLETED ===");
    }
}