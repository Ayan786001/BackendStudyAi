package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendStudyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendStudyAiApplication.class, args);
        System.out.println(System.getenv("OPEN_API_KEY"));
    }

}
