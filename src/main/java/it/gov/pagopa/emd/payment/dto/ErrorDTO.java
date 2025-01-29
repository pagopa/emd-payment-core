package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.emd.payment.exception.ServiceExceptionPayload;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
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
