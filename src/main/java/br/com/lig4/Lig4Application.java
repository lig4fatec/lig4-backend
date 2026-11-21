package br.com.lig4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Lig4Application {

    public static void main(String[] args) {
        SpringApplication.run(Lig4Application.class, args);
    }
}