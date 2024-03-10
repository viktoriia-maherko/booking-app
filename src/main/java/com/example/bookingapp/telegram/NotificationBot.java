package com.example.bookingapp.telegram;

import com.example.bookingapp.config.BotConfig;
import com.example.bookingapp.model.User;
import com.example.bookingapp.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
@RequiredArgsConstructor
public class NotificationBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final UserRepository userRepository;
    private final Map<Long, AuthenticationContext> authenticationContextMap = new HashMap<>();
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (authenticationContextMap.containsKey(chatId)) {
                handleAuthentication(chatId, messageText, update);
            } else {
                handleIncomingMessage(chatId, messageText, update);
            }
        }
    }

    private void handleIncomingMessage(Long chatId, String messageText, Update update) {
        switch (messageText) {
            case "/start":
                handleStartCommand(update);
                break;
            case "/authenticate":
                startAuthentication(chatId);
                break;
            default:
                sendNotification("Unknown command", chatId);
        }
    }

    public void sendNotification(String message, Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Can't send message", e);
        }
    }

    private void handleStartCommand(Update update) {
        Long chatId = update.getMessage().getChatId();
        sendNotification("Hello! I'm bot for sending notification about your booking.", chatId);
    }

    private void startAuthentication(long chatId) {
        if (userRepository.existsUserByChatId(chatId)) {
            sendNotification("You are already authenticated", chatId);
            return;
        }
        sendNotification("Please enter your email to start the authentication process:", chatId);
        AuthenticationContext context = new AuthenticationContext();
        authenticationContextMap.put(chatId, context);
    }

    private void handleAuthentication(Long chatId, String messageText, Update update) {
        AuthenticationContext context = authenticationContextMap.get(chatId);
        if (context.getEmail() == null) {
            context.setEmail(messageText);
            sendNotification("Email received. Now, please enter your password:", chatId);
        } else if (context.getPassword() == null) {
            context.setPassword(messageText);
            if (userRepository.existsUserByEmail(context.email)) {
                User user = userRepository.findByEmail(context.email).get();
                String storedEncodedPassword = user.getPassword();

                if (passwordEncoder.matches(context.password, storedEncodedPassword)) {
                    user.setChatId(chatId);
                    userRepository.save(user);
                    sendNotification("You have successfully authenticated", chatId);
                } else {
                    sendNotification("Authentication failed!", chatId);
                }
            } else {
                sendNotification("Authentication failed!", chatId);
            }
            authenticationContextMap.remove(chatId);
        }
    }

    @Getter
    @Setter
    private static class AuthenticationContext {
        private String email;
        private String password;
    }
}
