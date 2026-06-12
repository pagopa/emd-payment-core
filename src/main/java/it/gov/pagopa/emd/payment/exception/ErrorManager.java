package it.gov.pagopa.emd.payment.exception;

import it.gov.pagopa.emd.payment.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;
import java.util.Optional;

/**
 * Global exception handler for REST API endpoints using Spring's {@code @RestControllerAdvice}.
 * <p>
 * This class provides centralized exception handling for all REST controllers in the application,
 * ensuring consistent error response formats and appropriate HTTP status codes. It handles
 * different types of exceptions and converts them into standardized JSON error responses
 * or status-only responses based on the exception type.
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class ErrorManager {
  private final ErrorDTO defaultErrorDTO;

  public ErrorManager(@Nullable ErrorDTO defaultErrorDTO) {
    this.defaultErrorDTO = Optional.ofNullable(defaultErrorDTO)
            .orElse(new ErrorDTO("Error", "Something gone wrong"));
  }

  /**
   * Handles all {@link RuntimeException} instances thrown by REST controllers.
   * <p>
   * This method differentiates handling and logging levels based on whether the exception
   * is an expected business/client side-effect (4xx) or an unhandled server infrastructure error (5xx).
   * </p>
   *
   * @param error the runtime exception to handle
   * @return a {@link ResponseEntity} containing the structured error context
   */
  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<ErrorDTO> handleException(RuntimeException error) {

    // Process intelligent and contextual logging based on exception properties
    logClientException(error);

    if (error instanceof ClientExceptionNoBody clientExceptionNoBody) {
      return ResponseEntity.status(clientExceptionNoBody.getHttpStatus()).build();
    } else {
      ErrorDTO errorDTO;
      HttpStatus httpStatus;

      if (error instanceof ClientExceptionWithBody clientExceptionWithBody) {
        httpStatus = clientExceptionWithBody.getHttpStatus();
        errorDTO = new ErrorDTO(clientExceptionWithBody.getCode(), error.getMessage());
      }
      else if (error instanceof WebClientResponseException webClientResponseException) {
        httpStatus = HttpStatus.valueOf(webClientResponseException.getStatusCode().value());
        String responseBody = webClientResponseException.getResponseBodyAsString();

        if (isValidErrorDTO(responseBody)) {
          errorDTO = new ErrorDTO(Objects.requireNonNull(webClientResponseException.getResponseBodyAs(ErrorDTO.class)));
        } else {
          errorDTO = new ErrorDTO(webClientResponseException.getStatusCode().toString(),
                  webClientResponseException.getMessage());
        }
      }
      else {
        // Severe unhandled server exception path
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        errorDTO = defaultErrorDTO;
      }

      return ResponseEntity.status(httpStatus)
              .contentType(MediaType.APPLICATION_JSON)
              .body(errorDTO);
    }
  }

  /**
   * Provides logging for runtime exceptions with the appropriate logging level and detail.
   * It unwraps {@link ServiceException} causes when present and adjusts logging behavior based on:
   * <ul>
   * <li>Exception type</li>
   * <li>Stack trace printing preference</li>
   * <li>Presence of underlying causes</li>
   * </ul>
   * <p>
   * Logging levels:
   * <ul>
   * <li>ERROR level - For system exceptions, client exceptions with stack trace enabled,
   * or exceptions with underlying causes</li>
   * <li>INFO level - For simple client exceptions without stack trace requirements</li>
   * </ul>
   *
   * @param error the runtime exception to log
   */
  public static void logClientException(RuntimeException error) {
    Throwable unwrappedException = error.getCause() instanceof ServiceException
            ? error.getCause()
            : error;

    String clientExceptionMessage = "";
    boolean isClientException = error instanceof ClientException;

    if (isClientException) {
      ClientException clientException = (ClientException) error;
      clientExceptionMessage = "HttpStatus %s - %s%s".formatted(
              clientException.getHttpStatus(),
              (clientException instanceof ClientExceptionWithBody clientExceptionWithBody) ? clientExceptionWithBody.getCode() + ": " : "",
              clientException.getMessage()
      );
    }

    // Determine severity: if it's not a ClientException, or if stacktrace/nested causes are requested, log as ERROR
    if (!isClientException || ((ClientException) error).isPrintStackTrace() || unwrappedException.getCause() != null) {
      String exceptionName = error.getClass().getName();
      log.error("Something went wrong [Exception: {} - Details: {}] : {}", exceptionName, error.getMessage(), clientExceptionMessage, unwrappedException);
    } else {
      // Clean informational tracing for expected functional business results (e.g., 404 Retrieval Not Found)
      log.info("Expected Client Exception processed: {}", clientExceptionMessage);
    }
  }

  /**
   * Validates if a response body string contains valid ErrorDTO structure.
   * * @param responseBody the response body string to validate
   * @return true if the response body contains valid ErrorDTO structure, false otherwise
   */
  private boolean isValidErrorDTO(String responseBody) {
    return responseBody != null && responseBody.contains("\"code\"") && responseBody.contains("\"message\"");
  }
}