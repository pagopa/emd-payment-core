package it.gov.pagopa.emd.payment.stub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling React application routing.
 * <p>
 * This controller provides client-side routing support for React applications by
 * forwarding all non-API requests to the main index.html file.
 */
@Controller
public class ReactController {

    /**
     * Redirects all non-static resource requests to the React application's index.html.
     * 
     * @return a forward directive to index.html for React application handling
     */
    @GetMapping("/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}
