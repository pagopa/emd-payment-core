package it.gov.pagopa.emd.payment.connector;


import it.gov.pagopa.emd.payment.dto.TppDTO;
import reactor.core.publisher.Mono;

public interface TppConnector {
    Mono<TppDTO> getTppByEntityId(String entityId);
}