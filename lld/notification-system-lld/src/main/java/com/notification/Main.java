package com.notification;

import com.notification.channel.ChannelFactory;
import com.notification.model.NotificationRequest;
import com.notification.model.NotificationType;
import com.notification.model.User;
import com.notification.queue.NotificationTaskQueue;
import com.notification.service.NotificationDispatcher;
import com.notification.service.NotificationService;
import com.notification.template.SimpleTemplateEngine;
import com.notification.template.TemplateEngine;
import com.notification.worker.NotificationWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // 1. Initialize Components
        TemplateEngine templateEngine = new SimpleTemplateEngine();
        templateEngine.addTemplate("welcome", "Hello {{name}}, welcome to our platform!");
        templateEngine.addTemplate("promo", "Hi {{name}}, here is a 20% discount for your next purchase: {{code}}");
        templateEngine.addTemplate("alert", "Urgent Alert: {{message}}");

        ChannelFactory channelFactory = new ChannelFactory();
        
        // Use the new Facade
        NotificationSystem notificationSystem = new DefaultNotificationSystem(
                3,               // numWorkers
                100,             // queueCapacity
                templateEngine, 
                channelFactory
        );

        // 3. Create Sample Users
        User alice = new User("1", "Alice", "alice@example.com", "1234567890", "token_alice");
        User bob = new User("2", "Bob", "bob@example.com", "0987654321", "token_bob");

        // 4. Demonstrate Immediate Single Notification
        System.out.println("--- Sending Single Notification ---");
        Map<String, Object> welcomeData = new HashMap<>();
        welcomeData.put("name", alice.getName());
        notificationSystem.send(new NotificationRequest(alice, NotificationType.EMAIL, "welcome", welcomeData));

        // 5. Demonstrate Bulk Notifications
        System.out.println("--- Sending Bulk Notifications ---");
        List<NotificationRequest> bulkRequests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> promoData = new HashMap<>();
            promoData.put("name", "User" + i);
            promoData.put("code", "SAVE20-" + i);
            bulkRequests.add(new NotificationRequest(
                    new User(String.valueOf(i), "User" + i, "user" + i + "@example.com", "phone" + i, "token" + i),
                    NotificationType.SMS,
                    "promo",
                    promoData
            ));
        }
        notificationSystem.sendBulk(bulkRequests);

        // 6. Demonstrate Scheduled Notification
        System.out.println("--- Scheduling Notification (3 seconds delay) ---");
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("message", "Your account was logged in from a new device.");
        notificationSystem.schedule(new NotificationRequest(bob, NotificationType.PUSH, "alert", alertData), 3, TimeUnit.SECONDS);

        // 7. Wait and Shutdown
        System.out.println("Waiting for all tasks to complete...");
        TimeUnit.SECONDS.sleep(5);

        notificationSystem.shutdown();
    }
}
