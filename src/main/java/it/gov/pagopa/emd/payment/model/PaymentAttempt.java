package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
public class PaymentAttempt {

  @Id
  private ObjectId id;

  private String fiscalCode;
  private String tppId;
  private String originId;

  private List<AttemptDetails> attemptDetails;


}
