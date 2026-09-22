package com.bassstrings.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BassStringsHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BassStringsHubApplication.class, args);
    }
}
