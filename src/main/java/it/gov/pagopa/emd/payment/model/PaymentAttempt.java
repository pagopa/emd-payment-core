package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAttempt {

  private String fiscalCode;
  private String tppId;
  private String originId;

  private List<AttemptDetails> attemptDetails;


}
