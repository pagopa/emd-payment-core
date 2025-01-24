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

@RestControllerAdvice
@Slf4j
public class ErrorManager {
  private final ErrorDTO defaultErrorDTO;

  public ErrorManager(@Nullable ErrorDTO defaultErrorDTO) {
    this.defaultErrorDTO = Optional.ofNullable(defaultErrorDTO)
            .orElse(new ErrorDTO("Error", "Something gone wrong"));
  }

  @ExceptionHandler(RuntimeException.class)
  protected ResponseEntity<ErrorDTO> handleException(RuntimeException error) {

    logClientException(error);

    log.error("Caught exception: {}", error.getClass().getName());
    log.error("Exception details: {}", error.getMessage());

    if(error instanceof ClientExceptionNoBody clientExceptionNoBody){
      return ResponseEntity.status(clientExceptionNoBody.getHttpStatus()).build();
    }
    else {
      ErrorDTO errorDTO;
      HttpStatus httpStatus;
      if (error instanceof ClientExceptionWithBody clientExceptionWithBody){
        httpStatus=clientExceptionWithBody.getHttpStatus();
        errorDTO = new ErrorDTO(clientExceptionWithBody.getCode(),  error.getMessage());
      }
      else if(error instanceof WebClientResponseException webClientResponseException){
        httpStatus=HttpStatus.valueOf(webClientResponseException.getStatusCode().value());
        String responseBody = webClientResponseException.getResponseBodyAsString();

        if (isValidErrorDTO(responseBody)) {
          errorDTO = new ErrorDTO(Objects.requireNonNull(webClientResponseException.getResponseBodyAs(ErrorDTO.class)));
        } else {
          errorDTO = new ErrorDTO(webClientResponseException.getStatusCode().toString(),
                  webClientResponseException.getMessage());
        }
      }
      else {
        httpStatus=HttpStatus.INTERNAL_SERVER_ERROR;
        errorDTO = defaultErrorDTO;
      }
      return ResponseEntity.status(httpStatus)
              .contentType(MediaType.APPLICATION_JSON)
              .body(errorDTO);
    }
  }
  public static void logClientException(RuntimeException error) {
    Throwable unwrappedException = error.getCause() instanceof ServiceException
            ? error.getCause()
            : error;

    String clientExceptionMessage = "";
    if(error instanceof ClientException clientException) {
      clientExceptionMessage = "HttpStatus %s - %s%s".formatted(
              clientException.getHttpStatus(),
              (clientException instanceof ClientExceptionWithBody clientExceptionWithBody) ? clientExceptionWithBody.getCode() + ": " : "",
              clientException.getMessage()
      );
    }

    if(!(error instanceof ClientException clientException) || clientException.isPrintStackTrace() || unwrappedException.getCause() != null){
      log.error("Something went wrong : {}", clientExceptionMessage, unwrappedException);
    } else {
      log.info("{}",clientExceptionMessage);
    }
  }

  private boolean isValidErrorDTO(String responseBody) {
    return responseBody != null && responseBody.contains("\"code\"") && responseBody.contains("\"message\"");
  }


}
