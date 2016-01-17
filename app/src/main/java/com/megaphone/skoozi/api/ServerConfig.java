package com.megaphone.skoozi.api;

public class ServerConfig {

    private static final String DEBUG_API = "http:192.168.1.13:8282/_ah/api";
    private static final String PROD_API = "https:skoozi-959.appspot.com/_ah/api";

    private static boolean debugEnabled = true;

    public static String getSkooziUrl() {
        return debugEnabled ? DEBUG_API : PROD_API;
    }
}
