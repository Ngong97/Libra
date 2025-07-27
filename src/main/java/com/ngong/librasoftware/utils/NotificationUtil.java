package com.ngong.librasoftware.utils;

import com.ngong.librasoftware.DAO.DatabaseService;

import java.awt.*;

public class NotificationUtil {

    private static TrayIcon trayIcon;
    private static final DatabaseService db = new DatabaseService();

    static {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();

                // 🔄 Enhanced: Dynamic icon based on trial urgency
                String iconPath = db.isActivated() ? "icon.png" : "alert-icon.png";
                Image image = Toolkit.getDefaultToolkit().createImage(iconPath);


                trayIcon = new TrayIcon(image, "Libra");
                trayIcon.setImageAutoSize(true);

                // 🆕 Updated: Tooltip now reflects trial context
                trayIcon.setToolTip("Libra Trial Reminder");

                tray.add(trayIcon);
            } catch (Exception e) {
                trayIcon = null;
                System.err.println("Tray initialization failed: " + e.getMessage());
            }
        }
    }

    public static void showSystemTrayMessage(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } else {
            // 🆕 Logged fallback for analytics/debugging
            System.out.println("[TrayFallback] " + title + ": " + message);
        }
    }


}
