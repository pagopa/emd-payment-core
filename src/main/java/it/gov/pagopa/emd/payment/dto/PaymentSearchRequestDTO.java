package it.gov.pagopa.emd.payment.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Request parameters for cursor-based payment search. */
@Data
public class PaymentSearchRequestDTO {

    private String iun;
    private String psp;

    @Pattern(regexp = "(?i)^[0-9a-f]{24}$", message = "Il cursore deve essere un ObjectId MongoDB valido")
    private String cursor;

    @Min(value = 1, message = "Il limite deve essere almeno 1")
    @Max(value = 100, message = "Il limite massimo è 100")
    private int limit = 20;

    @AssertTrue(message = "Specificare almeno un criterio di ricerca: IUN oppure PSP")
    public boolean isSearchCriteriaPresent() {
        return (iun != null && !iun.isBlank()) || (psp != null && !psp.isBlank());
    }
}

