package it.gov.pagopa.emd.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
@EnableFeignClients(basePackages = "it.gov.pagopa.emd.payment.connector")
public class EmdPaymentCore {

	public static void main(String[] args) {
		SpringApplication.run(EmdPaymentCore.class, args);
	}

}
