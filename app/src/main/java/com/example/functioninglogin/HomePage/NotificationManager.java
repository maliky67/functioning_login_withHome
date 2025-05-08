package com.example.functioninglogin.HomePage;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static NotificationManager instance;
    private final List<NotificationItem> notifications = new ArrayList<>();
    private int unreadCount = 0;

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addNotification(String title, String message) {
        notifications.add(0, new NotificationItem(title, message));
        unreadCount++;
    }

    public List<NotificationItem> getNotifications() {
        return notifications;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void markAllRead() {
        unreadCount = 0;
    }

    public void clearAll() {
        notifications.clear();
        unreadCount = 0;
    }
}
