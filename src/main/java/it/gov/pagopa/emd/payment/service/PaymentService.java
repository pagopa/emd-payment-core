package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.dto.NetworkResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import reactor.core.publisher.Mono;

public interface PaymentService {

  Mono<RetrievalResponseDTO> saveRetrieval(String tppId, RetrievalRequestDTO retrievalRequestDTO);

  Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId);

  Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber);

  Mono<NetworkResponseDTO> testConnection(String tppName);
}
