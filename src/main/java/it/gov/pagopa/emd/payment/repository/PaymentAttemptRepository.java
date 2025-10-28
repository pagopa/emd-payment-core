package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository interface for managing {@link PaymentAttempt} entities.
 */
@Repository
public interface PaymentAttemptRepository extends ReactiveMongoRepository<PaymentAttempt,String> {

    Mono<PaymentAttempt> findByTppIdAndOriginIdAndFiscalCode(String tppId, String originId, String fiscalCode);

    Flux<PaymentAttempt> findByTppId(String tppId);

    Flux<PaymentAttempt> findByTppIdAndFiscalCode(String tppId, String fiscalCode);

}
