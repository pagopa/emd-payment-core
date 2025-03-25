package it.gov.pagopa.emd.payment.exception;


import it.gov.pagopa.emd.payment.utils.CommonConstants;

public class EmdEncryptionException extends ServiceException {

  public EmdEncryptionException(String message, boolean printStackTrace, Throwable ex) {
    this(CommonConstants.ExceptionCode.GENERIC_ERROR, message, printStackTrace, ex);
  }
  public EmdEncryptionException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}
