package it.gov.pagopa.emd.payment.stub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactController {
    @GetMapping("/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
