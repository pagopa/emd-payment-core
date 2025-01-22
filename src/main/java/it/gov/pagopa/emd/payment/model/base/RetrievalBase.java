package it.gov.pagopa.emd.payment.model.base;


import lombok.Data;

@Data
public class RetrievalBase {

  private String retrievalId;
  private String deeplink;
  private String paymentButton;
  private String originId;
}
