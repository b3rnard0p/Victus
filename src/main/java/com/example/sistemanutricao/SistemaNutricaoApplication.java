package com.example.sistemanutricao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SistemaNutricaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SistemaNutricaoApplication.class, args);
    }

}
