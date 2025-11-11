package it.gov.pagopa.emd.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing the response for payment attempt.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentAttemptResponseDTO {

    private String fiscalCode;
    private String originId;
    private String tppId;

    private List<AttemptDetailsResponseDTO> attemptDetails;

}
