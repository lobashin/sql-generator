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

    @Operation(description = "Возвращает HTML-страницу с формой для общения с LLM через не потоковый эндпоинт")
    @GetMapping("/ai/chat")
    public String chatPage() {
        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Чат с LLM</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .chat-container {
                        background-color: white;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        padding: 20px;
                    }
                    h1 {
                        color: #333;
                        text-align: center;
                    }
                    #chat-output {
                        height: 400px;
                        overflow-y: auto;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        padding: 15px;
                        margin-bottom: 20px;
                        background-color: #fafafa;
                    }
                    .message {
                        margin-bottom: 10px;
                        padding: 10px;
                        border-radius: 5px;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    }
                    .user-message {
                        background-color: #e3f2fd;
                        text-align: right;
                    }
                    .ai-message {
                        background-color: #f1f8e9;
                        text-align: left;
                    }
                    .message pre {
                        background-color: #f8f8f8;
                        padding: 10px;
                        border-radius: 5px;
                        overflow-x: auto;
                        margin: 5px 0;
                    }
                    .message code {
                        font-family: 'Courier New', monospace;
                        background-color: #f8f8f8;
                        padding: 2px 4px;
                        border-radius: 3px;
                    }
                    #message-form {
                        display: flex;
                        gap: 10px;
                    }
                    #message-input {
                        flex-grow: 1;
                        padding: 10px;
                        border: 1px solid #ddd;
                        border-radius: 5px;
                        font-size: 16px;
                    }
                    button {
                        padding: 10px 20px;
                        background-color: #4CAF50;
                        color: white;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                        font-size: 16px;
                    }
                    button:hover {
                        background-color: #45a049;
                    }
                    button:disabled {
                        background-color: #cccccc;
                        cursor: not-allowed;
                    }
                    .loading {
                        color: #666;
                        font-style: italic;
                    }
                    .typing-indicator {
                        display: inline-block;
                        margin-left: 5px;
                    }
                    .typing-indicator span {
                        display: inline-block;
                        width: 8px;
                        height: 8px;
                        background-color: #888;
                        border-radius: 50%;
                        margin: 0 2px;
                        animation: typing 1.4s infinite both;
                    }
                    .typing-indicator span:nth-child(2) {
                        animation-delay: 0.2s;
                    }
                    .typing-indicator span:nth-child(3) {
                        animation-delay: 0.4s;
                    }
                    @keyframes typing {
                        0%, 60%, 100% { transform: translateY(0); }
                        30% { transform: translateY(-5px); }
                    }
                </style>
            </head>
            <body>
                <div class="chat-container">
                    <h1>Чат с LLM</h1>
                    <div id="chat-output"></div>
                    <form id="message-form">
                        <input type="text" id="message-input" placeholder="Введите ваш запрос... (используйте стрелки вверх/вниз для навигации по истории)" autocomplete="off">
                        <button type="submit" id="send-button">Отправить</button>
                    </form>
                </div>

                <script>
                    const chatOutput = document.getElementById('chat-output');
                    const messageForm = document.getElementById('message-form');
                    const messageInput = document.getElementById('message-input');
                    const sendButton = document.getElementById('send-button');
                    
                    // Ключ для localStorage
                    const HISTORY_KEY = 'llm_chat_history';
                    const MAX_HISTORY_ITEMS = 50;
                    
                    // Текущий индекс в истории для навигации
                    let historyIndex = -1;
                    let history = [];
                    
                    // Загрузка истории из localStorage
                    function loadHistory() {
                        try {
                            const saved = localStorage.getItem(HISTORY_KEY);
                            if (saved) {
                                history = JSON.parse(saved);
                            }
                        } catch (e) {
                            console.error('Ошибка загрузки истории:', e);
                            history = [];
                        }
                    }
                    
                    // Сохранение истории в localStorage
                    function saveHistory() {
                        try {
                            localStorage.setItem(HISTORY_KEY, JSON.stringify(history));
                        } catch (e) {
                            console.error('Ошибка сохранения истории:', e);
                        }
                    }
                    
                    // Добавление запроса в историю
                    function addToHistory(query) {
                        if (!query || query.trim() === '') {
                            return;
                        }
                        
                        // Удаляем дубликаты
                        history = history.filter(item => item !== query);
                        
                        // Добавляем в начало
                        history.unshift(query);
                        
                        // Ограничиваем размер истории
                        if (history.length > MAX_HISTORY_ITEMS) {
                            history = history.slice(0, MAX_HISTORY_ITEMS);
                        }
                        
                        saveHistory();
                    }
                    
                    // Навигация по истории с помощью клавиш вверх/вниз
                    function navigateHistory(direction) {
                        if (history.length === 0) {
                            return;
                        }
                        
                        if (direction === 'up') {
                            // Стрелка вверх - предыдущий запрос
                            if (historyIndex < history.length - 1) {
                                historyIndex++;
                                messageInput.value = history[historyIndex];
                            } else if (historyIndex === -1 && history.length > 0) {
                                // Если только начали навигацию
                                historyIndex = 0;
                                messageInput.value = history[0];
                            }
                        } else if (direction === 'down') {
                            // Стрелка вниз - следующий запрос
                            if (historyIndex > 0) {
                                historyIndex--;
                                messageInput.value = history[historyIndex];
                            } else if (historyIndex === 0) {
                                // Вернуться к пустому полю ввода
                                historyIndex = -1;
                                messageInput.value = '';
                            }
                        }
                    }
                    
                    // Функция для добавления сообщения в чат
                    function addMessage(text, isUser = false) {
                        const messageDiv = document.createElement('div');
                        messageDiv.className = `message ${isUser ? 'user-message' : 'ai-message'}`;
                        
                        // Форматирование текста: выделяем SQL код
                        const formattedText = text.replace(/```sql\\n([\\s\\S]*?)```/g, '<pre><code>$1</code></pre>')
                                                 .replace(/```([\\s\\S]*?)```/g, '<pre><code>$1</code></pre>')
                                                 .replace(/\\n/g, '<br>');
                        
                        messageDiv.innerHTML = formattedText;
                        chatOutput.appendChild(messageDiv);
                        chatOutput.scrollTop = chatOutput.scrollHeight;
                    }

                    // Функция для создания индикатора набора текста
                    function createTypingIndicator() {
                        const indicatorDiv = document.createElement('div');
                        indicatorDiv.className = 'message ai-message';
                        indicatorDiv.id = 'typing-indicator';
                        indicatorDiv.innerHTML = 'LLM думает<span class="typing-indicator"><span></span><span></span><span></span></span>';
                        return indicatorDiv;
                    }

                    // Функция для отправки запроса на не потоковый эндпоинт
                    async function sendRequest(message) {
                        addMessage(message, true);
                        
                        // Добавляем запрос в историю
                        addToHistory(message);
                        
                        // Добавляем индикатор набора текста
                        const typingIndicator = createTypingIndicator();
                        chatOutput.appendChild(typingIndicator);
                        chatOutput.scrollTop = chatOutput.scrollHeight;
                        
                        // Очищаем поле ввода
                        messageInput.value = '';
                        sendButton.disabled = true;
                        historyIndex = -1; // Сбрасываем индекс истории
                        
                        try {
                            // Отправляем POST-запрос на /ai/prompt
                            const response = await fetch('/ai/prompt', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Accept': 'application/json'
                                },
                                body: JSON.stringify({ message: message })
                            });
                            
                            if (!response.ok) {
                                throw new Error(`HTTP error! status: ${response.status}`);
                            }
                            
                            const responseText = await response.text();
                            
                            // Удаляем индикатор набора текста
                            chatOutput.removeChild(typingIndicator);
                            
                            // Добавляем ответ от LLM
                            addMessage(responseText, false);
                            
                        } catch (error) {
                            console.error('Ошибка:', error);
                            chatOutput.removeChild(typingIndicator);
                            addMessage('Ошибка при получении ответа от сервера: ' + error.message, false);
                        } finally {
                            sendButton.disabled = false;
                            messageInput.focus();
                        }
                    }

                    // Обработчик отправки формы
                    messageForm.addEventListener('submit', async (e) => {
                        e.preventDefault();
                        
                        const message = messageInput.value.trim();
                        if (!message) {
                            return;
                        }
                        
                        await sendRequest(message);
                    });

                    // Обработчик нажатия Enter в поле ввода
                    messageInput.addEventListener('keydown', (e) => {
                        if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            messageForm.dispatchEvent(new Event('submit'));
                        }
                        
                        // Навигация по истории с помощью стрелок вверх/вниз
                        if (e.key === 'ArrowUp') {
                            e.preventDefault();
                            navigateHistory('up');
                        } else if (e.key === 'ArrowDown') {
                            e.preventDefault();
                            navigateHistory('down');
                        }
                    });

                    // Фокус на поле ввода при загрузке
                    messageInput.focus();
                    
                    // Загружаем историю при загрузке страницы
                    loadHistory();
                    
                    // Добавляем приветственное сообщение
                    addMessage('Привет! Я LLM, готовый помочь вам с генерацией SQL запросов. Задайте ваш вопрос на естественном языке.', false);
                </script>
            </body>
            </html>
            """;
    }
}
