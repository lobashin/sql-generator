package org.example.app.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.app.dto.MessageRequest;
import org.example.app.service.RagService;
import org.example.app.service.VectorStoreInitializer;
import org.example.app.service.VectorStoreInitializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "RAG API", description = "API для генерации SQL запросов")
public class RagController {

    public static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;
    private final VectorStoreInitializerFactory vectorStoreInitializerFactory;
    private final Resource datamartStructureFile;

    public RagController(
            RagService ragService,
            VectorStoreInitializerFactory vectorStoreInitializerFactory,
            List<McpSyncClient> syncClientList,
            @Qualifier("datamartStructureFile") Resource datamartStructureFile) {
        this.ragService = ragService;
        this.vectorStoreInitializerFactory = vectorStoreInitializerFactory;
        this.datamartStructureFile = datamartStructureFile;
    }

    @Operation(
            description = "Принимает естественный язык и возвращает результат выполнения сгенерированного SQL запроса")
    @PostMapping("/ai/prompt")
    public String generate(@RequestBody MessageRequest request) {
        return ragService.selectData(request.getMessage());
    }

    @Operation(description = "Загружает структуру датамарта в векторное хранилище")
    @GetMapping("/ai/rag/initial")
    public String initDatamart() {
        try {
            VectorStoreInitializer structureInitializer = vectorStoreInitializerFactory.createInitializer(
                    datamartStructureFile, "datamart_structure", "datamart_structure");
            structureInitializer.initializeFromFile();
            log.info("Инициализация датамарта завершена успешно");
            return "vector_store init: datamart structure loaded";
        } catch (Exception e) {
            log.error("Ошибка инициализации датамарта", e);
            return "ERROR: " + e.getMessage();
        }
    }
}
