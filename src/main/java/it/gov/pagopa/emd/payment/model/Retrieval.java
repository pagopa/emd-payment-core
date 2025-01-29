package it.gov.pagopa.emd.payment.model;


import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class Retrieval extends RetrievalBase {

  @CreatedDate
  private Date createdAt;
}
