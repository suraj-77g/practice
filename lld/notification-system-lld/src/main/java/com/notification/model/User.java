package com.notification.model;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String pushToken;

    public User(String id, String name, String email, String phoneNumber, String pushToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pushToken = pushToken;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPushToken() { return pushToken; }

    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "'}";
    }
}
