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

  @PostMapping("/retrievalTokens/{entityId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Valid @PathVariable String entityId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

  @GetMapping("/retrievalTokens/{retrievalId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Valid @PathVariable String retrievalId) ;

  @GetMapping("/token")
  Mono<ResponseEntity<Void>> generateDeepLink(@Valid @RequestParam String retrievalId, @Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber);

  @GetMapping("/paymentAttempts/{tppId}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllPaymentAttemptsByTppId(@Valid @PathVariable String tppId);

  @GetMapping("/paymentAttempts/{tppId}/{fiscalCode}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllAttemptDetailsByTppIdAndFiscalCode(@Valid @PathVariable String tppId, @Valid @PathVariable String fiscalCode);

}
