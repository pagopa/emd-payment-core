package it.gov.pagopa.emd.payment.model.base;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class RetrievalBase {

  private String retrievalId;
  private String deeplink;
  private String paymentButton;
  private String originId;
  private String tppId;
}
