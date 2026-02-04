package org.example.app.service.impl;

import org.example.app.repository.DataSelectorRepository;
import org.example.app.service.McpClient;
import org.example.app.service.RagService;
import org.springframework.stereotype.Service;

@Service
public class RagServiceImpl implements RagService {

    private final McpClient mcpClient;
    private final DataSelectorRepository dataSelectorRepository;

    public RagServiceImpl(McpClient mcpClient, DataSelectorRepository dataSelectorRepository) {
        this.mcpClient = mcpClient;
        this.dataSelectorRepository = dataSelectorRepository;
    }

    @Override
    public String selectData(String message) {
        String sql = mcpClient.requestResolveSql(message);
        String data = dataSelectorRepository.findDataFromRepository(sql);
        return String.format("получены данные:\n%s\n\nпо сгенерированному sql:\n%s", data, sql);
    }
}
