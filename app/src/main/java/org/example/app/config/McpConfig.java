package org.example.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class McpConfig {

    @Bean("datamartStructureFile")
    public Resource datamartStructureFile() {
        return new ClassPathResource(
                "vector-store-info/mcp-init-with-datamart-structure.txt"
        );
    }

    @Bean("suitableServicesFile")
    public Resource suitableServicesFile() {
        return new ClassPathResource(
                "vector-store-info/suitable-services.txt"
        );
    }
}
