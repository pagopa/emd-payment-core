package it.gov.pagopa.emd.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/payment")
public interface PaymentController {

  @PostMapping("/retrivalTokens")
  Mono<ResponseEntity<Void>> saveData();

  @GetMapping("/retrivalTokens/{retrivalId}")
  Mono<ResponseEntity<Void>> getData(@PathVariable String retrivalId) ;

  @GetMapping("/token/{fiscalCode}/{noticeNumber}")
  Mono<ResponseEntity<Void>> generateDeepLink(@PathVariable String fiscalCode, @PathVariable String noticeNumber);
}
