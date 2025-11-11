package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.stub.model.PaymentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Service class for handling payment operations through SOAP web services integration.
 */
@Service
@Slf4j
public class PaymentService {

    private final WebClient webClient;

    public PaymentService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /**
     * Creates a SOAP request XML
     * 
     * @param fiscalCode the fiscal code
     * @param noticeNumber the notice number
     * @return formatted SOAP XML request as a string
     */
    public String createSoapRequest(String fiscalCode, String noticeNumber) {
        return String.format("""
        <?xml version="1.0" encoding="utf-8"?>
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          <soapenv:Body>
            <nfp:verifyPaymentNoticeReq xmlns:nfp="http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd">
              <idPSP>AGID_01</idPSP>
              <idBrokerPSP>97735020584</idBrokerPSP>
              <idChannel>97735020584_03</idChannel>
              <password>pwd_AgID</password>
              <qrCode>
                <fiscalCode>%s</fiscalCode>
                <noticeNumber>%s</noticeNumber>
              </qrCode>
            </nfp:verifyPaymentNoticeReq>
          </soapenv:Body>
        </soapenv:Envelope>
        """, fiscalCode, noticeNumber);
    }

    /**
     * Sends a SOAP request to the PagoPA platform for payment notice verification.
     * 
     * @param fiscalCode the fiscal code
     * @param noticeNumber the payment notice number
     * @return Mono containing the SOAP response as a string
     */
     
    public Mono<String> sendSoapRequest(String fiscalCode, String noticeNumber) {
        String body = createSoapRequest(fiscalCode, noticeNumber);

        return webClient.post()
                .uri("https://api.uat.platform.pagopa.it/nodo/node-for-psp/v1")
                .header("Content-Type", "application/xml")
                .header("SOAPAction", "verifyPaymentNotice")
                .header("Cache-Control", "no-cache")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("[SOAP RESPONSE] SOAP response: {} ",response));
    }

    /**
     * Parses a SOAP response XML to extract payment information.
     * 
     * @param xml the SOAP response XML string to parse
     * @return PaymentInfo object containing extracted payment details
     */
    public PaymentInfo parseSoapResponse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        String amount = getTagContent(document, "amount");
        String dueDate = getTagContent(document, "dueDate");
        String paymentNote = getTagContent(document, "paymentNote");

        return new PaymentInfo(amount, dueDate, paymentNote);
    }

    /**
     * Extracts text content from XML elements by tag name.
     * 
     * @param doc the XML document to search
     * @param tagName the name of the XML tag to find
     * @return text content of the first matching element or "N/A" if not found
     */
    private String getTagContent(Document doc, String tagName) {
        NodeList all = doc.getElementsByTagName(tagName); // <-- rimosso namespace
        return all.getLength() > 0 ? all.item(0).getTextContent() : "N/A";
    }

    /**
     * Reads an HTML template file from the classpath resources.
     * 
     * @param filename the name of the template file to read from resources
     * @return the template content as a UTF-8 string
     */
    public String readTemplateFromResources(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) throw new FileNotFoundException("File non trovato: " + filename);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore durante la lettura del template", e);
        }
    }
}
