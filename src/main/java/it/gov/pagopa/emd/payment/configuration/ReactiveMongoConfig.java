package it.gov.pagopa.emd.payment.configuration;

import it.gov.pagopa.emd.payment.model.Retrieval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;

@Configuration
@Slf4j
public class ReactiveMongoConfig {

    @Value("${app.mongo.ttl.seconds}")
    private long ttlSeconds;

    @Bean
    public void ensureIndexes(ReactiveMongoTemplate reactiveMongoTemplate) {
        ReactiveIndexOperations indexOps = reactiveMongoTemplate.indexOps(Retrieval.class);

        Index ttlIndex = new Index()
                .on("createdAt", org.springframework.data.domain.Sort.Direction.ASC)
                .expire(ttlSeconds);

        indexOps.ensureIndex(ttlIndex).doOnSuccess(result ->
                log.info("[REACTIVE-MONGO-CONFIG] TTL index created result: " + result))
                .subscribe();
    }
}

