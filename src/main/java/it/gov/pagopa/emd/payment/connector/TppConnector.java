package it.gov.pagopa.emd.payment.connector;


import it.gov.pagopa.emd.payment.dto.TppDTO;
import reactor.core.publisher.Mono;

/**
 *  Connector for the interaction with emd-tpp
 */ 
public interface TppConnector {

    /**
     * Find TPP object by TPP fiscal code
     * @param entityId  the fiscal code of the TPP
     * @return TPP object
     */
    Mono<TppDTO> getTppByEntityId(String entityId);
}