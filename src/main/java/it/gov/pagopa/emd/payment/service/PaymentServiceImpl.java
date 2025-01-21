package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.dto.RetrivalDTO;
import reactor.core.publisher.Mono;

public class PaymentServiceImpl implements PaymentService {

  @Override
  public Mono<String> saveRetrival() {
    return null;
  }

  @Override
  public Mono<RetrivalDTO> getRetrival() {
    return null;
  }

  @Override
  public Mono<String> getRedirect(String fiscalCode, String noticeNumber) {
    return null;
  }
}
