package com.ngong.librasoftware.utils;

import java.awt.*;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class SubscriptionBrowserLauncher {

    public static void launchEmail(String schoolName) {
        String mac = getMacAddress();
        String subject = encode("Libra Installation Notification");
        String body = encode(schoolName + " has installed Libra on " + mac);

        String url = "https://mail.google.com/mail/?view=cm&fs=1" +
                "&to=joncopy7@gmail.com" +
                "&su=" + subject +
                "&body=" + body;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Failed to open browser.");
            e.printStackTrace();
        }
    }

    public static String getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                byte[] mac = ni.getHardwareAddress();

                if (mac != null && mac.length == 6 && !ni.isLoopback() && ni.isUp()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            ErrorNotifier.show("MAC address lookup failed", e);
        }
        return "Unknown MAC";
    }


    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
