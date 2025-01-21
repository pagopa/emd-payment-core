package it.gov.pagopa.emd.payment.repository;

import it.gov.pagopa.emd.payment.model.Retrival;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RetrivalRepository extends ReactiveMongoRepository<Retrival,String> {
}
