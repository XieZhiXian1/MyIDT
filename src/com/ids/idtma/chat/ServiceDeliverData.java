package com.ids.idtma.chat;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ServiceDeliverData implements Parcelable {
	private int style;
	private Uri uri;
	private String callto_persion_num;
	private String sms_resource_url;
	private String pcFileName;

	public ServiceDeliverData(int style, Uri uri, String callto_persion_num ,String sms_resource_url,String pcFileName) {
		super();
		this.style = style;
		this.uri = uri;
		this.callto_persion_num = callto_persion_num;
		this.sms_resource_url=sms_resource_url;
		this.pcFileName=pcFileName;
	}

	public ServiceDeliverData(Parcel source) {
		style = source.readInt();
		uri = (Uri) source.readValue(Uri.class.getClassLoader());
		callto_persion_num = source.readString();
		sms_resource_url=source.readString();
		pcFileName=source.readString();
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
	

	public String getSms_resource_url() {
		return sms_resource_url;
	}

	public void setSms_resource_url(String sms_resource_url) {
		this.sms_resource_url = sms_resource_url;
	}

	public String getPcFileName() {
		return pcFileName;
	}

	public void setPcFileName(String pcFileName) {
		this.pcFileName = pcFileName;
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
		dest.writeString(sms_resource_url);
		dest.writeString(pcFileName);
	}

	public final Parcelable.Creator<ServiceDeliverData> CREATOR = new Parcelable.Creator<ServiceDeliverData>() {
		@Override
		public ServiceDeliverData createFromParcel(Parcel source) {
			return new ServiceDeliverData(source);
		}

		@Override
		public ServiceDeliverData[] newArray(int size) {
			return new ServiceDeliverData[size];
		}
	};

}