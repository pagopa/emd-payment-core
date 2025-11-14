package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;

public class RetrievalResponseDTOFaker {
    public static RetrievalResponseDTO mockInstance() {
        return RetrievalResponseDTO.builder()
                .retrievalId("retrievalId")
                .deeplink("deepLink")
                .paymentButton("paymentButton")
                .originId("originId")
                .isPaymentEnabled(Boolean.TRUE)
                .build();

    }

}
