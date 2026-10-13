package it.gov.pagopa.emd.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Cursor-based page of payment summaries; totalElements counts matching payment_attempt documents. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchResponseDTO {
    private List<PaymentSearchItemDTO> content;
    private int pageSize;
    private long totalElements;
    private long totalPages;
    private String nextCursor;
    private boolean hasNext;
}

