package it.gov.pagopa.emd.payment.model;


import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
public class Retrieval extends RetrievalBase {

  @Indexed(expireAfterSeconds = 36000)
  private Date createdAt;
}
