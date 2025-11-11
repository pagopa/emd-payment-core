package it.gov.pagopa.emd.payment.constant;

/**
 * Utility class that contains constant definitions for the payment module.
 */
public class PaymentConstants {

    /**
     * Contains constant definitions for exception codes.
     */
    public static final class ExceptionCode {
        public static final String TPP_NOT_FOUND = "TPP_NOT_FOUND";
        public static final String RETRIEVAL_NOT_FOUND = "RETRIEVAL_NOT_FOUND";
        public static final String AGENT_DEEP_LINKS_EMPTY = "AGENT_DEEP_LINKS_EMPTY";
        public static final String AGENT_NOT_FOUND_IN_DEEP_LINKS = "AGENT_NOT_FOUND_IN_DEEP_LINKS";


        private ExceptionCode() {}
    }

    /**
     * Contains constant definitions for exception messages.
     */
    public static final class ExceptionMessage {
        public static final String TPP_NOT_FOUND = "TPP does not exist or is not active";
        public static final String RETRIEVAL_NOT_FOUND = "Retrieval does not exist or is not active";
        public static final String GENERIC_ERROR = "GENERIC_ERROR";
        public static final String AGENT_DEEP_LINKS_EMPTY = "Agent deep links null or empty";
        public static final String AGENT_NOT_FOUND_IN_DEEP_LINKS = "Agent not found in deep links";


        private ExceptionMessage() {}
    }

    /**
     * Contains constant definitions for exception names used in exception mapping and handling.
     */
    public static final class ExceptionName {
        public static final String TPP_NOT_FOUND = "TPP_NOT_FOUND";
        public static final String RETRIEVAL_NOT_FOUND = "RETRIEVAL_NOT_FOUND";
        public static final String AGENT_DEEP_LINKS_EMPTY = "AGENT_DEEP_LINKS_EMPTY";
        public static final String AGENT_NOT_FOUND_IN_DEEP_LINKS = "AGENT_NOT_FOUND_IN_DEEP_LINKS";
        public static final String GENERIC_ERROR = "GENERIC_ERROR";

        private ExceptionName() {}
    }


    private PaymentConstants() {}
}
