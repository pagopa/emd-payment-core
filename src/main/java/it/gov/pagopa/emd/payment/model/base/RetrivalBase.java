package it.gov.pagopa.emd.payment.model.base;


import lombok.Data;

@Data
public class RetrivalBase {

  private String retrivalId;
  private String deeplink;
  private String paymentButton;
  private String originId;
}
