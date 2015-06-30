package com.megaphone.skoozi;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ahmadul.hassan on 2015-05-02.
 */
public class Answer implements Parcelable {
    private String author;
    private String content;
    private String key;
    private String timestamp;
    private double locationLat;
    private double locationLon;

    public Answer() {

    }

    public Answer(String key, String author, String content, String timestamp, double locationLat, double locationLon) {
        this.key = key;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLon() {
        return locationLon;
    }

    public void setLocationLon(double locationLon) {
        this.locationLon = locationLon;
    }

    //region IParcelable implementation

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
        setKey(in.readString());
        setAuthor(in.readString());
        setContent(in.readString());
        setTimestamp(in.readString());
        setLocationLat(in.readDouble());
        setLocationLon(in.readDouble());
    }

    /**
     * method that saves the attribute data of the Answer class to a parcel
     * essentially flattens the Answer object
     * @param dest values that the destination parcel will contain
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getKey());
        dest.writeString(getAuthor());
        dest.writeString(getContent());
        dest.writeString(getTimestamp());
        dest.writeDouble(getLocationLat());
        dest.writeDouble(getLocationLon());
    }

    //endregion
}
