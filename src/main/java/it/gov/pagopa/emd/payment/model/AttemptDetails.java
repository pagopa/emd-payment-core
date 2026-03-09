package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity class representing the details of a payment attempt.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptDetails {

  private String noticeNumber;
  /**
   * Fiscal code or P.iva of TPP
   */
  private String fiscalCode;
  private String amount;
  private Date paymentAttemptDate;

}
