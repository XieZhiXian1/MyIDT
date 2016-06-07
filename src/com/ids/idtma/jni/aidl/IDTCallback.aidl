package com.ids.idtma.jni.aidl;
import com.ids.idtma.jni.aidl.GroupData;
import com.ids.idtma.jni.aidl.UserData;
import com.ids.idtma.jni.aidl.UserGroup;
import com.ids.idtma.jni.aidl.GpsData;

interface IDTCallback
{
    //״ָ̬ʾ
	void IDT_StatusInd(int status, int cause);
	//����Ϣָʾ
	void IDT_GInfoInd(in UserGroup userInfo);
	//����Ӧ��
	int IDT_CallPeerAnswer(long pUsrCtx, int type, String num, String name);
	//�յ�����
	int IDT_CallIn(int ID, String pcMyNum, String pcPeerNum, int SrvType,
	    int iAudioRecv, int iAudioSend, int iVideoRecv, int iVideoSend, long pExtInfo);
	//�Է���IDT�ڲ��ͷź���
	int IDT_CallRelInd(int ID, long pUsrCtx, int uiCause);
	//��Ȩָʾ
	int IDT_CallMicInd(long pUsrCtx, int uiInd);
	//��������ʾ
	void IDT_CallTalkingIDInd(long pUsrCtx, String num, String name);
	//ͨ��״̬���յ��Է����͵ĺ���
	int IDT_CallRecvNum(long pUsrCtx, char cNum);
	//�յ���Ƶ����
	int IDT_CallRecvVideoData(long pUsrCtx, int ucCodec,in byte[] ucBuf, int IFrame, int uiTs);
	
	int IDT_RecvPassThrouth(String myNum, String peerNum, String pBuf, int iLen);
	
	//�յ���Ϣ
	int IDT_RecvIM(String pcFrom, String pcTo, String pBuf, int iLen);
	
	void IDT_UOptRsp(int dwOptCode, int dwSn, int wRes, in UserData userData);
	
	void IDT_GOptRsp(int dwOptCode, int dwSn, int wRes, in GroupData groupData);     
    //��ȡIM�ļ���
	int IDT_IMGetFileNameRsp(int dwSn,String pcFileName);
    //�յ�IM��Ϣ
	int IDT_IMRecv(String pucSn, int dwType, String pcFrom, String pcTo,String pcOriTo,String pcTxt, String pcFileName, String pcSourceFileName);
	// IM״ָ̬ʾ
	int IDT_IMStatusInd(int dwSn, String pucSn, int dwType, int ucStatus);
	//�յ��û���gps��Ϣ
	void IDT_GpsRecInd(String ucNum, int ucStatus, float longitude, float latitude, float speed, float direction, int year,
			int month, int day, int hour, int minute, int second);
	//��ȡ�û�״̬
	void IDT_GUStatusInd(int iType, String ucNum, int iStatus, int iSrvType, int iCallStatue);
	//�����ʱ����Ƶ�������Ƿ�
	int IDT_CallSetVideoCodec(int ucRecv,int ucSend);
	// ����ͳ��
	void IDT_CallMediaStats(long pUsrCtx, int ucType, int uiRxBytes, int uiRxUsrBytes, int uiRxCount, int uiRxUserCount,
				int uiTxBytes, int uiTxUsrBytes, int uiTxCount ,int uiTxUserCount);
}