package com.ids.idtma.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ContactsEntity implements Parcelable {

	private int contactId;
	private String displayName;
	private String phoneNum;
	private String sortKey;
	private Long photoId;
	private String lookUpKey;
	private int selected = 0;
	private String formattedNumber;
	private String pinyin;
	private Bitmap avatar;

	public ContactsEntity() {

	}

	public ContactsEntity(String displayName, String phoneNumber) {
		this.displayName = displayName;
		this.phoneNum = phoneNumber;
	}

	public ContactsEntity(Bitmap avatar, String displayName,
			String phoneNumber, String pinyin) {
		this.avatar = avatar;
		this.displayName = displayName;
		this.phoneNum = phoneNumber;
		this.pinyin = pinyin;
	}

	public int getContactId() {
		return contactId;
	}

	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getSortKey() {
		return sortKey;
	}

	public void setSortKey(String sortKey) {
		this.sortKey = sortKey;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public String getLookUpKey() {
		return lookUpKey;
	}

	public void setLookUpKey(String lookUpKey) {
		this.lookUpKey = lookUpKey;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public String getFormattedNumber() {
		return formattedNumber;
	}

	public void setFormattedNumber(String formattedNumber) {
		this.formattedNumber = formattedNumber;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public static final Parcelable.Creator<ContactsEntity> CREATOR = new Parcelable.Creator<ContactsEntity>() {
		public ContactsEntity createFromParcel(Parcel in) {
			return new ContactsEntity(in);
		}

		public ContactsEntity[] newArray(int size) {
			return new ContactsEntity[size];
		}
	};

	private ContactsEntity(Parcel in) {
		displayName = in.readString();
		phoneNum = in.readString();
		pinyin = in.readString();
		avatar = in.readParcelable(null);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		parcel.writeString(displayName);
		parcel.writeString(phoneNum);
		parcel.writeString(pinyin);
		parcel.writeParcelable(avatar, arg1);
	}
}
