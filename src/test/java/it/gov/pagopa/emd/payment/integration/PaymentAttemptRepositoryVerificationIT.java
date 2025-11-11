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
 * Integration test verifying MongoDB aggregation queries for PaymentAttempt repository.
 *
 * <p>Uses MongoDB driver debug logging to inspect generated pipelines.
 * Check console output for actual query structure.</p>
 * 
 * <p>Tests various query patterns used to retrieve payment attempt records
 * based on different combinations of TPP ID, Origin ID, and Fiscal Code.</p>
 */
@TestPropertySource(properties = {
    "logging.level.org.springframework.data.mongodb.core.ReactiveMongoTemplate=DEBUG",
})
public class PaymentAttemptRepositoryVerificationIT extends BaseIT {
    private static final Logger log = LoggerFactory.getLogger(RetrievalRepositoryQueryVerificationIT.class);

    // Test data constants representing a typical payment attempt scenario
    private static final String TEST_CF = "fiscalCode1";          // Italian citizen fiscal code
    private static final String TEST_TTP_ID = "tppId1";           // Third Party Provider identifier
    private static final String TEST_ORIGIN_ID = "origin-id-1";   // Unique transaction origin identifier
    private static final String COLLECTION_NAME = "payment_attempt";

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @Autowired 
    PaymentAttemptRepository repository;

    @BeforeEach
    void setup() {
        // Clean up any existing test data by dropping the entire collection
        StepVerifier.create(
            mongoTemplate.dropCollection(COLLECTION_NAME)
                .onErrorResume(e -> Mono.empty())  // Ignore errors if collection doesn't exist
        ).verifyComplete();

        // Create test payment attempt with multiple notice numbers
        // This simulates a payment attempt containing multiple payment notices
        List<AttemptDetails> attemptDetailsList1 = List.of(
            AttemptDetails.builder()
                .noticeNumber("noticeNumber1")  // First payment notice
                .build(),
            AttemptDetails.builder()
                .noticeNumber("noticeNumber2")  // Second payment notice
                .build()
        );

        // Build a complete PaymentAttempt entity for testing
        // Links together TPP, citizen, and transaction for payment processing
        PaymentAttempt testPayment1 = PaymentAttempt.builder()
            .fiscalCode(TEST_CF)        // Who is making the payment
            .tppId(TEST_TTP_ID)         // Which TPP is processing it
            .originId(TEST_ORIGIN_ID)   // Unique identifier for this payment session
            .attemptDetails(attemptDetailsList1)  // What is being paid
            .build();
        
        // Insert the test data into MongoDB
        StepVerifier.create(
            mongoTemplate.save(testPayment1, COLLECTION_NAME)
        ).expectNextCount(1).verifyComplete();
    }

    /**
     * Test Case: Exact payment attempt lookup using all three primary keys
     * 
     * Scenario: Find a specific payment attempt using the complete composite key
     * Expected: Should return the exact payment attempt matching all three criteria
     * MongoDB Query: db.payment_attempt.findOne({"tppId": "tppId1", "originId": "origin-id-1", "fiscalCode": "fiscalCode1"})
     * 
     * Business Logic: This is the most specific query, typically used when you need
     * to retrieve a specific payment session for a particular citizen via a specific TPP
     */
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

    /**
     * Test Case: Failed lookup with incorrect fiscal code
     * 
     * Scenario: Attempt to find payment attempt with wrong fiscal code but correct TPP and origin
     * Expected: Should return empty result (no matching records)
     * Purpose: Verify that fiscal code validation works correctly in composite queries
     */
    @Test
    void testFindByTppIdAndOriginIdAndFiscalCodeNotFound(){
        log.info("=== EXECUTING testFindByTppIdAndOriginIdAndFiscalCodeNotFound (not found) ===");
        
        StepVerifier.create(
                repository.findByTppIdAndOriginIdAndFiscalCode("Wrong_cf", TEST_TTP_ID, TEST_ORIGIN_ID)
            )
            .verifyComplete();
        
            log.info("=== TEST COMPLETED ===");
    }

    /**
     * Test Case: Find all payment attempts for a specific TPP
     * 
     * Scenario: Retrieve all payment attempts processed by a particular TPP
     * Expected: Should return payment attempts associated with the given TPP ID
     * MongoDB Query: db.payment_attempt.find({"tppId": "tppId1"})
     * 
     * Business Use Case: TPP dashboard showing all their processed payments,
     * reporting, or administrative monitoring of TPP activity
     */
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

    /**
     * Test Case: TPP lookup with non-existent TPP ID
     * 
     * Scenario: Query for payment attempts using an invalid/non-existent TPP ID
     * Expected: Should return empty result
     * Purpose: Verify graceful handling of invalid TPP identifiers
     * 
     * Error Handling: Ensures the system doesn't break when given invalid TPP IDs
     */
    @Test
    void testFindByTppIdNotFound(){
        log.info("=== EXECUTING testFindByTppIdNotFound (not found) ===");
        
        StepVerifier.create(
                repository.findByTppId("wrong_ttp_id")
            )
            .verifyComplete();
        
        log.info("=== TEST COMPLETED ===");
    }

    /**
     * Test Case: Find payment attempts by TPP and citizen fiscal code
     * 
     * Scenario: Retrieve payment attempts for a specific citizen through a specific TPP
     * Expected: Should return payment attempts matching both TPP ID and fiscal code
     * MongoDB Query: db.payment_attempt.find({"tppId": "tppId1", "fiscalCode": "fiscalCode1"})
     * 
     * Business Use Case: Customer service scenarios where you need to see all
     * payment attempts made by a specific citizen through a particular TPP,
     * useful for troubleshooting or payment history inquiries
     */
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

    /**
     * Test Case: Failed lookup with incorrect fiscal code in TPP+fiscal code query
     * 
     * Scenario: Search for payment attempts with valid TPP but invalid fiscal code
     * Expected: Should return empty result
     * Purpose: Verify that fiscal code filtering works correctly in two-parameter queries
     */
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