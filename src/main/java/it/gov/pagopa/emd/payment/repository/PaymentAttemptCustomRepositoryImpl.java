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
                .setOnInsert("_class", PaymentAttempt.class.getName())
                .push("attemptDetails", attemptDetails);

        // Execute the native findAndModify/upsert operation on the single document reference
        return mongoTemplate.upsert(query, update, PaymentAttempt.class).then();
    }

    @Override
    public Flux<PaymentAttempt> searchPayments(String iun, String tppId, ObjectId cursor, int limit) {
        List<Criteria> filters = new ArrayList<>();
        if (iun != null) {
            filters.add(Criteria.where("originId").is(iun));
        }
        if (tppId != null) {
            filters.add(Criteria.where("tppId").is(tppId));
        }
        if (cursor != null) {
            filters.add(Criteria.where("_id").gt(cursor));
        }

        Query query = new Query(new Criteria().andOperator(filters.toArray(Criteria[]::new)))
                .with(Sort.by(Sort.Direction.ASC, "_id"))
                .limit(limit + 1);
        return mongoTemplate.find(query, PaymentAttempt.class);
    }
}