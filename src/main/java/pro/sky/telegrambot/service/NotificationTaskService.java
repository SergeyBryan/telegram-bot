package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTaskEntity;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationTaskService {
    NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public List<NotificationTaskEntity> find(LocalDateTime localDateTime) {
        return notificationTaskRepository.findAllByDate(localDateTime);
    }

    public void save(NotificationTaskEntity notificationTask) {
        notificationTaskRepository.save(notificationTask);
    }

}
