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
     * Finds a single payment attempt by TPP ID, origin ID, and fiscal code.
     * 
     * @param tppId the TPP identifier
     * @param originId the unique numeric identifier (IUN) of the notification
     * @param fiscalCode the fiscal code or P.iva of TPP
     * @return {@link Mono} containing the matching PaymentAttempt or empty if no match is found
     */
    Mono<PaymentAttempt> findByTppIdAndOriginIdAndFiscalCode(String tppId, String originId, String fiscalCode);

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
     * 
     * @param tppId the TPP identifier
     * @param fiscalCode the fiscal code or P.iva of TPP
     * @return {@link Flux} containing all PaymentAttempt entities for the specified TPP and fiscal code,
     *         or empty Flux if no attempts are found for the combination
     */
    Flux<PaymentAttempt> findByTppIdAndFiscalCode(String tppId, String fiscalCode);

}
