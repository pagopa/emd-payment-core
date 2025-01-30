package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.model.AttemptDetails;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;

import java.util.ArrayList;

public class PaymentAttemptFaker {
    public static PaymentAttempt mockInstance() {
        ArrayList<AttemptDetails> attemptDetails = new ArrayList<>();
        attemptDetails.add(AttemptDetailsFaker.mockInstance());

        return PaymentAttempt.builder()
                .attemptDetails(attemptDetails)
                .tppId("tppId")
                .fiscalCode("fiscalCode")
                .originId("originId")
                .build();

    }

}
