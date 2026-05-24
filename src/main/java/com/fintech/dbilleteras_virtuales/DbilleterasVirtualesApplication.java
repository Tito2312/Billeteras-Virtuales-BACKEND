package com.fintech.dbilleteras_virtuales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DbilleterasVirtualesApplication {

	 public static void main(String[] args) {
        SpringApplication.run(DbilleterasVirtualesApplication.class, args);
        System.out.println("🚀 Aplicación iniciada con scheduling habilitado");
    }

}
