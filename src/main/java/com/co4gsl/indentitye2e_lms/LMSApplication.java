package com.co4gsl.indentitye2e_lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LMSApplication {

	public static void main(String[] args) {
		SpringApplication.run(LMSApplication.class, args);
	}
}
