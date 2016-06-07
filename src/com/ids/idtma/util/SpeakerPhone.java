/**
 * 免提功能管理类
 */
package com.ids.idtma.util;

import android.content.Context;
import android.media.AudioManager;

public class SpeakerPhone {

	private Context mContext;
	private int currVolume;

	public SpeakerPhone() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SpeakerPhone(Context mContext) {
		super();
		this.mContext = mContext;
	}

	// 打开扬声器
	public void OpenSpeaker() {
		try {
			AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			// audioManager.setMode(AudioManager.ROUTE_SPEAKER);
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

			if (!audioManager.isSpeakerphoneOn()) {
				audioManager.setSpeakerphoneOn(true);

				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
						audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
						AudioManager.STREAM_VOICE_CALL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 关闭扬声器
	public void CloseSpeaker() {

		try {
			AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager != null) {
				if (audioManager.isSpeakerphoneOn()) {
					audioManager.setSpeakerphoneOn(false);
					audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
							AudioManager.STREAM_VOICE_CALL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
