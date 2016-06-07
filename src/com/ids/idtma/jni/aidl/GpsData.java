package com.ids.idtma.jni.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class GpsData implements Parcelable {
	String ucNum; // 号码
	int ucStatus; // 是否在线,UT_STATUS_ONLINE
	float longitude; // 经度
	float latitude; // 纬度
	float speed; // 速度
	float direction; // 方向
	// 时间
	int year; // 年
	int month; // 月
	int day; // 日
	int hour; // 时
	int minute; // 分
	int second; // 秒

	public GpsData(String ucNum, int ucStatus, float longitude, float latitude, float speed, float direction, int year,
			int month, int day, int hour, int minute, int second) {
		super();
		this.ucNum = ucNum;
		this.ucStatus = ucStatus;
		this.longitude = longitude;
		this.latitude = latitude;
		this.speed = speed;
		this.direction = direction;
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
	}

	public GpsData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUcNum() {
		return ucNum;
	}

	public void setUcNum(String ucNum) {
		this.ucNum = ucNum;
	}

	public int getUcStatus() {
		return ucStatus;
	}

	public void setUcStatus(int ucStatus) {
		this.ucStatus = ucStatus;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<GpsData> CREATOR = new Parcelable.Creator<GpsData>() {
		public GpsData createFromParcel(Parcel in) {
			return new GpsData(in);
		}

		public GpsData[] newArray(int size) {
			return new GpsData[size];
		}
	};

	private GpsData(Parcel in) {
		ucNum = in.readString();
		ucStatus = in.readInt();
		longitude = in.readFloat();
		latitude = in.readFloat();
		speed = in.readFloat();
		direction = in.readFloat();
		year = in.readInt();
		month = in.readInt();
		day = in.readInt();
		hour = in.readInt();
		minute = in.readInt();
		second = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(ucNum);
		dest.writeInt(ucStatus);
		dest.writeFloat(longitude);
		dest.writeFloat(latitude);
		dest.writeFloat(speed);
		dest.writeFloat(direction);
		dest.writeInt(year);
		dest.writeInt(month);
		dest.writeInt(day);
		dest.writeInt(hour);
		dest.writeInt(minute);
		dest.writeInt(second);
	}
}
