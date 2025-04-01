package it.gov.pagopa.emd.payment.stub.service;

import it.gov.pagopa.emd.payment.stub.model.PaymentInfo;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
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

    public String generateHtmlResponse(PaymentInfo info) {
        return String.format("""
        <!DOCTYPE html>
        <html lang="it">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>TPP Platform</title>
          <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap" />
          <style>
            body {
              font-family: "Roboto", sans-serif;
              background-color: #f5f5f5;
              margin: 0;
              padding: 0;
            }
            .box {
              max-width: 600px;
              margin: 50px auto;
              background: #fff;
              padding: 30px;
              border-radius: 8px;
              box-shadow: 0 0 10px rgba(0,0,0,0.1);
            }
            h1 {
              text-align: center;
              color: #333;
            }
            p {
              font-size: 18px;
              text-align: center;
            }
            button {
              width: 100%%;
              padding: 12px;
              margin-top: 20px;
              font-size: 16px;
              background-color: #1976d2;
              color: white;
              border: none;
              border-radius: 4px;
              cursor: pointer;
            }
            .modal {
              display: none;
              position: fixed;
              z-index: 1;
              left: 0;
              top: 0;
              width: 100%%;
              height: 100%%;
              overflow: auto;
              background-color: rgba(0, 0, 0, 0.4);
            }
            .modal-content {
              position: relative;
              background-color: #fff;
              margin: 15%% auto;
              padding: 20px;
              border: 1px solid #888;
              width: 80%%;
              max-width: 400px;
              text-align: center;
              border-radius: 6px;
              box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            }
            .close-btn {
              position: absolute;
              top: 10px;
              right: 15px;
              font-size: 20px;
              font-weight: bold;
              color: #aaa;
              cursor: pointer;
            }
            .close-btn:hover {
              color: #000;
            }
          </style>
        </head>
        <body>
          <div class="box">
            <h1>TPP PLATFORM</h1>
            <p><strong>Importo:</strong> €%s</p>
            <p><strong>Data di scadenza:</strong> %s</p>
            <p><strong>Note di pagamento:</strong> %s</p>
            <button onclick="handlePayment()">Paga</button>
          </div>

          <div id="myModal" class="modal">
            <div class="modal-content">
              <span class="close-btn" onclick="closeModal()">&times;</span>
              <p id="modalText">Pagamento in corso...</p>
            </div>
          </div>

          <script>
            function handlePayment() {
              var modal = document.getElementById("myModal");
              var text = document.getElementById("modalText");
              modal.style.display = "block";
              text.innerText = "Pagamento in corso...";
              setTimeout(() => {
                text.innerText = "Pagamento completato!";
                setTimeout(() => {
                  modal.style.display = "none";
                }, 2000);
              }, 2000);
            }

            function closeModal() {
              document.getElementById("myModal").style.display = "none";
            }
          </script>
        </body>
        </html>
        """, info.getAmount(), info.getDueDate(), info.getPaymentNote());
    }
}
