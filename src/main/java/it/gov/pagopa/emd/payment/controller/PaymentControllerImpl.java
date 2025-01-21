package it.gov.pagopa.emd.payment.controller;


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
  public Mono<ResponseEntity<Void>> saveData() {
    return paymentServiceImpl.saveRetrival()
            .map(savedData -> ResponseEntity.status(HttpStatus.CREATED).build());
  }


  @Override
  public Mono<ResponseEntity<Void>> getData(@PathVariable String retrivalId) {
    return paymentServiceImpl.getRetrival()
              .map(data -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
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
