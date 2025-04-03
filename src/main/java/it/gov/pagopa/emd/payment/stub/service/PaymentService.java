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

@Service
@Slf4j
public class PaymentService {

    private final WebClient webClient;

    public PaymentService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

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

    private String getTagContent(Document doc, String tagName) {
        NodeList all = doc.getElementsByTagName(tagName); // <-- rimosso namespace
        return all.getLength() > 0 ? all.item(0).getTextContent() : "N/A";
    }

    public String readTemplateFromResources(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is == null) throw new FileNotFoundException("File non trovato: " + filename);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Errore durante la lettura del template", e);
        }
    }
}
