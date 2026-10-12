package it.gov.pagopa.emd.payment.model.base;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base class containing common retrieval information.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Document(collection = "retrieval")
@CompoundIndex(name = "retrieval_tpp_origin_idx", def = "{'tppId': 1, 'originId': 1}")
public class RetrievalBase {

  private String retrievalId;
  /**
   * Notification address link
   */
  private String deeplink;
  private String pspDenomination;
  /**
   * Unique Numeric Identifier (IUN) of the notification
   */
  private String originId;
  private String tppId;
  private Boolean isPaymentEnabled;
}
