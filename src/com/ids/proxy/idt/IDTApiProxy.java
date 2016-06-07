package com.ids.proxy.idt;

import com.ids.idtma.jni.IDTApi;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.service.IDTNativeService;
import com.ids.proxy.IDSApiProxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class IDTApiProxy implements IDSApiProxy {
	private boolean isInited = false;

	@Override
	public void loadLibrary(Activity context) {
		System.loadLibrary("svcapi");
	}

	@Override
	public void init(Activity context) {
		if (isInited)
			return;
		isInited = true;
		// 启动一个与so库进行互动的服务
		Intent startIDTService = new Intent(context, IDTNativeService.class);
		context.startService(startIDTService);
	}

	public void unloadLibrary(Context context) {
		isInited = false;
		context.stopService(new Intent(context, IDTNativeService.class));
	}

	@Override
	public int CallMakeOut(String pcPeerNum, int SrvType, MediaAttribute pAttr, long pUsrCtx) {
		return IDTApi.IDT_CallMakeOut(pcPeerNum, SrvType, pAttr, pUsrCtx);
	}

	@Override
	public int CallAnswer(int ID, MediaAttribute pAttr, long pUsrCtx) {
		return IDTApi.IDT_CallAnswer(ID, pAttr, pUsrCtx);
	}

	//取消当前通话
	@Override
	public int CallRel(int ID, long pUsrCtx, int uiCause) {
		return IDTApi.IDT_CallRel(ID, pUsrCtx, uiCause);
	}

	@Override
	public int Exit() {
		return IDTApi.IDT_Exit();
	}

	@Override
	public int CallMicCtrl(int ID, boolean bWant) {
		return IDTApi.IDT_CallMicCtrl(ID, bWant);
	}

	@Override
	public int SendIM(String from, String pcPeerNum, String pBuf, int iLen) {
		return IDTApi.IDT_SendIM(from, pcPeerNum, pBuf, iLen);
	}

	@Override
	public int CallSendVideoData(int ID, int ucCodec, byte[] pucHdr, int iHdrLen, byte[] pucBuf, int iLen, int IFrame,
			int uiTs, int uiDatalLen, int uiFlg) {
		return IDTApi.IDT_CallSendVideoData(ID, ucCodec, pucHdr, iHdrLen, pucBuf, iLen, IFrame, uiTs, uiDatalLen,
				uiFlg);
	}
	
	@Override
	public int iMGetFileName(int dwSn, String pcTo, int dwType) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_IMGetFileName(dwSn, pcTo, dwType);
	}

	@Override
	public int iMSend(int dwSn, int dwType, String pcTo, String pcTxt, String pcFileName, String pcSourceFileName) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_IMSend(dwSn, dwType, pcTo, pcTxt, pcFileName, pcSourceFileName);
	}

	@Override
	public int iMRead(int dwSn, String pucSn, int dwType, String pcTo) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_IMRead(dwSn, pucSn, dwType, pcTo);
	}
	
	@Override
	public int GpsReport(float longitude, float latitude, float speed, float direction,
			int year, int month, int day, int hour, int minute, int second) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_GpsReport(longitude, latitude, speed, direction, year, month, day, hour, minute, second);
	}
	
	@Override
	public int SetSurface(Object object) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_SetSurface(object);
	}
	
	@Override
	public int GpsSubs(String pcNum, int ucSubs) {
		// TODO Auto-generated method stub
		return IDTApi.IDT_GpsSubs(pcNum, ucSubs);
	}

	@Override
	public Class getVideoCallActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lockGroup(String tel) {
		// TODO Auto-generated method stub
		
	}

}
