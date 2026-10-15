package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Compact payment summary returned by payment search. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSearchItemDTO {
    private String iun;
    private String tppId;
    private String businessName;
    private String entityId;
    private long attemptsCount;
}

