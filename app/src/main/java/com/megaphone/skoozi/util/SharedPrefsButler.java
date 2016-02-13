package com.megaphone.skoozi.util;

public class SharedPrefsButler {

    public static String getUserNickname() {
        return SharedPrefsUtil.getInstance().getString(SharedPrefsUtil.KEY_USER_NICKNAME, null);
    }

    public void putFutureUserNickname(String userNickname) {
        SharedPrefsUtil.getEditor()
                .putString(SharedPrefsUtil.KEY_ACCOUNT_NAME, userNickname).apply();
    }
}