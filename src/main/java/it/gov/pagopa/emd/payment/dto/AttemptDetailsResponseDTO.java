package it.gov.pagopa.emd.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data Transfer Object representing the details of a payment attempt.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptDetailsResponseDTO {

  private String noticeNumber;
  private Date paymentAttemptDate;

}
