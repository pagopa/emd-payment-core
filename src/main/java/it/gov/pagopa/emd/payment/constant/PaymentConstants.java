package it.gov.pagopa.emd.payment.constant;

public class PaymentConstants {
    public static final class ExceptionCode {
        public static final String TPP_NOT_FOUND = "TPP_NOT_FOUND";
        public static final String RETRIEVAL_NOT_FOUND = "RETRIEVAL_NOT_FOUND";

        private ExceptionCode() {}
    }

    public static final class ExceptionMessage {
        public static final String TPP_NOT_FOUND = "TPP does not exist or is not active";
        public static final String RETRIEVAL_NOT_FOUND = "Retrieval does not exist or is not active";
        public static final String GENERIC_ERROR = "GENERIC_ERROR";
        private ExceptionMessage() {}
    }

    public static final class ExceptionName {
        public static final String TPP_NOT_FOUND = "TPP_NOT_FOUND";
        public static final String RETRIEVAL_NOT_FOUND = "RETRIEVAL_NOT_FOUND";
        public static final String GENERIC_ERROR = "GENERIC_ERROR";

        private ExceptionName() {}
    }


    private PaymentConstants() {}
}
