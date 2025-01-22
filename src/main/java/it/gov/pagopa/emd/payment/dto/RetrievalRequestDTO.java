package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class RetrievalRequestDTO {

    private String agent;
    private String originId;
}
