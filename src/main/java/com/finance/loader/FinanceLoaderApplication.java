package com.finance.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;

@ConfigurationPropertiesScan(basePackages = {"com.finance.loader.props"})
@SpringBootApplication(exclude = ContextStackAutoConfiguration.class)
public class FinanceLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceLoaderApplication.class, args);
    }
}
