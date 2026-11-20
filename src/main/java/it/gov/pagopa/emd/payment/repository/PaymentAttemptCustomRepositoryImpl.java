package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the custom repository fragment utilizing {@link ReactiveMongoTemplate}
 * to achieve atomic operations at the database engine level.
 */
public class PaymentAttemptCustomRepositoryImpl implements PaymentAttemptCustomRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public PaymentAttemptCustomRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> upsertAttemptDetails(String tppId, String originId, AttemptDetails attemptDetails) {
        // Build the unique criteria query for the targeted document location
        Query query = new Query(Criteria.where("tppId").is(tppId).and("originId").is(originId));

        // Define atomic modifier operators ($push and $setOnInsert) to execute within a single write-lock
        Update update = new Update()
                .setOnInsert("tppId", tppId)
                .setOnInsert("originId", originId)
                .setOnInsert("createdAt", Instant.now())
                .setOnInsert("_class", PaymentAttempt.class.getName())
                .push("attemptDetails", attemptDetails);

        // Execute the native findAndModify/upsert operation on the single document reference
        return mongoTemplate.upsert(query, update, PaymentAttempt.class).then();
    }

    /**
     * Searches payment attempts ordered by creation date descending (most recent first),
     * with {@code _id} descending as tie-breaker.
     * The cursor is the {@code _id} of the last returned document: its {@code createdAt} is resolved
     * with an {@code _id} lookup so the cursor format exposed by the API does not change.
     * All documents are expected to have {@code createdAt}. If the cursor document does not exist
     * (or has no {@code createdAt}) the result is empty.
     */
    @Override
    public Flux<PaymentAttempt> searchPayments(String iun, String tppId, ObjectId cursor, int limit) {
        List<Criteria> filters = buildSearchFilters(iun, tppId);
        if (cursor == null) {
            return mongoTemplate.find(buildSearchQuery(filters, limit), PaymentAttempt.class);
        }

        return mongoTemplate.findById(cursor, PaymentAttempt.class)
                .filter(cursorDocument -> cursorDocument.getCreatedAt() != null)
                .flatMapMany(cursorDocument -> {
                    List<Criteria> pagedFilters = new ArrayList<>(filters);
                    pagedFilters.add(buildAfterCursorCriteria(cursor, cursorDocument.getCreatedAt()));
                    return mongoTemplate.find(buildSearchQuery(pagedFilters, limit), PaymentAttempt.class);
                });
    }

    private Query buildSearchQuery(List<Criteria> filters, int limit) {
        return new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)))
                .with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("_id")))
                .limit(limit + 1);
    }

    private Criteria buildAfterCursorCriteria(ObjectId cursor, Instant cursorCreatedAt) {
        return new Criteria().orOperator(
                Criteria.where("createdAt").lt(cursorCreatedAt),
                Criteria.where("createdAt").is(cursorCreatedAt).and("_id").lt(cursor));
    }

    @Override
    public Mono<Long> countSearchPayments(String iun, String tppId) {
        List<Criteria> filters = buildSearchFilters(iun, tppId);
        Query query = filters.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        return mongoTemplate.count(query, PaymentAttempt.class);
    }

    private List<Criteria> buildSearchFilters(String iun, String tppId) {
        List<Criteria> filters = new ArrayList<>();
        if (iun != null) {
            filters.add(Criteria.where("originId").is(iun));
        }
        if (tppId != null) {
            filters.add(Criteria.where("tppId").is(tppId));
        }
        return filters;
    }
}