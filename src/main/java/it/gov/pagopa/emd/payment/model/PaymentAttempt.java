package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "payment_attempt")
public class PaymentAttempt {

  @Id
  private String id;

  private String fiscalCode;
  private String tppId;
  private String originId;

  private List<AttemptDetails> attemptDetails;


}
