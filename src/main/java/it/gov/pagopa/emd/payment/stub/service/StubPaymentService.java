package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for stub payment operations.
 */
public interface StubPaymentService {

  /**
   * Creates and saves a new retrieval record in the stub environment.
   * 
   * @param tppId the TPP identifier
   * @param linkVersion version of the link, if null default link version is used
   * @param retrievalRequestDTO the retrieval request containing agent and origin information
   * @return {@link Mono} containing the created {@link RetrievalResponseDTO} with retrieval details
   */
  Mono<RetrievalResponseDTO> saveRetrieval(String tppId, String linkVersion, RetrievalRequestDTO retrievalRequestDTO);

  /**
   * Retrieves an existing retrieval record by its unique identifier.
   * 
   * @param retrievalId the unique retrieval identifier
   * @return {@link Mono} containing the {@link RetrievalResponseDTO} or empty if not found
   */
  Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId);

  /**
   * Generates a redirect URL for payment processing simulation.
   * 
   * @param retrievalId the unique retrieval identifier
   * @param fiscalCode the fiscal code
   * @param noticeNumber the payment notice number
   * @param amount amount of the payment
   * @return {@link Mono} containing the generated redirect URL string
   */
  Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber, String amount);

}
