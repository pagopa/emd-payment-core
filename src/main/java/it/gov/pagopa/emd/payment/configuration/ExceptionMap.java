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

@Configuration
@Slf4j
public class ExceptionMap {

    private final Map<String, Function<String, ClientException>> exceptions = new HashMap<>();

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
    }

    public RuntimeException throwException(String exceptionKey, String message) {
        if (exceptions.containsKey(exceptionKey)) {
            return exceptions.get(exceptionKey).apply(message);
        } else {
            log.error("[EMP-PAYMENT][EXCEPTION-MAP] Exception Name Not Found: {}", exceptionKey);
            return new RuntimeException();
        }
    }

}
