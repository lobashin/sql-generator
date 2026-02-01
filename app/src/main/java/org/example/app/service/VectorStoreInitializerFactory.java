package org.example.app.service;

import lombok.extern.slf4j.Slf4j;
import org.example.app.service.impl.FileBasedVectorStoreInitializer;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VectorStoreInitializerFactory {

    private final McpClient mcpClient;

    public VectorStoreInitializerFactory(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    /**
     * Создает инициализатор для конкретного файла
     */
    public VectorStoreInitializer createInitializer(
            Resource file,
            String category,
            String initializerType) {

        return new FileBasedVectorStoreInitializer(
                mcpClient, file, category, initializerType
        );
    }
}
