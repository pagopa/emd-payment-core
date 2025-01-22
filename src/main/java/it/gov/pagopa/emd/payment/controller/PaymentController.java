package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/emd/payment")
public interface PaymentController {

  @PostMapping("/retrievalTokens/{tppId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Valid @PathVariable String tppId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

  @GetMapping("/retrievalTokens/{retrievalId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Valid @PathVariable String retrievalId) ;

  @GetMapping("/token")
  Mono<ResponseEntity<Void>> generateDeepLink(@Valid @RequestParam String retrievalId, @Valid @RequestParam String fiscalCode, @Valid @RequestParam String noticeNumber);

}
