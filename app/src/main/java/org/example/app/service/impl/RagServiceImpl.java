package org.example.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.app.repository.DataSelectorRepository;
import org.example.app.restclient.McpClient;
import org.example.app.service.RagService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final McpClient mcpClient;
    private final DataSelectorRepository dataSelectorRepository;

    public String selectData(String message) {
        String sql = mcpClient.requestResolveSql(message);
        return dataSelectorRepository.findDataFromRepository(sql);
    }
}
