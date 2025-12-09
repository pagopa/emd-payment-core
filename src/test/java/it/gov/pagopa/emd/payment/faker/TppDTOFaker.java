package it.gov.pagopa.emd.payment.faker;


import it.gov.pagopa.emd.payment.dto.AgentLink;
import it.gov.pagopa.emd.payment.dto.Contact;
import it.gov.pagopa.emd.payment.dto.TppDTO;
import it.gov.pagopa.emd.payment.dto.VersionDetails;
import it.gov.pagopa.emd.payment.enums.AuthenticationType;

import java.util.HashMap;

public class TppDTOFaker {
    public static TppDTO mockInstance() {

        Contact contact = new Contact("name","number", "email");
        VersionDetails versionDetails = new VersionDetails("1.0.0");
        AgentLink agentLink = new AgentLink("ios", new HashMap<>() {{
            put("v1", versionDetails);
        }});

        return TppDTO.builder()
                .tppId("tppId")
                .messageUrl("https://wwwmessageUrl.it")
                .authenticationUrl("https://www.AuthenticationUrl.it")
                .idPsp("idPsp")
                .legalAddress("legalAddress")
                .authenticationType(AuthenticationType.OAUTH2)
                .entityId("entityId01234567")
                .businessName("businessName")
                .contact(contact)
                .lastUpdateDate(null)
                .creationDate(null)
                .pspDenomination("#button")
                .agentDeepLinks(new HashMap<>() {{
                    put("agent", "link");
                }})
                .agentLinks(new HashMap<>() {{
                    put("agent", agentLink);
                }})
                .isPaymentEnabled(Boolean.TRUE)
                .build();
    }

}
