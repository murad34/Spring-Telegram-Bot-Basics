package com.example.springdemobot.service;

import com.example.springdemobot.config.BotConfig;
import com.example.springdemobot.model.Ads;
import com.example.springdemobot.model.AdsRepository;
import com.example.springdemobot.model.User;
import com.example.springdemobot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdsRepository adsRepository;

    final BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

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

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send") && chatId == botConfig.getOwnerId()) {

                String textToSend = messageText.substring(messageText.indexOf(" "));
                Iterable<User> allUsers = userRepository.findAll();

                for (User user : allUsers) {
                    if (user.getChatId() != botConfig.getOwnerId()) {
                        prepareAndSendMessage(user.getChatId(), textToSend);
                    }
                }

            } else {

                switch (messageText) {

                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceive(chatId, update.getMessage().getChat().getFirstName());
                        break;

                    case "/help":
                        prepareAndSendMessage(chatId, "This Bot is created to demonstrate this business. \n \n" +
                                "You can execute commands from the main menu on the left or by typing a command : \n \n" +
                                "Type /start for start this Bot \n" +
                                "Type /help for start this Bot");
                        break;

                    case "Register":
                        register(chatId);
                        break;

                    default:
                        prepareAndSendMessage(chatId, "Sorry, ERROR !");

                }

            }

        } else if (update.hasCallbackQuery()) {

            String callBackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals("yes_button")) {

                executeEditMessageText("You pressed YES button", chatId, messageId);

            } else if (callBackData.equals("no_button")) {

                executeEditMessageText("You pressed NO button", chatId, messageId);

            }

        }

    }

    private void executeEditMessageText(String text, long chatId, long messageId) {

        EditMessageText editMessageText = new EditMessageText(text);
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId((int) messageId);

        executeMessage(editMessageText);

    }

    private void registerUser(Message message) {

        if (userRepository.findById(message.getChatId()).isEmpty()) {

            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User(chatId, chat.getFirstName(), chat.getUserName(), new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);

        }

    }

    private void register(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Are You ready to change your life ?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("yes_button");

        InlineKeyboardButton noButton = new InlineKeyboardButton("No");
        noButton.setCallbackData("no_button");

        inlineRow.add(yesButton);
        inlineRow.add(noButton);

        inlineRows.add(inlineRow);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        executeMessage(sendMessage);

    }

    public void startCommandReceive(long chatId, String name) {

        String answer = "Hi, " + name + ", nice to meet you !";

        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String textToSend) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Info");
        row.add("Data");
        row.add("Settings");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("Log in");
        row.add("Register");
        row.add("Log out");
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        executeMessage(sendMessage);

    }

    private void executeMessage(BotApiMethod sendMessage) {

        try {
            execute(sendMessage);
        } catch (TelegramApiException telegramApiException) {

        }

    }

    private void prepareAndSendMessage(long chatId, String textToSend) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        executeMessage(sendMessage);

    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds() {

        Iterable<Ads> ads = adsRepository.findAll();
        Iterable<User> users = userRepository.findAll();

        for (Ads advertising : ads) {
            for (User user : users) {
                prepareAndSendMessage(user.getChatId(),advertising.getReklama());
            }
        }

    }

}
