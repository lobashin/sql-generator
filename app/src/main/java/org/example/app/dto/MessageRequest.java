package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для генерации SQL")
public class MessageRequest {

    @Schema(
            description = "Сообщение на естественном языке",
            example = "Покажи всех пользователей из Москвы")
    private String message;
}
