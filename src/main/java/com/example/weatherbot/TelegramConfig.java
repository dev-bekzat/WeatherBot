package com.example.weatherbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

        WeatherBot weatherBot = new WeatherBot();
        weatherBot.setBotUsername(botUsername);
        weatherBot.setBotToken(botToken);

        try {
            telegramBotsApi.registerBot(weatherBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return telegramBotsApi;
    }
}
