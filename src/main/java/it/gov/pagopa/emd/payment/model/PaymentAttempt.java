package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB document entity representing a payment attempt record.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "payment_attempt")
@CompoundIndexes({
    @CompoundIndex(name = "payment_attempt_tpp_id_id_idx", def = "{'tppId': 1, '_id': 1}"),
    @CompoundIndex(name = "payment_attempt_origin_id_id_idx", def = "{'originId': 1, '_id': 1}"),
    @CompoundIndex(name = "payment_attempt_tpp_origin_id_idx", def = "{'tppId': 1, 'originId': 1, '_id': 1}")
})
public class PaymentAttempt {

  @Id
  private ObjectId id;
  private String tppId;
  /**
   * Unique numeric identifier (IUN) of the notification
   */
  private String originId;
  private List<AttemptDetails> attemptDetails;

}
