package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.Retrieval;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository interface for managing {@link Retrieval} entities.
 * <p>
 * Collection name: {@code retrieval}
 */
@Repository
public interface RetrievalRepository extends ReactiveMongoRepository<Retrieval,String> {

     /**
     * Finds a single retrieval record by its unique retrieval identifier.
     * 
     * @param retrievalId the unique retrieval identifier
     * @return {@link Mono} containing the matching Retrieval entity or empty if not found
     */
    Mono<Retrieval> findByRetrievalId(String retrievalId);

}
