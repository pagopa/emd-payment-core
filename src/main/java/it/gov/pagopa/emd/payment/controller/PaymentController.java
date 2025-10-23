package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequestMapping("/emd/payment")
public interface PaymentController {

  /**
   * Save new Retrieval
   * 
   * @param entityId the fiscal code of the TPP
   * @param retrievalRequestDTO retrieval object
   * @return outcome of saving the retrieval
   */
  @PostMapping("/retrievalTokens/{entityId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Valid @PathVariable String entityId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

  /**
   * Get a retrieval by retrievalId
   * 
   * @param retrievalId to get
   * @return outcome of getting retrieval
   */
  @GetMapping("/retrievalTokens/{retrievalId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Valid @PathVariable String retrievalId) ;

  /**
   * Generate deep link by retrievalId, fiscalCode and noticeNumber
   * 
   * @param retrievalId to get 
   * @param fiscalCode the fiscal code of the TPP
   * @param noticeNumber
   * @return outcome of generate deep link
   */
  @GetMapping("/token")
  Mono<ResponseEntity<Void>> generateDeepLink(@Valid @RequestParam String retrievalId, @Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber);

  /**
   * Retrive a list of payment attempt by tppId
   * 
   * @param tppId to get
   * @return outcome of get all payment attempts
   */
  @GetMapping("/paymentAttempts/{tppId}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllPaymentAttemptsByTppId(@Valid @PathVariable String tppId);

  /**
   * Retrive a list of payment attempt by tppId and fiscal code
   * 
   * @param tppId to get
   * @param fiscalCode the fiscal code of the TPP
   * @return outcome of get all payment attempts
   */
  @GetMapping("/paymentAttempts/{tppId}/{fiscalCode}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllAttemptDetailsByTppIdAndFiscalCode(@Valid @PathVariable String tppId, @Valid @PathVariable String fiscalCode);

}
