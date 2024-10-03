package org.p0.calendly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CalendlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalendlyApplication.class, args);
        System.out.println("Hello");
    }

}
