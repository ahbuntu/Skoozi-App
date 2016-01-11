package com.megaphone.skoozi;

import android.location.Location;

import com.megaphone.skoozi.model.Question;

import java.util.List;

/**
 * Created by ahmadul.hassan on 2016-01-10.
 */
public class PendingMapUpdate {

    public Location origin;
    public int radius;
    public List<Question> questions;

    PendingMapUpdate(Location origin, int radius) {
        this.origin = origin;
        this.radius = radius;
    }

    public void setMapQuestionsMarkers(List<Question> questions) {
        this.questions = questions;
    }
}
