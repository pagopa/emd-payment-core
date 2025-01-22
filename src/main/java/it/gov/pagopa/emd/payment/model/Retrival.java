package it.gov.pagopa.emd.payment.model;


import it.gov.pagopa.emd.payment.model.base.RetrivalBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
public class Retrival extends RetrivalBase {

  @Indexed(expireAfterSeconds = 3600)
  private Date createdAt;
}
