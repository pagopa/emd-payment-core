package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.AttemptDetailsResponseDTO;
import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;

import java.util.ArrayList;

public class PaymentAttemptResponseDTOFaker {
    public static PaymentAttemptResponseDTO mockInstance() {
        ArrayList<AttemptDetailsResponseDTO> attemptDetails = new ArrayList<>();
        attemptDetails.add(AttemptDetailsResponseDTOFaker.mockInstance());

        return PaymentAttemptResponseDTO.builder()
                .attemptDetails(attemptDetails)
                .tppId("tppId")
                .fiscalCode("fiscalCode")
                .originId("originId")
                .build();

    }

}
