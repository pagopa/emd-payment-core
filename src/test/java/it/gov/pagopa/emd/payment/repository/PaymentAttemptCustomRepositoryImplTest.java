package it.gov.pagopa.emd.payment.repository;

import com.mongodb.client.result.UpdateResult;
import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptCustomRepositoryImplTest {

    @Mock
    private ReactiveMongoTemplate mongoTemplate;

    private PaymentAttemptCustomRepositoryImpl customRepository;

    @BeforeEach
    void setUp() {
        customRepository = new PaymentAttemptCustomRepositoryImpl(mongoTemplate);
    }

    /**
     * Verifies that the upsert flow builds the correct criteria and atomic operations,
     * and completes successfully.
     */
    @Test
    void testUpsertAttemptDetails_Success() {
        // Given
        String tppId = "test-tpp-id";
        String originId = "test-origin-id";

        AttemptDetails attemptDetails = new AttemptDetails();
        attemptDetails.setAmount("10.00");
        attemptDetails.setFiscalCode("RSSMRA80A01F205X");
        attemptDetails.setNoticeNumber("302012345678901234");
        attemptDetails.setPaymentAttemptDate(new Date());

        UpdateResult mockUpdateResult = UpdateResult.acknowledged(1L, 1L, null);
        when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq(PaymentAttempt.class)))
                .thenReturn(Mono.just(mockUpdateResult));

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);

        // When & Then
        StepVerifier.create(customRepository.upsertAttemptDetails(tppId, originId, attemptDetails))
                .verifyComplete();

        verify(mongoTemplate, times(1)).upsert(queryCaptor.capture(), updateCaptor.capture(), eq(PaymentAttempt.class));

        // 1. Verify Query filter criteria matching fields
        Query capturedQuery = queryCaptor.getValue();
        Document queryObj = capturedQuery.getQueryObject();
        assertEquals(tppId, queryObj.get("tppId"));
        assertEquals(originId, queryObj.get("originId"));

        // 2. Verify Update configuration structures
        Update capturedUpdate = updateCaptor.getValue();
        Document updateObj = capturedUpdate.getUpdateObject();

        // Assert atomic $setOnInsert operator initializing properties on a new document
        assertTrue(updateObj.containsKey("$setOnInsert"));
        Document setOnInsertObj = (Document) updateObj.get("$setOnInsert");
        assertEquals(tppId, setOnInsertObj.get("tppId"));
        assertEquals(originId, setOnInsertObj.get("originId"));
        assertEquals(PaymentAttempt.class.getName(), setOnInsertObj.get("_class"));

        // Assert atomic $push operator appending details to the nested array field
        assertTrue(updateObj.containsKey("$push"));
        Document pushObj = (Document) updateObj.get("$push");
        assertTrue(pushObj.containsKey("attemptDetails"));
    }

    /**
     * Verifies that any infrastructure exception emitted by ReactiveMongoTemplate
     * is correctly forwarded down the reactive stream.
     */
    @Test
    void testUpsertAttemptDetails_Failure() {
        // Given
        String tppId = "test-tpp-id";
        String originId = "test-origin-id";
        AttemptDetails attemptDetails = new AttemptDetails();

        when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq(PaymentAttempt.class)))
                .thenReturn(Mono.error(new RuntimeException("Cosmos DB Connection Timeout")));

        // When & Then
        StepVerifier.create(customRepository.upsertAttemptDetails(tppId, originId, attemptDetails))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && "Cosmos DB Connection Timeout".equals(throwable.getMessage()))
                .verify();

        verify(mongoTemplate, times(1)).upsert(any(Query.class), any(Update.class), eq(PaymentAttempt.class));
    }
}