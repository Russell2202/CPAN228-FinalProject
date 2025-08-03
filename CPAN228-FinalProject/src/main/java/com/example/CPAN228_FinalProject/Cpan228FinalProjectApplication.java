package com.example.CPAN228_FinalProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.CPAN228_FinalProject.repository")
@EntityScan(basePackages = "com.example.CPAN228_FinalProject.model")
@ComponentScan(basePackages = "com.example.CPAN228_FinalProject")

public class Cpan228FinalProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(Cpan228FinalProjectApplication.class, args);
	}

}
