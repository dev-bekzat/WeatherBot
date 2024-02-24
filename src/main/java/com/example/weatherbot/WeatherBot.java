package com.example.weatherbot;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@SpringBootApplication
public class WeatherBot extends TelegramLongPollingBot {

    public String botUsername;
    public String botToken;

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    private String extractCityName(String messageText) {
        return messageText.trim();
    }

    private String getWeatherInfo(String cityName) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String apiKey = "506a6350d6c7419b7776dc3d5fbdc478";
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        try {
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            System.out.println("JSON Response: " + jsonResponse);

            Gson gson = new Gson();

            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            double temperatureCelcius = temperatureKelvin - 273.15;

            String formattedTemperature = String.format("%.1f", temperatureCelcius);
            String sign = temperatureCelcius >= 0 ? "+" : "";

            double humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsDouble();
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

            String weatherInfo = "Температура: " + sign + formattedTemperature + "C\n" +
                                 "Влажность: " + humidity + "%\n" +
                                 "Скорость ветра: " + windSpeed + "m/c";

            return weatherInfo;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error occurred while getting weather info: " + e.getMessage());
            return "Такого города не существует";
        }
    }

    private void sendResponse(Long chatId, String response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        if (message != null && message.hasText()) {
            // Получаем текст сообщения от пользователя
            String messageText = message.getText();

            System.out.println("Received message: " + messageText);

            // Извлекаем название города из сообщения пользователя
            String cityName = extractCityName(messageText);

            System.out.println("Extracted city name: " + cityName);

            // Отправляем запрос на OpenWeather API для получения информации о погоде
            String weatherInfo = getWeatherInfo(cityName);

            System.out.println("Weather info: " + weatherInfo);

            // Отправляем ответ пользователю с информацией о погоде
            sendResponse(message.getChatId(), weatherInfo);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherBot.class, args);
    }
}
