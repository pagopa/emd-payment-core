package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import reactor.core.publisher.Mono;

public interface StubPaymentService {

  Mono<RetrievalResponseDTO> saveRetrieval(String tppId, RetrievalRequestDTO retrievalRequestDTO);

  Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId);

  Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber);

}
