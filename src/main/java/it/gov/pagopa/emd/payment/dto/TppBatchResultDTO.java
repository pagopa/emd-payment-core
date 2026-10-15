package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Minimal TPP data returned by the batch lookup endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TppBatchResultDTO {
	private String tppId;
	private String businessName;
	private String entityId;
}

