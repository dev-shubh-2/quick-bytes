package dev.danvega.qb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class QuickBytesApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuickBytesApplication.class, args);
	}

}
