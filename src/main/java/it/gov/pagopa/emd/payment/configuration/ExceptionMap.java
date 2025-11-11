package it.gov.pagopa.emd.payment.configuration;


import it.gov.pagopa.emd.payment.constant.PaymentConstants;
import it.gov.pagopa.emd.payment.exception.ClientException;
import it.gov.pagopa.emd.payment.exception.ClientExceptionWithBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Configuration class that provides a centralized mapping of exception names to their corresponding
 * ClientException instances for the payment module.
 */
@Configuration
@Slf4j
public class ExceptionMap {

    /**
     * Internal map that stores the association between exception names and their corresponding
     * exception factory functions
     */    
    private final Map<String, Function<String, ClientException>> exceptions = new HashMap<>();

    /**
     * Constructs a new ExceptionMap and initializes the internal exception mappings.
     */
    public ExceptionMap() {
        exceptions.put(PaymentConstants.ExceptionName.TPP_NOT_FOUND, message ->
                new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        PaymentConstants.ExceptionCode.TPP_NOT_FOUND,
                        message
                )
        );

        exceptions.put(PaymentConstants.ExceptionName.RETRIEVAL_NOT_FOUND, message ->
                new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        PaymentConstants.ExceptionCode.RETRIEVAL_NOT_FOUND,
                        message
                )
        );

        exceptions.put(PaymentConstants.ExceptionName.AGENT_DEEP_LINKS_EMPTY, message ->
                new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        PaymentConstants.ExceptionCode.AGENT_DEEP_LINKS_EMPTY,
                        message
                )
        );

        exceptions.put(PaymentConstants.ExceptionName.AGENT_NOT_FOUND_IN_DEEP_LINKS, message ->
                new ClientExceptionWithBody(
                        HttpStatus.NOT_FOUND,
                        PaymentConstants.ExceptionCode.AGENT_NOT_FOUND_IN_DEEP_LINKS,
                        message
                )
        );
    }

    /**
     * Creates and returns a RuntimeException based on the provided exception key and message.
     * 
     * @param exceptionKey the key identifying the type of exception to create.
     * @param message message the error message to be included in the exception
     * @return a {@link RuntimeException} instance
     */
    public RuntimeException throwException(String exceptionKey, String message) {
        if (exceptions.containsKey(exceptionKey)) {
            return exceptions.get(exceptionKey).apply(message);
        } else {
            log.error("[EMP-PAYMENT][EXCEPTION-MAP] Exception Name Not Found: {}", exceptionKey);
            return new RuntimeException();
        }
    }

}
