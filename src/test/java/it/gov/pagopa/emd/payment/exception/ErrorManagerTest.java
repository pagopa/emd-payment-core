package it.gov.pagopa.emd.payment.exception;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import it.gov.pagopa.emd.payment.utils.MemoryAppender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@WebFluxTest(value = {
        ErrorManagerTest.TestController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ErrorManagerTest.TestController.class, ErrorManager.class})
@Slf4j
class ErrorManagerTest {
  private static final String EXPECTED_GENERIC_ERROR = "{\"code\":\"Error\",\"message\":\"Something gone wrong\"}";

  @Autowired
  private WebTestClient webTestClient;
  @SpyBean
  private TestController testControllerSpy;

  private static MemoryAppender memoryAppender;

  @RestController
  static class TestController {
    @GetMapping("/test")
    String testEndpoint() {
      return "OK";
    }
  }

  @BeforeAll
  static void configureMemoryAppender() {
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    memoryAppender.start();
  }

  @BeforeEach
  void clearMemoryAppender() {
    memoryAppender.reset();
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ErrorManager.class.getName());
    logger.setLevel(ch.qos.logback.classic.Level.INFO);
    logger.addAppender(memoryAppender);
  }

  @Test
  void handleExceptionClientExceptionNoBody() {
    Mockito.doThrow(
                    new ClientExceptionNoBody(HttpStatus.BAD_REQUEST, "NOTFOUND ClientExceptionNoBody"))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest();

    checkStackTraceSuppressedLog(memoryAppender,
            "HttpStatus 400 BAD_REQUEST - NOTFOUND ClientExceptionNoBody");
  }

  @Test
  void handleExceptionClientExceptionWithBody() {
    Mockito.doThrow(
                    new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "Error", "Error ClientExceptionWithBody"))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json("{\"code\":\"Error\",\"message\":\"Error ClientExceptionWithBody\"}");

    Mockito.doThrow(
                    new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "Error", "Error ClientExceptionWithBody",
                            new Exception()))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json("{\"code\":\"Error\",\"message\":\"Error ClientExceptionWithBody\"}");
  }

  @Test
  void handleExceptionWebClientResponseException() {
    Mockito.doThrow(
                    new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "Error", "Error WebClientResponseException"))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .json("{\"code\":\"Error\",\"message\":\"Error WebClientResponseException\"}");

    Mockito.doThrow(
            new WebClientResponseException(
                    HttpStatus.NOT_FOUND.value(),
                    "Resource not found",
                    new HttpHeaders(),
                    null,
                    StandardCharsets.UTF_8
            ))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .json("{\"code\":\"404 NOT_FOUND\",\"message\":\"404 Resource not found\"}");
  }


  @Test
  void handleExceptionClientExceptionTest() {
    Mockito.doThrow(ClientException.class)
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .json(EXPECTED_GENERIC_ERROR);

    checkStackTraceSuppressedLog(memoryAppender, "HttpStatus null - null");
    memoryAppender.reset();

    Mockito.doThrow(
                    new ClientException(HttpStatus.BAD_REQUEST, "ClientException with httpStatus and message"))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .json(EXPECTED_GENERIC_ERROR);

    checkStackTraceSuppressedLog(memoryAppender, "HttpStatus 400 BAD_REQUEST - ClientException with httpStatus and message");
    memoryAppender.reset();

    Mockito.doThrow(new ClientException(HttpStatus.BAD_REQUEST,
                    "ClientException with httpStatus, message and throwable", new Throwable()))
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .json(EXPECTED_GENERIC_ERROR);

    checkLog(memoryAppender,
            "Something went wrong : HttpStatus 400 BAD_REQUEST - ClientException with httpStatus, message and throwable",
            "it.gov.pagopa.emd.payment.exception.ClientException: ClientException with httpStatus, message and throwable",
            "it.gov.pagopa.emd.payment.exception.ErrorManagerTest$TestController.testEndpoint"
    );
  }

  @Test
  void handleExceptionRuntimeException() {
    Mockito.doThrow(RuntimeException.class)
            .when(testControllerSpy).testEndpoint();

    webTestClient.get()
            .uri("/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody()
            .json(EXPECTED_GENERIC_ERROR);
  }

  public static void checkStackTraceSuppressedLog(MemoryAppender memoryAppender, String expectedLoggedMessage) {
    String loggedMessage = memoryAppender.getLoggedEvents().get(0).getFormattedMessage();
    Assertions.assertTrue(Pattern.matches(expectedLoggedMessage, loggedMessage),
            "Unexpected logged message: " + loggedMessage);
  }

  public static void checkLog(MemoryAppender memoryAppender, String expectedLoggedMessageRegexp, String expectedLoggedExceptionMessage, String expectedLoggedExceptionOccurrencePosition) {
    ILoggingEvent loggedEvent = memoryAppender.getLoggedEvents().get(0);
    IThrowableProxy loggedException = loggedEvent.getThrowableProxy();
    StackTraceElementProxy loggedExceptionOccurrenceStackTrace = loggedException.getStackTraceElementProxyArray()[0];

    String loggedMessage = loggedEvent.getFormattedMessage();
    Assertions.assertTrue(Pattern.matches(expectedLoggedMessageRegexp,
                    loggedEvent.getFormattedMessage()),
            "Unexpected logged message: " + loggedMessage);

    Assertions.assertEquals(expectedLoggedExceptionMessage,
            loggedException.getClassName() + ": " + loggedException.getMessage());

    Assertions.assertEquals(expectedLoggedExceptionOccurrencePosition,
            loggedExceptionOccurrenceStackTrace.getStackTraceElement().getClassName() + "." + loggedExceptionOccurrenceStackTrace.getStackTraceElement().getMethodName());
  }
}
