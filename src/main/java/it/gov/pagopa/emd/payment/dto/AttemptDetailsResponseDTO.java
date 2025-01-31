package it.gov.pagopa.emd.payment.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttemptDetailsResponseDTO {

  private String noticeNumber;
  private Date paymentAttemptDate;

}
