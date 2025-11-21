package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.model.Retrieval;

public class RetrievalFaker {
    public static Retrieval mockInstance() {
        return Retrieval.builder()
                .retrievalId("retrievalId")
                .deeplink("deepLink")
                .pspDenomination("pspDenomination")
                .paymentBUtton("copyOfpspDenomination")
                .originId("originId")
                .isPaymentEnabled(Boolean.TRUE)
                .build();

    }

}
