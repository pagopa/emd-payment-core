package it.gov.pagopa.emd.payment.controller;

import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller interface for managing payment-related operations.
 * <p>
 * Base Path: {@code /emd/payment}
 */
@Tag(
    name = "Payment Management", 
    description = "API per la gestione delle operazioni di pagamento."
)
@RestController
@RequestMapping("/emd/payment")
public interface PaymentController {

  /**
   * Creates a new retrieval token for the specified TPP.
   * 
   * @param entityId identifier of the TPP
   * @param retrievalRequestDTO the retrieval configuration object
   * @return a {@link Mono} containing a {@link ResponseEntity} with the 
   *         {@link RetrievalResponseDTO} representing the outcome of the token creation
   */
  @Operation(summary = "Create TPP retrieval token",
            description = "Creates a new retrieval token for a TPP. " +
                    "The token will be generated with the specified configuration version " +
                    "or use the default fallback version if the requested version is not available.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieval created successfully",
            content = @Content(schema = @Schema(implementation = RetrievalResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid TPP or agent")
    })
  @PostMapping("/retrievalTokens/{entityId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(@Parameter(description = "Entity Id", example = "04256050875", required = true)
    @Valid @PathVariable String entityId, @Valid @RequestBody RetrievalRequestDTO retrievalRequestDTO);

  /**
   * Get a retrieval by retrievalId.
   * 
   * @param retrievalId the unique identifier of the retrieval
   * @return a {@link Mono} containing a {@link ResponseEntity} with the 
   *         {@link RetrievalResponseDTO} if found
   */
  @Operation(summary = "Get a retrieval",
            description = "Retrieves detailed information about a retrieval token using its unique identifier. " +
                    "Returns TPP configuration, deep links, and payment settings.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieval retrieved successfully",
            content = @Content(schema = @Schema(implementation = RetrievalResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid retrieval Id")
    })
  @GetMapping("/retrievalTokens/{retrievalId}")
  Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(@Parameter(description = "Retrieval Id", example = "54763ac2-48be-4b55-a0ad-e94df963f79a-1766393182258", required = true)
    @Valid @PathVariable String retrievalId) ;

  /**
   * Generate deep link by retrievalId, fiscalCode and noticeNumber.
   * 
   * @param retrievalId the unique identifier of the retrieval
   * @param fiscalCode the fiscal code of the TPP
   * @param noticeNumber the notice number
   * @param amount (optional) amount of the payment
   * @return a {@link Mono} containing a {@link ResponseEntity} with void body.
   */
  @Operation(summary = "Generate deep link",
            description = "Generates a payment deep link redirect for the specified parameters. " + 
                            "Creates or updates payment attempt tracking and returns a 302 redirect " +
                            "to the TPP's payment application with fiscal code and notice number.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deeplink generated successfully",
            content = @Content(schema = @Schema(implementation = RetrievalResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
  @GetMapping("/token")
  Mono<ResponseEntity<Void>> generateDeepLink(
    @Parameter(description = "Retrieval Id", example = "54763ac2-48be-4b55-a0ad-e94df963f79a-1766393182258", required = true) @Valid @RequestParam String retrievalId, 
    @Parameter(description = "Fiscal Code", example = "RSSMRA80A01H501K", required = true) @Valid @RequestParam String fiscalCode,
    @Parameter(description = "Notice Number", example = "X01ABCDEF12345678901", required = true) @Valid @RequestParam String noticeNumber, 
    @Parameter(description = "Amount", example = "100.00", required = true) @RequestParam(required = false) String amount);

  /**
   * Retrieves all payment attempts associated with a specific TPP.
   * 
   * @param tppId the unique identifier of the TPP
   * @return a {@link Mono} containing a {@link ResponseEntity} with a list of 
   *         {@link PaymentAttemptResponseDTO} objects representing all payment attempts
   */
  @Operation(summary = "Get all TPP payment attempts",
            description = "Retrieves all payment attempts associated with a specific Third Party Provider. " +
                    "Returns a comprehensive list including attempt details, dates, and notice numbers " +
                    "for monitoring and analytics purposes.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment attempts retrieved successfully",
            content = @Content(schema = @Schema(implementation = RetrievalResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid TPP Id")
    })
  @GetMapping("/paymentAttempts/{tppId}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllPaymentAttemptsByTppId(
    @Parameter(description = "TPP Id", example = "be46399d-23e4-43d9-b2b8-41c8fd5f5e40-1732202076421", required = true) @Valid @PathVariable String tppId);

  /**
   * Retrieves payment attempts filtered by TPP ID and fiscal code.
   * 
   * @param tppId tppId the unique identifier of the Third Party Provider
   * @param fiscalCode the fiscal code
   * @return a {@link Mono} containing a {@link ResponseEntity} with a list of 
   *         {@link PaymentAttemptResponseDTO} objects
   */
    @Operation(summary = "Get user payment attempts by TPP",
            description = "Retrieves payment attempts filtered by TPP ID and user fiscal code. " +
                    "Provides a focused view of payment history for a specific user " +
                    "within a particular Third Party Provider context.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment attempts retrieved successfully",
            content = @Content(schema = @Schema(implementation = RetrievalResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid TPP Id or Fiscal code")
    })
  @GetMapping("/paymentAttempts/{tppId}/{fiscalCode}")
  Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllAttemptDetailsByTppIdAndFiscalCode(@Valid @PathVariable String tppId, @Valid @PathVariable String fiscalCode);

}
