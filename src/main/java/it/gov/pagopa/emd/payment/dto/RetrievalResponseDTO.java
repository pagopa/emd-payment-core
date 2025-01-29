package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.emd.payment.model.base.RetrievalBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrievalResponseDTO extends RetrievalBase {
}
