package com.example.assignment.fuel.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class FuelCalculatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuelCalculatorApplication.class,args);
    }
}
