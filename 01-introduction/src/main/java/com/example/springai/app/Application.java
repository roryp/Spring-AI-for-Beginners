package com.example.springai.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Entry point for the Getting Started module.  This simple Spring Boot
 * application exposes a chat endpoint backed by Microsoft Foundry via Spring AI.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.springai")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}