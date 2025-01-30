package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "payment_attempt")
public class PaymentAttempt {

  private String fiscalCode;
  private String tppId;
  private String originId;

  private List<AttemptDetails> attemptDetails;


}
