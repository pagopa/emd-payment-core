package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.dto.RetrivalDTO;
import reactor.core.publisher.Mono;

public interface PaymentService {

  Mono<String> saveRetrival();

  Mono<RetrivalDTO> getRetrival();

  Mono<String> getRedirect(String fiscalCode, String noticeNumber);
}
