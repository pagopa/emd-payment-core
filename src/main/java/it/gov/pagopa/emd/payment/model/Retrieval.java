package it.gov.pagopa.emd.payment.model;


import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Retrieval extends RetrievalBase {

  @CreatedDate
  private Date createdAt;
}
