package com.maho.simple_network_disk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class StorageConfig {

    @Value("${app.storage.root}")
    private String storageRoot;

    @Bean
    public String storageRoot() {
        return storageRoot;
    }
}