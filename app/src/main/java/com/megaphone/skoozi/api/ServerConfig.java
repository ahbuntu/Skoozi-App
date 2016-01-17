package com.megaphone.skoozi.api;

public class ServerConfig {

    private static final String DEBUG_API = "http:192.168.1.13:8282/_ah/api";
    private static final String PROD_API = "https:skoozi-959.appspot.com/_ah/api";

    private static final String AUDIENCE_PREFIX = "server:client_id:";
    private static final String WEB_CLIENT_ID = "26298710398-8jbuih8cj38ihi87bsloqkvur2mfut11" +
            ".apps.googleusercontent.com";

    private static boolean debugEnabled = true;

    public static String getSkooziUrl() {
        return debugEnabled ? DEBUG_API : PROD_API;
    }

    public static String getCredentialAudience() {
        return AUDIENCE_PREFIX + WEB_CLIENT_ID;
    }
}
