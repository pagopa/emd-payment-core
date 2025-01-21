package it.gov.pagopa.emd.payment.model;


import lombok.Data;

@Data
public class RetrivalBase {

  private String retrivalId;
  private String deeplink;
  private String paymentButton;
  private String origindId;
}
