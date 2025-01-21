package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.RetrivalDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/payment")
public interface PaymentController {

  @PostMapping("/retrivalTokens")
  Mono<ResponseEntity<Void>> createRetrival();

  @GetMapping("/retrivalTokens/{retrivalId}")
  Mono<ResponseEntity<RetrivalDTO>> getRetrival(@PathVariable String retrivalId) ;

  @GetMapping("/token/{fiscalCode}/{noticeNumber}")
  Mono<ResponseEntity<Void>> generateDeepLink(@PathVariable String fiscalCode, @PathVariable String noticeNumber);
}
