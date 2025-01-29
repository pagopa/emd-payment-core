package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;

public class RetrievalRequestDTOFaker {
    public static RetrievalRequestDTO mockInstance() {
        return RetrievalRequestDTO.builder()
                .agent("agent")
                .originId("originId")
                .build();

    }

}
