package it.gov.pagopa.emd.payment.model.base;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class containing common retrieval information.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class RetrievalBase {

  private String retrievalId;
  /**
   * Notification address link
   */
  private String deeplink;
  private String pspDenomination;
  private String paymentBUtton;
  /**
   * Unique Numeric Identifier (IUN) of the notification
   */
  private String originId;
  private String tppId;
  private Boolean isPaymentEnabled;
}
