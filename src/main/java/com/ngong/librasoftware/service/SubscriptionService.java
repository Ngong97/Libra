package com.ngong.librasoftware.service;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.SubscriptionBrowserLauncher;
import com.ngong.librasoftware.utils.SubscriptionNotification;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class SubscriptionService {

    private final DatabaseService db;

    public SubscriptionService(DatabaseService db) {
        this.db = db;
    }

    public void startRepeatingNotification(Stage ownerStage) {
        showOnce(ownerStage); // show immediately on launch

        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(0.5), e -> showOnce(ownerStage)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void showOnce(Stage ownerStage) {
        if (!db.isUserSubscribed()) {
            String schoolName = db.getCurrentUserSchool();
            SubscriptionNotification.show(ownerStage, () -> {
                SubscriptionBrowserLauncher.launchEmail(schoolName);
                db.setUserSubscribed(true);
            });
        }
    }
}


