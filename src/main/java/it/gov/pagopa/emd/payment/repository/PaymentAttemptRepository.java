package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository interface for managing {@link PaymentAttempt} entities.
 * <p>
 * Collection name: {@code payment_attempt}
 */
@Repository
public interface PaymentAttemptRepository extends ReactiveMongoRepository<PaymentAttempt,String> {

    /**
     * Finds a single payment attempt by TPP ID, origin ID.
     * 
     * @param tppId the TPP identifier
     * @param originId the unique numeric identifier (IUN) of the notification
     * @return {@link Mono} containing the matching PaymentAttempt or empty if no match is found
     */
    Mono<PaymentAttempt> findByTppIdAndOriginId(String tppId, String originId);

    /**
     * Finds all payment attempts associated with a specific TPP.
     * 
     * @param tppId the TPP identifier
     * @return {@link Flux} containing all PaymentAttempt entities for the specified TPP,
     *         or empty Flux if no attempts are found
     */
    Flux<PaymentAttempt> findByTppId(String tppId);

    /**
     * Finds all payment attempts for a specific TPP and fiscal code combination.
     * Query = { 'tppId': ?0, 'attemptDetails.fiscalCode': ?1 }
     * 
     * @param tppId the TPP identifier
     * @param fiscalCode the fiscal code to filter payment attempts
     * @return {@link Flux} containing all PaymentAttempt entities for the specified TPP and fiscal code,
     *         or empty Flux if no attempts are found for the combination
     */
    Flux<PaymentAttempt> findByTppIdAndAttemptDetailsFiscalCode(String tppId, String fiscalCode);

}
