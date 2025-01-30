package it.gov.pagopa.emd.payment.faker;

import it.gov.pagopa.emd.payment.dto.RetrievalRequestDTO;
import it.gov.pagopa.emd.payment.dto.RetrievalResponseDTO;
import it.gov.pagopa.emd.payment.dto.TppDTO;
import it.gov.pagopa.emd.payment.model.PaymentAttempt;
import it.gov.pagopa.emd.payment.model.Retrieval;

public class TestUtils {


    public static final String RESPONSE = "OK";
    public static final RetrievalRequestDTO RETRIEVAL_REQUEST_DTO = RetrievalRequestDTOFaker.mockInstance();
    public static final RetrievalResponseDTO RETRIEVAL_RESPONSE_DTO = RetrievalResponseDTOFaker.mockInstance();
    public static final TppDTO TPP_DTO = TppDTOFaker.mockInstance();
    public static final Retrieval RETRIEVAL = RetrievalFaker.mockInstance();
    public static final PaymentAttempt PAYMENT_ATTEMPT = PaymentAttemptFaker.mockInstance();
    public static final String MESSAGE_URL = "messageUrl";
    public static final String AUTHENTICATION_URL = "authenticationUrl";
    public static final long RETRY = 1;
}
