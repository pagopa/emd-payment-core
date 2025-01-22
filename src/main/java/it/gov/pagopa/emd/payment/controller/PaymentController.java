package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.RetrivalDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/payment")
public interface PaymentController {

  /*
    1 - Identifica TPP da access token
    2 - Ricerca deeplink e payment button
    3 - Genera retrival id (UUID)
    4 - Memorizza il retrival impostando un time-to-live


   */
  @PostMapping("/retrivalTokens/{tppId}")
  Mono<ResponseEntity<Void>> createRetrival();

  /*
      1 - Ricerca e restituisce retrival tramite retrivalId
   */
  @GetMapping("/retrivalTokens/{retrivalId}")
  Mono<ResponseEntity<RetrivalDTO>> getRetrival(@PathVariable String retrivalId) ;

  /*
      1 - Ricerca retrival tramite retrivalId
      2 - Genera redirect tramite deeplink fiscalCode e noticeNumber
   */
  @GetMapping("/token/{retrivalId}/{fiscalCode}/{noticeNumber}")
  Mono<ResponseEntity<Void>> generateDeepLink(@PathVariable String retrivalId, @PathVariable String fiscalCode, @PathVariable String noticeNumber);
}
