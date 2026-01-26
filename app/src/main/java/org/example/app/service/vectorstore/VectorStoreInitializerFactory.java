package org.example.app.service.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.example.app.restclient.McpClient;
import org.example.app.service.vectorstore.initialaizer.VectorStoreInitializer;
import org.example.app.service.vectorstore.initialaizer.impl.FileBasedVectorStoreInitializer;
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
