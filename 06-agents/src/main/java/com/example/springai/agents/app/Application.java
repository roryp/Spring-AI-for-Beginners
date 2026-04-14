package com.example.springai.agents.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.springai.agents")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
