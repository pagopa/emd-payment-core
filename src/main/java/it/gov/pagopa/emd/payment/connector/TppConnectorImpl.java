package it.gov.pagopa.emd.payment.connector;


import it.gov.pagopa.emd.payment.dto.TppDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementation of the {@link TppConnector} interface that provides HTTP-based
 * communication with the EMD-TPP service.
 */
@Service
@Slf4j
public class TppConnectorImpl implements  TppConnector {
    private final WebClient webClient;

    /**
     * Constructs a new TppConnectorImpl with the specified base URL.
     * 
     * @param baseUrl the base URL for the TPP service, injected from the configuration
     */
    public TppConnectorImpl(@Value("${rest-client.tpp.baseUrl}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation performs an HTTP GET request to the TPP service endpoint.
     */
    public Mono<TppDTO> getTppByEntityId(String entityId) {
        return webClient.get()
                .uri("/emd/tpp/entityId/{entityId}",entityId)
                .retrieve()
                .bodyToMono(TppDTO.class);
    }
}
