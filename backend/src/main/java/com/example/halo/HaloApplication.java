package com.example.halo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HaloApplication {

  public static void main(String[] args) {
    SpringApplication.run(HaloApplication.class, args);
  }

}
