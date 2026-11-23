package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom repository interface defining atomic operations for payment attempts.
 */
public interface PaymentAttemptCustomRepository {

    /**
     * Performs an atomic Upsert operation on MongoDB to push new attempt details.
     * This isolates the update logic from application-level concurrency race conditions.
     *
     * @param tppId          the TPP identifier
     * @param originId       the unique numeric identifier (IUN)
     * @param attemptDetails the new payment attempt details to append
     * @return a {@link Mono} signaling completion when the database operation succeeds
     */
    Mono<Void> upsertAttemptDetails(String tppId, String originId, AttemptDetails attemptDetails);

    /**
     * Searches payment attempts ordered by creation date descending, most recent first.
     * The cursor is the {@code _id} of the last document of the previous page.
     *
     * @param iun    the notification identifier to filter by, or {@code null}
     * @param tppId  the TPP identifier to filter by, or {@code null}
     * @param cursor the {@code _id} of the last document of the previous page, or {@code null} for the first page
     * @param limit  the page size; one more document is read to detect whether a next page exists
     * @return the matching documents, at most {@code limit + 1}
     */
    Flux<PaymentAttempt> searchPayments(String iun, String tppId, ObjectId cursor, int limit);

    /** Counts matching payment_attempt documents, independently of the page cursor. */
    Mono<Long> countSearchPayments(String iun, String tppId);
}