package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.Retrieval;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RetrievalRepository extends ReactiveMongoRepository<Retrieval,String> {

    Mono<Retrieval> findByRetrievalId(String retrievalId);

}
