package it.gov.pagopa.emd.payment.connector;


import it.gov.pagopa.emd.payment.dto.TppDTO;
import reactor.core.publisher.Mono;

/**
 *  Connector interface for the interaction with emd-tpp service.
 */ 
public interface TppConnector {

    /**
     * Retrieves a TPP by its entity identifier.
     *
     * @param entityId the identifier of the TPP to retrieve.
     *                This represents the unique identifier of the TPP entity.
     * @return a {@link Mono} that emits the {@link TppDTO} object containing the TPP
     *         information when found, or completes empty if no TPP exists with the
     *         specified entity ID.
     */
    Mono<TppDTO> getTppByEntityId(String entityId);
}