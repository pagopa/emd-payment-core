package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.AttemptDetailsResponseDTO;

import java.util.Date;

public class AttemptDetailsResponseDTOFaker {
    public static AttemptDetailsResponseDTO mockInstance() {
        return AttemptDetailsResponseDTO.builder()
                .noticeNumber("noticeNumber")
                .paymentAttemptDate(new Date())
                .build();
    }

}
