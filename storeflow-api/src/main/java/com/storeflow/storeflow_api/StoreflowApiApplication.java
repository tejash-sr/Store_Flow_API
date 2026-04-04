package com.storeflow.storeflow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StoreflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreflowApiApplication.class, args);
	}

}
