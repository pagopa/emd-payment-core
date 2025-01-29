package it.gov.pagopa.emd.payment.exception;

import org.springframework.http.HttpStatus;

public class ClientExceptionNoBody extends ClientException {

  public ClientExceptionNoBody(HttpStatus httpStatus, String message) {
    super(httpStatus, message);
  }

}

