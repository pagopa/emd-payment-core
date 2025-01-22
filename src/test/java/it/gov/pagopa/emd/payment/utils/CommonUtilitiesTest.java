package it.gov.pagopa.emd.payment.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;
import it.gov.pagopa.emd.payment.exception.EmdEncryptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonUtilitiesTest {

    @Mock
    private Message<String> messageMock;
    @Mock
    private ObjectReader objectReaderMock;

    @Test
    void createSHA256_Ko_NoSuchAlgorithm() {
        try (MockedStatic<MessageDigest> mockedStatic = Mockito.mockStatic(MessageDigest.class)) {
            mockedStatic.when(() -> MessageDigest.getInstance(any()))
                    .thenThrow(new NoSuchAlgorithmException("SHA-256 not available"));

            EmdEncryptionException exception = assertThrows(EmdEncryptionException.class, () -> Utils.createSHA256(""));

            assertEquals("SHA-256 not available", exception.getCause().getMessage());
        }
    }

    @Test
    void createSHA256_Ok() {
        String toHash = "RSSMRA98B18L049O";
        String hashedExpected = "0b393cbe68a39f26b90c80a8dc95abc0fe4c21821195b4671a374c1443f9a1bb";
        String actualHash = Utils.createSHA256(toHash);
        assertEquals(hashedExpected, actualHash);
    }

        record MyObject(String name) { }
}
