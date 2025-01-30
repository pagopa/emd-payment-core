package it.gov.pagopa.emd.payment.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptDetails {

  private String noticeNumber;
  private Date paymentAttemptDate;

}
