package com.megaphone.skoozi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.megaphone.skoozi.thread.ThreadItemVhBinder;

/**
 * Created by ahmadul.hassan on 2015-05-02.
 */
public class Question implements Parcelable, ThreadItemVhBinder.TypeContract {
    public String author;
    public String content;
    public String key;
    public Long timestamp;
    public double locationLat;
    public double locationLon;

    public Question() {

    }

    public Question(String author, String content, String key, Long timestamp, double locationLat, double locationLon) {
        this.author = author;
        this.content = content;
        this.key = key;
        this.timestamp = timestamp;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
    }

    //region Parcelable implementation

    /**
     * generate instances of the Question class from a Parcel
     */
    public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * private constructor to create a question from a parcel. should only be called from Parcelable Creator
     * @param in - the parcel from which to create the Question object
     */
    private Question(Parcel in) {
        readFromParcel(in);
    }

    /**
     * called by the Person parcelable constructor
     * @param in the parcel used to construct the Question object
     */
    public void readFromParcel(Parcel in) {
        this.author = (in.readString());
        this.content = (in.readString());
        this.key = (in.readString());
        this.timestamp = in.readLong();
        this.locationLat = (in.readDouble());
        this.locationLon = (in.readDouble());
    }

    /**
     * method that saves the attribute data of the Question class to a parcel
     * essentially flattens the Question object
     * @param dest values that the destination parcel will contain
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.content);
        dest.writeString(this.key);
        dest.writeLong(this.timestamp);
        dest.writeDouble(this.locationLat);
        dest.writeDouble(this.locationLon);
    }

    //endregion
}
