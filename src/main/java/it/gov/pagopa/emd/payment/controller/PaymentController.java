package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller interface for managing payment-related operations.
 * <p>
 * Base Path: {@code /emd/payment}
 */
@RestController
@RequestMapping("/emd/payment")
public interface PaymentController {

  /**
   * Creates a new retrieval token for the specified TPP.
   * 
   * @param entityId identifier of the TPP
   * @param retrievalRequestDTO the retrieval configuration object
   * @return a {@link Mono} containing a {@link ResponseEntity} with the 
   *         {@link RetrievalResponseDTO} representing the outcome of the token creation
   */
  @PostMapping("/retrievalTokens/{entityId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Valid @PathVariable String entityId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

  /**
   * Get a retrieval by retrievalId.
   * 
   * @param retrievalId the unique identifier of the retrieval
   * @return a {@link Mono} containing a {@link ResponseEntity} with the 
   *         {@link RetrievalResponseDTO} if found
   */
  @GetMapping("/retrievalTokens/{retrievalId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Valid @PathVariable String retrievalId) ;

  /**
   * Generate deep link by retrievalId, fiscalCode and noticeNumber.
   * 
   * @param retrievalId the unique identifier of the retrieval
   * @param fiscalCode the fiscal code of the TPP
   * @param noticeNumber the notice number
   * @return a {@link Mono} containing a {@link ResponseEntity} with void body.
   */
  @GetMapping("/token")
  Mono<ResponseEntity<Void>> generateDeepLink(@Valid @RequestParam String retrievalId, @Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber, @Valid @RequestParam String amount);

  /**
   * Retrieves all payment attempts associated with a specific TPP.
   * 
   * @param tppId the unique identifier of the TPP
   * @return a {@link Mono} containing a {@link ResponseEntity} with a list of 
   *         {@link PaymentAttemptResponseDTO} objects representing all payment attempts
   */
  @GetMapping("/paymentAttempts/{tppId}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllPaymentAttemptsByTppId(@Valid @PathVariable String tppId);

  /**
   * Retrieves payment attempts filtered by TPP ID and fiscal code.
   * 
   * @param tppId tppId the unique identifier of the Third Party Provider
   * @param fiscalCode the fiscal code
   * @return a {@link Mono} containing a {@link ResponseEntity} with a list of 
   *         {@link PaymentAttemptResponseDTO} objects
   */
  @GetMapping("/paymentAttempts/{tppId}/{fiscalCode}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllAttemptDetailsByTppIdAndFiscalCode(@Valid @PathVariable String tppId, @Valid @PathVariable String fiscalCode);

}
