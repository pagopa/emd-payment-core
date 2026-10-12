package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Cursor-based page of payment summaries. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchResponseDTO {
    private List<PaymentSearchItemDTO> payments;
    private long returnedCount;
    private long attemptsCount;
    private String nextCursor;
    private boolean hasMore;
}

