package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.stub.model.PaymentInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {

    private final WebClient webClient;

    public PaymentService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String createSoapRequest(String fiscalCode, String noticeNumber) {
        return String.format("""
                <?xml version=\"1.0\" encoding=\"utf-8\"?>
                <Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">
                  <Body>
                    <verifyPaymentNoticeReq xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd\">
                      <idPSP>AGID_01</idPSP>
                      <idBrokerPSP>97735020584</idBrokerPSP>
                      <idChannel>97735020584_03</idChannel>
                      <password>pwd_AgID</password>
                      <qrCode>
                        <fiscalCode>%s</fiscalCode>
                        <noticeNumber>%s</noticeNumber>
                      </qrCode>
                    </verifyPaymentNoticeReq>
                  </Body>
                </Envelope>
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
            .bodyToMono(String.class);
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

    public String generateHtmlResponse(PaymentInfo info) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset=\"UTF-8\">
              <title>Dettagli del Pagamento</title>
              <style>
                body { font-family: Arial; background: #f5f5f5; }
                .container { max-width: 600px; margin: 50px auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                h1 { text-align: center; color: #333; }
                p { font-size: 18px; line-height: 1.6; }
              </style>
            </head>
            <body>
              <div class=\"container\">
                <h1>Dettagli del Pagamento</h1>
                <p><strong>Importo:</strong> %s €</p>
                <p><strong>Scadenza:</strong> %s</p>
                <p><strong>Note:</strong> %s</p>
              </div>
            </body>
            </html>
            """, info.getAmount(), info.getDueDate(), info.getPaymentNote());
                }
}
