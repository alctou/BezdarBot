package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeepSeekChat {
    private static final String API_KEY = "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";
    private static final String MODEL = "deepseek/deepseek-r1";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public String generateTarotReading(String cardList, String selectedTarotType) {
        if (cardList.isEmpty()) {
            return "Ошибка: Сначала выберите расклад и вытяните карты.";
        }

        String tarotType = selectedTarotType.isEmpty() ? "Общий расклад" : selectedTarotType;
        String cards = String.join(", ", cardList);
        String prompt = "Ты опытный таролог. Проанализируй расклад '" + tarotType + "' и связи между картами.\n"
                + "Карты: " + cards + "\n"
                + "Не пересказывай значения карт, а дай их интерпретацию. Разделяй мысли пустой строкой. "
                + "Добавь немного эмодзи. Заверши конкретикой и вопросами для размышления.";


        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("model", MODEL);
            data.put("max_tokens", 2134);
            data.put("stream", false);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            data.put("messages", messages);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("API Response: " + response.toString());

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (!choices.isEmpty()) {
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    return message.getString("content")
                            .replaceAll("\\*+(.*?)\\*+", "<b>$1</b>")  // заменяет любые '*' на <b>...</b>
                            .replaceAll("_+(.*?)_+", "<i>$1</i>");     // заменяет любое количество '_' на '__'
                }
            }
        } catch (Exception e) {
            return "Ошибка: Не удалось получить ответ от OpenRouter API. " + e.getMessage();
        }
        return "Ошибка: OpenRouter не вернул ответ.";
    }
}
