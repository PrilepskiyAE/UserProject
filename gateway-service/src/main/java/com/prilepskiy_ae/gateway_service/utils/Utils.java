package com.prilepskiy_ae.gateway_service.utils;

public class Utils {
    public static boolean isUserPath(String path) {
        return path.equals("/api/users") || path.startsWith("/api/users/");
    }

    public static boolean isNotificationPath(String path) {
        return path.equals("/api/notification") || path.startsWith("/api/notification/");
    }
}
