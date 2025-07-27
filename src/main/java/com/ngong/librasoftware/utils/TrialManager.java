package com.ngong.librasoftware.utils;

import com.ngong.librasoftware.DAO.DatabaseService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TrialManager {

    private static final int TRIAL_DAYS = 1;
    private static boolean notificationShownThisSession = false;

    static DatabaseService db=new DatabaseService();

    public static boolean isTrialExpired() {
        LocalDate trialEnd = getTrialEndDate();
        return LocalDate.now().isAfter(trialEnd);
    }

    public static boolean isTrialActive() {
        return !db.isActivated() && !isTrialExpired();
    }

    private static LocalDate getTrialEndDate() {
        LocalDate launchDate = db.getFirstLaunchDate();
        return launchDate.plusDays(TRIAL_DAYS);
    }


    public static void showTrialNotification() {
        if (notificationShownThisSession) return;  // avoid spamming

        int daysLeft = getRemainingDays();
        String title = "Libra Trial – " + (daysLeft == 0 ? "Expires Today" : daysLeft + " day(s) remaining");
        String message = "Your trial will expire " + (daysLeft == 0 ? "today" : "in " + daysLeft + " day(s)") +
                ". Activate Libra to keep using all features.\nContact joncopy7@gmail.com";
        NotificationUtil.showSystemTrayMessage(title, message);
//        NotificationUtil.playNotificationSound();
    }

    public static int getRemainingDays() {
        LocalDate today = LocalDate.now();
        long elapsed = ChronoUnit.DAYS.between(db.getFirstLaunchDate(), today);
        long remaining = TRIAL_DAYS - elapsed;
        return (int) Math.max(0, remaining);
    }


    public static void scheduleTrialNotifications() {
        if (!isTrialActive()) return;  // don’t show if expired or activated

        showTrialNotification();  // first popup

        // Schedule second popup
        PauseTransition delay = new PauseTransition(Duration.minutes(20));  // ✅ JavaFX-friendly & readable
        delay.setOnFinished(e -> {
            showTrialNotification();  // second popup
            notificationShownThisSession = true;  // cap it
        });
        delay.play();
    }
}

