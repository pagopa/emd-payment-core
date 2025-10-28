package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.emd.payment.exception.ServiceExceptionPayload;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object representing an error response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO implements ServiceExceptionPayload {

  @NotBlank
  private String code;
  @NotBlank
  private String message;

  public ErrorDTO(ErrorDTO errorDTO){
    this.code = errorDTO.getCode();
    this.message = errorDTO.getMessage();
  }
}
