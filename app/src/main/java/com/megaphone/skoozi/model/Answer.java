package com.megaphone.skoozi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.megaphone.skoozi.thread.ThreadVhMaker;

/**
 * Created by ahmadul.hassan on 2015-05-02.
 */
public class Answer implements Parcelable, ThreadVhMaker.TypeContract {
    public String postKey;
    public String author;
    public String content;
    public String questionKey;
    public long timestamp;
    public double locationLat;
    public double locationLon;

    public Answer() {
    }

    /**
     * Use when creating an instance of Answer that hasen't been posted yet
     * @param questionKey
     * @param author
     * @param content
     * @param timestamp
     * @param locationLat
     * @param locationLon
     */
    public Answer(String questionKey, String author, String content, long timestamp, double locationLat, double locationLon) {
        this.questionKey = questionKey;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
    }

    /**
     * Use when creating an instance of an Answer that already exists
     * @param postKey
     * @param questionKey
     * @param author
     * @param content
     * @param timestamp
     * @param locationLat
     * @param locationLon
     */
    public Answer(String postKey, String questionKey, String author, String content, long timestamp, double locationLat, double locationLon) {
        this.postKey = postKey;
        this.questionKey = questionKey;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
    }

    //region Parcelable implementation

    /**
     * generate instances of the Answer class from a Parcel
     */
    public static final Creator<Answer> CREATOR = new Creator<Answer>() {
        public Answer createFromParcel(Parcel in) {
            return new Answer(in);
        }

        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * private constructor to create a anaswer from a parcel. should only be called from Parcelable Creator
     * @param in - the parcel from which to create the Answer object
     */
    private Answer(Parcel in) {
        readFromParcel(in);
    }

    /**
     * called by the Person parcelable constructor
     * @param in the parcel used to construct the Answer object
     */
    public void readFromParcel(Parcel in) {
        this.postKey = in.readString();
        this.questionKey = in.readString();
        this.author = in.readString();
        this.content = in.readString();
        this.timestamp = in.readLong();
        this.locationLat = in.readDouble();
        this.locationLon= in.readDouble();
    }

    /**
     * method that saves the attribute data of the Answer class to a parcel
     * essentially flattens the Answer object
     * @param dest values that the destination parcel will contain
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.postKey);
        dest.writeString(this.questionKey);
        dest.writeString(this.author);
        dest.writeString(this.content);
        dest.writeLong(this.timestamp);
        dest.writeDouble(this.locationLat);
        dest.writeDouble(this.locationLon);
    }

    //endregion
}
