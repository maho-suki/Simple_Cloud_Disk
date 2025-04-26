package com.maho.simple_network_disk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class SimpleNetworkDiskApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleNetworkDiskApplication.class, args);
    }

    @Bean
    CommandLineRunner initStorage(@Value("${app.storage.root}") String storagePath) {
        return args -> {
            Path root = Paths.get(storagePath);
            if (!Files.exists(root)) {
                Files.createDirectories(root);
                System.out.println("创建存储目录: " + root.toAbsolutePath());
            }
        };
    }
}
