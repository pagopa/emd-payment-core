package it.gov.pagopa.emd.payment.connector;


import it.gov.pagopa.emd.payment.dto.TppDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TppConnectorImpl implements  TppConnector {
    private final WebClient webClient;

    public TppConnectorImpl(@Value("${rest-client.tpp.baseUrl}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Mono<TppDTO> getTppByEntityId(String entityId) {
        return webClient.get()
                .uri("/emd/tpp/entityId/{entityId}",entityId)
                .retrieve()
                .bodyToMono(TppDTO.class);
    }
}
