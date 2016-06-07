package com.ids.idtma.chat;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class DeliverData implements Parcelable {
	private int style;
	private Uri uri;
	private String callto_persion_num;
	private String filename;

	public DeliverData(int style, Uri uri, String callto_persion_num,String filename) {
		super();
		this.style = style;
		this.uri = uri;
		this.callto_persion_num = callto_persion_num;
		this.filename=filename;
	}

	public DeliverData(Parcel source) {
		style = source.readInt();
		uri = (Uri) source.readValue(Uri.class.getClassLoader());
		callto_persion_num = source.readString();
		filename=source.readString();
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getCallto_persion_num() {
		return callto_persion_num;
	}

	public void setCallto_persion_num(String callto_persion_num) {
		this.callto_persion_num = callto_persion_num;
	}
	

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(style);
		dest.writeValue(uri);
		dest.writeString(callto_persion_num);
		dest.writeString(filename);
	}

	public final Parcelable.Creator<DeliverData> CREATOR = new Parcelable.Creator<DeliverData>() {
		@Override
		public DeliverData createFromParcel(Parcel source) {
			return new DeliverData(source);
		}

		@Override
		public DeliverData[] newArray(int size) {
			return new DeliverData[size];
		}
	};

}