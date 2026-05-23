package com.personalfinance.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PersonalFinanceManagerApplication {

    public static void main(String[] eloquenceArgs) {
        SpringApplication.run(PersonalFinanceManagerApplication.class, eloquenceArgs);
    }
}
