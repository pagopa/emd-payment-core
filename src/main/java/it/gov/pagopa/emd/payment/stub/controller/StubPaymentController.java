package it.gov.pagopa.emd.payment.stub.controller;


import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller interface for stub payment operations and retrieval management.
 */
@RequestMapping("/stub/emd/payment")
public interface StubPaymentController {

    /**
     * Creates a new retrieval token for a specified entity.
     * 
     * @param entityId the unique identifier of the entity
     * @param retrievalRequestDTO the retrieval request containing agent and origin information
     * @return {@link Mono} containing ResponseEntity with the created RetrievalResponseDTO
     */
    @PostMapping("/retrievalTokens/{entityId}")
    Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Valid @PathVariable String entityId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

    /**
     * Retrieves an existing retrieval token by its unique identifier.
     * 
     * @param retrievalId the unique identifier of the retrieval
     * @return {@link Mono} containing ResponseEntity with the RetrievalResponseDTO
     */
    @GetMapping("/retrievalTokens/{retrievalId}")
    Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Valid @PathVariable String retrievalId) ;

    /**
     * Generates a deep link for payment redirection.
     * 
     * @param retrievalId the unique identifier of the retrieval
     * @param fiscalCode the fiscal code
     * @param noticeNumber the payment notice number
     * @return {@link Mono} containing ResponseEntity with redirect information
     */
    @GetMapping("/token")
    Mono<ResponseEntity<Void>> generateDeepLink(@Valid @RequestParam String retrievalId, @Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber);

    /**
     * Creates a payment for testing purposes.
     * 
     * @param fiscalCode the fiscal code
     * @param noticeNumber the payment notice number
     * @return {@link Mono} containing ResponseEntity with payment creation result
     */
    @GetMapping("/payment")
    Mono<ResponseEntity<String>> createPayment(@Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber);

}
