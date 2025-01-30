package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.model.PaymentAttempt;

import java.util.ArrayList;

public class PaymentAttemptFaker {
    public static PaymentAttempt mockInstance() {
        return PaymentAttempt.builder()
                .attemptDetails(new ArrayList<>())
                .tppId("tppId")
                .fiscalCode("fiscalCode")
                .originId("originId")
                .tppId("tppId")
                .build();

    }

}
