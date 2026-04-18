package com.notification;

import com.notification.channel.ChannelFactory;
import com.notification.model.NotificationRequest;
import com.notification.queue.NotificationTaskQueue;
import com.notification.service.NotificationDispatcher;
import com.notification.service.NotificationService;
import com.notification.template.TemplateEngine;
import com.notification.worker.NotificationWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the NotificationSystem.
 * Acts as a Facade to the underlying dispatcher, workers, and queue.
 */
public class DefaultNotificationSystem implements NotificationSystem {
    private final NotificationDispatcher dispatcher;
    private final NotificationTaskQueue taskQueue;
    private final ExecutorService workerPool;
    private final List<NotificationWorker> workers;

    public DefaultNotificationSystem(int numWorkers, int queueCapacity, 
                                   TemplateEngine templateEngine, 
                                   ChannelFactory channelFactory) {
        
        this.taskQueue = new NotificationTaskQueue(queueCapacity);
        this.dispatcher = new NotificationDispatcher(taskQueue);
        
        NotificationService notificationService = new NotificationService(templateEngine, channelFactory);
        
        this.workerPool = Executors.newFixedThreadPool(numWorkers);
        this.workers = new ArrayList<>();
        
        for (int i = 0; i < numWorkers; i++) {
            NotificationWorker worker = new NotificationWorker(taskQueue, notificationService);
            workers.add(worker);
            workerPool.submit(worker);
        }
    }

    @Override
    public void send(NotificationRequest request) {
        dispatcher.dispatch(request);
    }

    @Override
    public void sendBulk(List<NotificationRequest> requests) {
        dispatcher.dispatchBulk(requests);
    }

    @Override
    public void schedule(NotificationRequest request, long delay, TimeUnit unit) {
        dispatcher.schedule(request, delay, unit);
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down Notification System...");
        dispatcher.shutdown();
        for (NotificationWorker worker : workers) {
            worker.stop();
        }
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Notification System terminated.");
    }
}
