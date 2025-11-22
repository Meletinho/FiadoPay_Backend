package com.fiadopay.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "FiadoPay API", description = "API de pagamentos", version = "v1"))
public class FiadopayBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FiadopayBackendApplication.class, args);
	}

}
