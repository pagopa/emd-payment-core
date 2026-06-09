package it.gov.pagopa.emd.payment.connector;

import it.gov.pagopa.emd.payment.dto.TppDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.gov.pagopa.emd.payment.enums.AuthenticationType.OAUTH2;
import static org.assertj.core.api.Assertions.assertThat;

class TppConnectorImplTest {

    private MockWebServer mockWebServer;
    private TppConnectorImpl tppConnector;

    private static final String TPP_JSON =
            "{\"tppId\":\"TPP_OK_1\",\"entityId\":\"ENTITY_OK_1\",\"businessName\":\"Test Business\"," +
            "\"messageUrl\":\"https://example.com/message\",\"authenticationUrl\":\"https://example.com/auth\"," +
            "\"authenticationType\":\"OAUTH2\",\"contact\":{\"name\":\"John Doe\",\"number\":\"+1234567890\"," +
            "\"email\":\"contact@example.com\"},\"state\":true}";

    private static MockResponse okTppResponse() {
        return new MockResponse()
                .setResponseCode(200)
                .setBody(TPP_JSON)
                .addHeader("Content-Type", "application/json");
    }

    private static MockResponse connectionResetResponse() {
        return new MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START);
    }

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        tppConnector = new TppConnectorImpl(WebClient.builder(), mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    // ── existing test ─────────────────────────────────────────────────────────

    @Test
    void testGetTppInfoOk() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"tppId\":\"TPP_OK_1\",\"entityId\":\"ENTITY_OK_1\",\"businessName\":\"Test Business\",\"messageUrl\":\"https://example.com/message\",\"authenticationUrl\":\"https://example.com/auth\",\"authenticationType\":\"OAUTH2\",\"contact\":{\"name\":\"John Doe\",\"number\":\"+1234567890\",\"email\":\"contact@example.com\"},\"state\":true}")
                .addHeader("Content-Type", "application/json"));

        Mono<TppDTO> resultMono = tppConnector.getTppByEntityId("TPP_OK_1");
        TppDTO tppDTO = resultMono.block();

        assertThat(tppDTO).isNotNull();
        assertThat(tppDTO.getTppId()).isEqualTo("TPP_OK_1");
        assertThat(tppDTO.getEntityId()).isEqualTo("ENTITY_OK_1");
        assertThat(tppDTO.getBusinessName()).isEqualTo("Test Business");
        assertThat(tppDTO.getMessageUrl()).isEqualTo("https://example.com/message");
        assertThat(tppDTO.getAuthenticationUrl()).isEqualTo("https://example.com/auth");
        assertThat(tppDTO.getAuthenticationType()).isEqualTo(OAUTH2);
        assertThat(tppDTO.getContact().getName()).isEqualTo("John Doe");
        assertThat(tppDTO.getContact().getNumber()).isEqualTo("+1234567890");
        assertThat(tppDTO.getContact().getEmail()).isEqualTo("contact@example.com");
        assertThat(tppDTO.getState()).isTrue();
    }

    // ── retry tests ───────────────────────────────────────────────────────────

    /**
     * 2 connection resets + 1 success → retries succeed.
     */
    @Test
    void testGetTppByEntityId_retriesOnConnectionResetAndSucceeds() {
        mockWebServer.enqueue(connectionResetResponse());
        mockWebServer.enqueue(connectionResetResponse());
        mockWebServer.enqueue(okTppResponse());

        StepVerifier.create(tppConnector.getTppByEntityId("ENTITY_OK_1"))
                .assertNext(dto -> assertThat(dto.getTppId()).isEqualTo("TPP_OK_1"))
                .verifyComplete();
    }

    /**
     * 3 consecutive connection resets → retries exhausted, error propagated.
     */
    @Test
    void testGetTppByEntityId_exhaustsRetriesAndPropagatesError() {
        mockWebServer.enqueue(connectionResetResponse());
        mockWebServer.enqueue(connectionResetResponse());
        mockWebServer.enqueue(connectionResetResponse());

        StepVerifier.create(tppConnector.getTppByEntityId("ENTITY_OK_1"))
                .expectErrorMatches(ex ->
                        ex instanceof WebClientRequestException ||
                        (ex.getCause() instanceof WebClientRequestException))
                .verify();
    }
}
