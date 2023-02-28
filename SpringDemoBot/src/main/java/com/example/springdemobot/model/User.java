package com.example.springdemobot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Timestamp;

@Entity(name = "UsersTgBot")
public class User {

    @Id
    private Long chatId;

    private String firstName;

    private String username;

    private Timestamp registeredTime;

    public User() {
    }

    public User(Long chatId, String firstName, String username, Timestamp registeredTime) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.username = username;
        this.registeredTime = registeredTime;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(Timestamp registeredTime) {
        this.registeredTime = registeredTime;
    }
}
