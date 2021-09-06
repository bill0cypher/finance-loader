package com.finance.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.*;

@EnableConfigurationProperties
@SpringBootApplication
public class FinanceLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceLoaderApplication.class, args);
    }
}
