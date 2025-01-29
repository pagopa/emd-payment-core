package it.gov.pagopa.emd.payment.configuration;

import it.gov.pagopa.emd.payment.model.Retrieval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import reactor.core.publisher.Mono;

@Configuration
public class MongoConfig {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Bean
    public Mono<Void> createIndex() {
        ReactiveIndexOperations indexOps = reactiveMongoTemplate.indexOps(Retrieval.class);
        return indexOps.getIndexInfo()
                .collectList()
                .flatMap(indexInfos -> {
                    boolean indexExists = indexInfos.stream()
                            .anyMatch(indexInfo -> indexInfo.getName().equals("createdAt_1"));
                    if (!indexExists) {
                        return indexOps.ensureIndex(new Index().on("createdAt", Sort.DEFAULT_DIRECTION).expire(0)).then();
                    } else {
                        return Mono.empty();
                    }
                });    }

}

