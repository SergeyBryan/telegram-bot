package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTaskEntity;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

    private final Pattern pattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s+([А-я\\d\\s.,!?:]+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                Message message = update.message();
                String text = message.text();
                long chatId = update.message().chat().id();
                if ("/start".equals(text)) {
                    sendMessage(chatId, "Привет, пришли информацию об напоминании в формате: 01.01.2023 20:00 Сделать домашнюю работу");
                } else if (text != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        LocalDateTime localDateTime = parser(matcher.group(1));
                        if (Objects.isNull(localDateTime)) {
                            sendMessage(chatId, "Проверьте правильность даты напоминания");
                        } else {
                            String txt = matcher.group(2);
                            NotificationTaskEntity notificationTask = new NotificationTaskEntity();
                            notificationTask.setChatId(chatId);
                            notificationTask.setMessage(txt);
                            notificationTask.setDate(localDateTime);
                            notificationTaskService.save(notificationTask);
                            sendMessage(chatId, "Информация сохранена");
                        }
                    } else {
                        sendMessage(chatId, "Проверье правильность написания напоминания, отправьте в формате: 01.01.2023 20:00 Сделать домашнюю");
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parser(String text) {
        try {
            return LocalDateTime.parse(text, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sendMessage(long id, String message) {
        SendMessage sendMessage = new SendMessage(id, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("При отправке сообщения произошла ошибка {}", sendResponse.description());
        }
    }
}
