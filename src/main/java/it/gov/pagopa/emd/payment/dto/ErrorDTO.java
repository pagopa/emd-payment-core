package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.emd.payment.exception.ServiceExceptionPayload;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorDTO implements ServiceExceptionPayload {

  @NotBlank
  private String code;
  @NotBlank
  private String message;
}
