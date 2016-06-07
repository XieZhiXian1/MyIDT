package com.ids.idtma.jni.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaAttribute implements Parcelable{

	public int ucAudioSend; // 是否发送语音
	public int ucAudioRecv; // 是否接收语音
	public int ucVideoSend; // 是否发送视频
	public int ucVideoRecv; // 是否接收视频
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int arg1) 
	{
		out.writeInt(ucAudioSend);
		out.writeInt(ucAudioRecv);
		out.writeInt(ucVideoSend);
		out.writeInt(ucVideoRecv);
	}
	
	public MediaAttribute()
	{
		
	}
	
	
	public MediaAttribute(Parcel in)
	{
		ucAudioSend = in.readInt();
		ucAudioRecv = in.readInt();
		ucVideoSend = in.readInt();
		ucVideoRecv = in.readInt();
	}
	
	public static final Parcelable.Creator<MediaAttribute> CREATOR = new Parcelable.Creator<MediaAttribute>()
	{

		@Override
		public MediaAttribute createFromParcel(Parcel arg0)
		{
			return new MediaAttribute(arg0);
		}

		@Override
		public MediaAttribute[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new MediaAttribute[arg0];
		}

	};
	
}
