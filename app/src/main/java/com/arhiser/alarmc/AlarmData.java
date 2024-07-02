package com.arhiser.alarmc;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmData implements Parcelable {

    private String alarmText;
    private String dayOfWeek;
    private String weekType;
    private int hour;
    private int minute;


    public AlarmData(String alarmText, String dayOfWeek, String weekType, int hour, int minute) {
        this.alarmText = alarmText;
        this.dayOfWeek = dayOfWeek;
        this.weekType = weekType;
        this.hour = hour;
        this.minute = minute;
    }

    protected AlarmData(Parcel in) {
        alarmText = in.readString();
        dayOfWeek = in.readString();
        weekType = in.readString();
        hour = in.readInt();
        minute = in.readInt();
    }

    public static final Creator<AlarmData> CREATOR = new Creator<AlarmData>() {
        @Override
        public AlarmData createFromParcel(Parcel in) {
            return new AlarmData(in);
        }

        @Override
        public AlarmData[] newArray(int size) {
            return new AlarmData[size];
        }
    };

    public String getAlarmText() {
        return alarmText;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getWeekTypeText() {
        return weekType;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(alarmText);
        dest.writeString(dayOfWeek);
        dest.writeString(weekType);
        dest.writeInt(hour);
        dest.writeInt(minute);
    }

    public static AlarmData fromString(String alarmString) {
        String[] parts = alarmString.split(",");
        if (parts.length >= 5) {
            String alarmText = parts[0].trim();
            String dayOfWeek = parts[1].trim();
            String weekType = parts[2].trim();

            int hour = parts[3].trim().isEmpty() ? 0 : Integer.parseInt(parts[3].trim());
            int minute = parts[4].trim().isEmpty() ? 0 : Integer.parseInt(parts[4].trim());

            return new AlarmData(alarmText, dayOfWeek, weekType, hour, minute);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%d,%d",
                alarmText, dayOfWeek, weekType, hour, minute);
    }
}
