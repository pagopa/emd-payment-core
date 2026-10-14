package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Response body from the TPP batch lookup endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TppBatchResponseDTO {
    private List<TppBatchResultDTO> results;
    private List<String> missingTppIds;
}
