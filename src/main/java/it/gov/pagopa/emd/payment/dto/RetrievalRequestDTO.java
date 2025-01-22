package it.gov.pagopa.emd.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class RetrievalRequestDTO {

    @NotNull
    private String agent;
    @NotNull
    private String originId;
}
