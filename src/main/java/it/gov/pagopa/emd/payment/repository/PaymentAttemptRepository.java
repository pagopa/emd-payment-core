package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentAttemptRepository extends ReactiveMongoRepository<PaymentAttempt,String> {

    Mono<PaymentAttempt> findByFiscalCodeAndTppIdAndOriginId(String fiscalCode, String tppId, String originId);

}
