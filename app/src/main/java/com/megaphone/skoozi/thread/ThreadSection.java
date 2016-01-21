package com.megaphone.skoozi.thread;

import com.megaphone.skoozi.base.BaseSection;

public class ThreadSection extends BaseSection {
    int firstPosition;
    String title;

public ThreadSection(int firstPosition, String title) {
        super(firstPosition);
        this.firstPosition = firstPosition;
        this.title = title;
    }
}
