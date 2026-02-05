package org.example.app.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.app.exception.UnsupportedSqlCommandException;
import org.example.app.repository.DataSelectorRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class DataSelectorRepositoryImpl implements DataSelectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public DataSelectorRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String findDataFromRepository(String sql) {
        try {
            log.info(">> Ответ от MCP {}:", sql);
            String trimmedSql = sql.trim().toLowerCase();
            if (trimmedSql.startsWith("select")) {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                return convertToJson(result);
            } else {
                throw new UnsupportedSqlCommandException("Неподдерживаемая SQL операция");
            }
        } catch (Exception e) {
            log.warn("Ошибка выполнения SQL запроса: " + e.getMessage(), e);
            return e.getMessage();
        }
    }

    private String convertToJson(List<Map<String, Object>> result) {
        if (result.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (Map<String, Object> row : result) {
            json.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(entry.getKey()).append("\":");

                Object value = entry.getValue();
                if (value == null) {
                    json.append("null");
                } else if (value instanceof Number) {
                    json.append(value);
                } else {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                }
                first = false;
            }
            json.append("},");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]");

        return json.toString();
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
