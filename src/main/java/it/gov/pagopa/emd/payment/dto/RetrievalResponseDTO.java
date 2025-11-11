package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object representing the response for retrieval operations.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrievalResponseDTO extends RetrievalBase {
}
