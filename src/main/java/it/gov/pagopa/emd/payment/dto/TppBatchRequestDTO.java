package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Request body for looking up multiple TPP records. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TppBatchRequestDTO {
    private List<String> ids;
}
