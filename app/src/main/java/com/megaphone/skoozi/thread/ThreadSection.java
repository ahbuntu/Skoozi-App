package com.megaphone.skoozi.thread;

import com.megaphone.skoozi.base.BaseSection;
import com.megaphone.skoozi.model.Question;

public class ThreadSection extends BaseSection {
    String title;
    Question question;

    public ThreadSection(int firstPosition, String title, Question question) {
        super(firstPosition);
        this.title = title;
        this.question = question;
    }

}
