package it.gov.pagopa.emd.payment.controller;


import it.gov.pagopa.emd.payment.dto.PaymentAttemptResponseDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.service.PaymentServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;


@Controller
public class PaymentControllerImpl implements PaymentController {

  private final PaymentServiceImpl paymentServiceImpl;

  public PaymentControllerImpl(PaymentServiceImpl paymentServiceImpl) {
    this.paymentServiceImpl = paymentServiceImpl;
  }

  @Override
  public Mono<ResponseEntity<RetrievalResponseDTO>> retrievalTokens(String entityId, RetrievalRequestDTO retrievalRequestDTO) {
    return paymentServiceImpl.saveRetrieval(entityId, retrievalRequestDTO)
            .map(ResponseEntity::ok);
  }


  @Override
  public Mono<ResponseEntity<RetrievalResponseDTO>> getRetrieval(String retrievalId) {
    return paymentServiceImpl.getRetrievalByRetrievalId(retrievalId)
            .map(ResponseEntity::ok);
  }


  @Override
  public Mono<ResponseEntity<Void>> generateDeepLink(String retrievalId, String fiscalCode, String noticeNumber){
    return paymentServiceImpl.getRedirect(retrievalId,fiscalCode,noticeNumber)
            .map(deepLink ->ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", deepLink)
                    .build()
            );
  }

  @Override
  public Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllPaymentAttemptsByTppId(String tppId){
    return paymentServiceImpl.getAllPaymentAttemptsByTppId(tppId)
           .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<List<PaymentAttemptResponseDTO>>> getAllAttemptDetailsByTppIdAndFiscalCode(String tppId, String fiscalCode){
    return paymentServiceImpl.getAllPaymentAttemptsByTppIdAndFiscalCode(tppId, fiscalCode)
            .map(ResponseEntity::ok);
  }


}
