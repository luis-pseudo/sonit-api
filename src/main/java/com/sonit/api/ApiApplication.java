package com.sonit.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(ApiApplication.class, args);
    }

    private static void loadEnv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int index = line.indexOf('=');
                if (index == -1) continue;
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                System.setProperty(key, value);
            }
        } catch (IOException e) {
            System.out.println(".env file not found, relying on system environment variables");
        }
    }
}