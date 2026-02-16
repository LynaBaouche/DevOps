package com.etudlife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EtudlifeApp {
    public static void main(String[] args) {
        SpringApplication.run(EtudlifeApp.class, args) ;
    }
}
