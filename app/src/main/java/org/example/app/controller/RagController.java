package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.app.dto.MessageRequest;
import org.example.app.service.rag.RagService;
import org.example.app.service.vectorstore.VectorStoreInitializerFactory;
import org.example.app.service.vectorstore.initialaizer.VectorStoreInitializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RAG API", description = "API для генерации SQL запросов")
public class RagController {

    private final RagService ragService;
    private final VectorStoreInitializerFactory vectorStoreInitializerFactory;
    @Qualifier("datamartStructureFile")
    private final Resource datamartStructureFile;
    @Qualifier("suitableServicesFile")
    private final Resource suitableServicesFile;



    @Operation(
            summary = "Генерация SQL запроса",
            description = "Принимает естественный язык и возвращает SQL запрос"
    )
    @PostMapping("/ai/prompt")
    public String generate(@RequestBody MessageRequest request) {
        return ragService.selectData(request.getMessage());
    }

    @Operation(
            summary = "Инициализация датамарта",
            description = "Загружает структуру датамарта в векторное хранилище"
    )
    @GetMapping("/ai/rag/initial")
    public String initDatamart() {
        try {
            VectorStoreInitializer structureInitializer = vectorStoreInitializerFactory.createInitializer(
                    datamartStructureFile,
                    "datamart_structure",
                    "datamart_structure"
            );
            structureInitializer.initializeFromFile();
            log.info("Инициализация датамарта завершена успешно");
            return "vector_store init: datamart structure loaded";
        } catch (Exception e) {
            log.error("Ошибка инициализации датамарта", e);
            return "ERROR: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Инициализация датамарта",
            description = "Загружает структуру датамарта в векторное хранилище"
    )
    @GetMapping("/ai/rag/initialServices")
    public void initServices() {
        try {
            VectorStoreInitializer structureInitializer = vectorStoreInitializerFactory.createInitializer(
                    suitableServicesFile,
                    "service_structure_category",
                    "service_structure_type"
            );
            structureInitializer.initializeFromFile();
            log.info("Инициализация предложенных сервисов завершена успешно");
        } catch (Exception e) {
            log.error("Ошибка инициализации предложенных сервисов", e);
        }
    }
}
