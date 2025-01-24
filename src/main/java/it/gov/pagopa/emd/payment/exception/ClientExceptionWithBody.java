package it.gov.pagopa.emd.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class ClientExceptionWithBody extends ResponseStatusException {
  private final String code;

  public ClientExceptionWithBody(HttpStatus httpStatus, String code, String message){
    super(httpStatus, message);
    this.code = code;
  }

}
