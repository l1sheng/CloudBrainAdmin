package com.neuCloudBrainMedical.admin;

import io.github.cdimascio.dotenv.Dotenv;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//
@SpringBootApplication
@MapperScan("com.neuCloudBrainMedical.admin.mapper")
public class AdminApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		SpringApplication.run(AdminApplication.class, args);
	}

}