package it.gov.pagopa.emd.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ClientException extends RuntimeException {
  private final HttpStatus httpStatus;
  private final boolean printStackTrace;

  public ClientException(
      HttpStatus httpStatus, String message, boolean printStackTrace, Throwable ex) {
    super(message, ex);
    this.httpStatus = httpStatus;
    this.printStackTrace = printStackTrace;
  }
}
