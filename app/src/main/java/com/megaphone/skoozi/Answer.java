package com.megaphone.skoozi;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ahmadul.hassan on 2015-05-02.
 */
public class Answer implements Parcelable {
    private String author;
    private String content;
    private String questionKey;
    private long timestamp;
    private double locationLat;
    private double locationLon;

    private String postKey;

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

    public String getPostKey() {
        return postKey;
    }

    public String getQuestionKey() {
        return questionKey;
    }

    public void setQuestionKey(String questionKey) {
        this.questionKey = questionKey;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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
        setQuestionKey(in.readString());
        setAuthor(in.readString());
        setContent(in.readString());
        setTimestamp(in.readLong());
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
        dest.writeString(getQuestionKey());
        dest.writeString(getAuthor());
        dest.writeString(getContent());
        dest.writeLong(getTimestamp());
        dest.writeDouble(getLocationLat());
        dest.writeDouble(getLocationLon());
    }

    //endregion
}
