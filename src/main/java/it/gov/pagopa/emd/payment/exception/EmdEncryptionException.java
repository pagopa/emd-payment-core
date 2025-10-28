package it.gov.pagopa.emd.payment.exception;


import it.gov.pagopa.emd.payment.utils.CommonConstants;

/**
 * Specialized service exception for encryption operation failures.
 * <p>
 * It is specifically designed to handle errors that occur during encryption operations.  
 */
public class EmdEncryptionException extends ServiceException {

  public EmdEncryptionException(String message, boolean printStackTrace, Throwable ex) {
    this(CommonConstants.ExceptionCode.GENERIC_ERROR, message, printStackTrace, ex);
  }
  public EmdEncryptionException(String code, String message, boolean printStackTrace, Throwable ex) {
    super(code, message,null, printStackTrace, ex);
  }
}
