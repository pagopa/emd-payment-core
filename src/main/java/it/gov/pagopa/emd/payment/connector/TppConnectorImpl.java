package it.gov.pagopa.emd.payment.connector;

import it.gov.pagopa.emd.payment.configuration.WebClientRetrySpecs;
import it.gov.pagopa.emd.payment.dto.TppDTO;
import it.gov.pagopa.emd.payment.dto.TppBatchRequestDTO;
import it.gov.pagopa.emd.payment.dto.TppBatchResponseDTO;
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
public class TppConnectorImpl implements TppConnector {

    private final WebClient webClient;

    public TppConnectorImpl(WebClient.Builder webClientBuilder,
                            @Value("${rest-client.tpp.baseUrl}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Idempotent GET → permissive retry on any transient network error.
     */
    @Override
    public Mono<TppDTO> getTppByEntityId(String entityId) {
        return webClient.get()
                .uri("/emd/tpp/entityId/{entityId}", entityId)
                .retrieve()
                .bodyToMono(TppDTO.class)
                .retryWhen(WebClientRetrySpecs.transientNetwork())
                .doOnError(ex -> log.error(
                        "[TPP-CONNECTOR] GET /emd/tpp/entityId/{{entityId}} failed: {}",
                        ex.getMessage()));
    }

    @Override
    public Mono<TppBatchResponseDTO> getTppsByIds(TppBatchRequestDTO request) {
        return webClient.post()
                .uri("/emd/tpp/batch")
                .header("X-API-Version", "1")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TppBatchResponseDTO.class)
                .retryWhen(WebClientRetrySpecs.connectFailureOnly())
                .doOnError(ex -> log.error(
                        "[TPP-CONNECTOR] POST /emd/tpp/batch failed: {}",
                        ex.getMessage()));
    }
}
