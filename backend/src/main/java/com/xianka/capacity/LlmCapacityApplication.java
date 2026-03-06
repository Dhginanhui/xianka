package com.xianka.capacity;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LlmCapacityApplication {

    public static void main(String[] args) {
        System.out.println("Starting LLM Capacity Estimator...");
        try {
            System.out.println("Attempting to load .env file...");
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            int count = 0;
            for (io.github.cdimascio.dotenv.DotenvEntry entry : dotenv.entries()) {
                System.setProperty(entry.getKey(), entry.getValue());
                count++;
            }
            System.out.println("Loaded " + count + " entries from .env file.");
            
            String apiKey = System.getProperty("LLM_API_KEY");
            if (apiKey != null && !apiKey.isEmpty()) {
                System.out.println("LLM_API_KEY found in environment (length: " + apiKey.length() + ")");
            } else {
                System.out.println("WARNING: LLM_API_KEY not found in .env file!");
            }
        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
        SpringApplication.run(LlmCapacityApplication.class, args);
    }
}
