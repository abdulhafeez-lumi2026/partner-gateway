package com.seera.lumi.partner.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PartnerGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerGatewayApplication.class, args);
    }
}
