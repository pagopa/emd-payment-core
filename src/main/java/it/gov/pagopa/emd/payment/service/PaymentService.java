package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PaymentService {

  Mono<RetrievalResponseDTO> saveRetrieval(String tppId, RetrievalRequestDTO retrievalRequestDTO);

  Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId);

  Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber);

  Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppId(String tppId);

  Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppIdAndFiscalCode(String tppId, String fiscalCode);

}
