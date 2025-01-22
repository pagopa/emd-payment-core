package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.model.Retrieval;

public class RetrievalFaker {
    public static Retrieval mockInstance() {
        return Retrieval.builder()
                .retrievalId("retrievalId")
                .deeplink("deepLink")
                .paymentButton("paymentButton")
                .originId("originId")
                .build();

    }

}
