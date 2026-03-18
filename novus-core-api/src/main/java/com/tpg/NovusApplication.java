package com.tpg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tpg"})
public class NovusApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovusApplication.class, args);
    }
}
