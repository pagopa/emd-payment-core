package it.gov.pagopa.emd.payment.service;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for managing payment operations and retrieval processes.
 */
public interface PaymentService {

  /**
   * Creates and saves a new retrieval record for a TTP.
   * 
   * @param tppId the TPP identifier
   * @param retrievalRequestDTO the retrieval request containing origin and agent information
   * @return {@link Mono} containing the created {@link RetrievalResponseDTO} with retrieval details
   */
  Mono<RetrievalResponseDTO> saveRetrieval(String tppId, RetrievalRequestDTO retrievalRequestDTO);

  /**
   * Retrieves an existing retrieval record by its unique identifier.
   * 
   * @param retrievalId the unique retrieval identifier
   * @return {@link Mono} containing the {@link RetrievalResponseDTO} or empty if not found
   */
  Mono<RetrievalResponseDTO> getRetrievalByRetrievalId(String retrievalId);

  /**
   * Generates a redirect URL for payment processing.
   * 
   * @param retrievalId the unique retrieval identifier
   * @param fiscalCode the fiscal code
   * @param noticeNumber the payment notice number
   * @param amount amount of the payment
   * @return {@link Mono} containing the redirect URL string
   */
  Mono<String> getRedirect(String retrievalId, String fiscalCode, String noticeNumber, String amount);

  /**
   * Retrieves all payment attempts for a specific TPP.
   * 
   * @param tppId the TPP identifier
   * @return {@link Mono} containing a list of {@link PaymentAttemptResponseDTO} objects
   */
  Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppId(String tppId);

  /**
   * Retrieve a list of payment attempt by tppId and fiscal code.
   * 
   * @param tppId the TPP identifier
   * @param fiscalCode the fiscal code
   * @return {@link Mono} containing a list of {@link PaymentAttemptResponseDTO} objects
   */
  Mono<List<PaymentAttemptResponseDTO>> getAllPaymentAttemptsByTppIdAndFiscalCode(String tppId, String fiscalCode);

}
