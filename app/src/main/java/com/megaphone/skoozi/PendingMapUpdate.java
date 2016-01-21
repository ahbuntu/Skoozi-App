package com.megaphone.skoozi;

import android.location.Location;

import com.megaphone.skoozi.model.Question;

import java.util.List;

public class PendingMapUpdate {

    public Location origin;
    public int radius;
    public List<Question> questions;

    public PendingMapUpdate(Location origin, int radius) {
        this.origin = origin;
        this.radius = radius;
    }

    public void setMapQuestionsMarkers(List<Question> questions) {
        this.questions = questions;
    }
}
