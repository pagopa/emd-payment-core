package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.model.AttemptDetails;

import java.util.Date;

public class AttemptDetailsFaker {
    public static AttemptDetails mockInstance() {
        return AttemptDetails.builder()
                .noticeNumber("noticeNumber")
                .paymentAttemptDate(new Date())
                .build();

    }

}
