package it.gov.pagopa.emd.payment.model;


import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Document(collection = "retrieval")
public class Retrieval extends RetrievalBase {

  private Date createdAt;
}