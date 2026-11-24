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
     * Documents without {@code createdAt} (created before the field was introduced, or by a previous
     * version of the service) are not discarded: null or missing values sort as the oldest, so they form
     * the last group of the ordering and are paged among themselves by {@code _id} descending.
     * If the cursor document does not exist anymore the result is empty.
     */
    @Override
    public Flux<PaymentAttempt> searchPayments(String iun, String tppId, ObjectId cursor, int limit) {
        List<Criteria> filters = buildSearchFilters(iun, tppId);
        if (cursor == null) {
            return mongoTemplate.find(buildSearchQuery(filters, limit), PaymentAttempt.class);
        }

        return mongoTemplate.findById(cursor, PaymentAttempt.class)
                .flatMapMany(cursorDocument -> {
                    List<Criteria> pagedFilters = new ArrayList<>(filters);
                    pagedFilters.add(buildAfterCursorCriteria(cursor, cursorDocument.getCreatedAt()));
                    return mongoTemplate.find(buildSearchQuery(pagedFilters, limit), PaymentAttempt.class);
                });
    }

    /**
     * Builds the search query combining all the given filters with a logical AND, sorted by creation date
     * descending and then by {@code _id} descending.
     * It requests one document more than {@code limit} so the caller can detect whether a next page exists.
     *
     * @param filters the filters to combine
     * @param limit   the page size requested by the client
     * @return the query to execute
     */
    private Query buildSearchQuery(List<Criteria> filters, int limit) {
        return new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)))
                .with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("_id")))
                .limit(limit + 1);
    }

    /**
     * Builds the condition selecting the documents that follow the cursor document in the
     * descending order of {@code createdAt} and {@code _id}, where documents without {@code createdAt}
     * come last.
     * <ul>
     *   <li>Cursor with a creation date: documents created before it, documents with the same date and a
     *   lower {@code _id}, and every document without creation date.</li>
     *   <li>Cursor without a creation date: only the documents without creation date and with a
     *   lower {@code _id}.</li>
     * </ul>
     *
     * @param cursor          the {@code _id} of the last document of the previous page
     * @param cursorCreatedAt the creation date of the cursor document, or {@code null} if it has none
     * @return the condition to add to the search filters
     */
    private Criteria buildAfterCursorCriteria(ObjectId cursor, Instant cursorCreatedAt) {
        if (cursorCreatedAt == null) {
            return new Criteria().andOperator(buildMissingCreatedAtCriteria(), Criteria.where("_id").lt(cursor));
        }
        return new Criteria().orOperator(
                Criteria.where("createdAt").lt(cursorCreatedAt),
                Criteria.where("createdAt").is(cursorCreatedAt).and("_id").lt(cursor),
                buildMissingCreatedAtCriteria());
    }

    /**
     * Builds the condition matching documents whose {@code createdAt} is {@code null} or not present at all.
     * Both cases are listed explicitly so the result does not depend on how the database treats
     * missing fields in an equality match with {@code null}.
     *
     * @return the condition matching documents without creation date
     */
    private Criteria buildMissingCreatedAtCriteria() {
        return new Criteria().orOperator(
                Criteria.where("createdAt").is(null),
                Criteria.where("createdAt").exists(false));
    }

    @Override
    public Mono<Long> countSearchPayments(String iun, String tppId) {
        List<Criteria> filters = buildSearchFilters(iun, tppId);
        Query query = filters.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        return mongoTemplate.count(query, PaymentAttempt.class);
    }

    /**
     * Builds the list of optional search filters, adding only those whose value is not {@code null}.
     *
     * @param iun   the notification identifier to match against {@code originId}, or {@code null}
     * @param tppId the TPP identifier to match against {@code tppId}, or {@code null}
     * @return the list of filters, empty if no value is provided
     */
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
