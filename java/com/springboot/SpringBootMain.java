package com.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.SpringVersion;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableAsync
@SpringBootApplication /* (exclude = {SecurityAutoConfiguration.class }) */
public class SpringBootMain {

	
	public static void main(String[] args) {

        System.out.println("version: " + SpringVersion.getVersion());
		SpringApplication.run(SpringBootMain.class, args);
	}
	

}
