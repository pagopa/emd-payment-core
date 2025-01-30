package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface PaymentAttemptRepository extends ReactiveMongoRepository<PaymentAttempt,String> {

    Mono<PaymentAttempt> findByTppIdAndOriginIdAndFiscalCode(String tppId, String originId, String fiscalCode);

    Mono<List<PaymentAttempt>> findAllByTppId(String tppId);

    Mono<List<PaymentAttempt>> findAllByTppIdAndFiscalCode(String tppId, String fiscalCode);

}
