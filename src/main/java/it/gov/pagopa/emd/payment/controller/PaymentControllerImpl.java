package it.gov.pagopa.emd.payment.controller;


import it.gov.pagopa.emd.payment.dto.RetrivalDTO;
import it.gov.pagopa.emd.payment.service.PaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


public class PaymentControllerImpl implements PaymentController {

  private final PaymentServiceImpl paymentServiceImpl;

  public PaymentControllerImpl(PaymentServiceImpl paymentServiceImpl) {
    this.paymentServiceImpl = paymentServiceImpl;
  }

  @Override
  public Mono<ResponseEntity<Void>> createRetrival() {
    return paymentServiceImpl.saveRetrival()
            .map(savedData -> ResponseEntity.status(HttpStatus.CREATED).build());
  }


  @Override
  public Mono<ResponseEntity<RetrivalDTO>> getRetrival(@PathVariable String retrivalId) {
    return paymentServiceImpl.getRetrival()
              .map(retrivalDTO -> ResponseEntity.status(HttpStatus.ACCEPTED).body(retrivalDTO));
  }


  @Override
  public Mono<ResponseEntity<Void>> generateDeepLink(@PathVariable String fiscalCode, @PathVariable String noticeNumber) {
    return paymentServiceImpl.getRedirect(fiscalCode,noticeNumber)
      .map(deepLink ->ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", deepLink)
        .build()
      );
  }
}
