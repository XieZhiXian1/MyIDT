package com.ids.idtma.jni;

import com.ids.idtma.jni.aidl.GpsData;
import com.ids.idtma.jni.aidl.IDTCallback;
import com.ids.idtma.jni.aidl.MediaAttribute;
import com.ids.idtma.jni.aidl.SDPAudioInfo;
import com.ids.idtma.jni.aidl.SDPVideoInfo;
import com.ids.idtma.jni.aidl.UserData;
import com.ids.idtma.util.LwtLog;

public class IDTApi {
	/**
	 * 初始化
	 */
	public static void init() {
		try {
			IDTNativeApi.init();
		} catch (Throwable e) {
			LwtLog.d("wulin", "init------------------此处有bug");
			e.printStackTrace();
		}
	}

	// 释放
	public static void dispose() {
		try {
			IDTNativeApi.dispose();
		} catch (Throwable e) {
			LwtLog.d("wulin", "dispose------------------此处有bug");
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------
	// 启动
	// 输入:
	// pcIniFile: INI文件名
	// iMaxCallNum: 最大并发呼叫数
	// cIp: 服务器IP地址
	// iPort: 服务器端口号
	// cNumber: 号码
	// cPwd: 密码
	// iRegFlag: 是否要注册,0不需要,1需要注册
	// CallBack: 回调函数
	// iSigPort: 信令端口 = 10001
	// iMedRtpPort: RTP媒体端口 = 11000
	// iMedTcpPort: TCP媒体监听端口 = 0
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_Start(String pcIniFile, int iMaxCallNum, String pcIp, int iPort, String pcNum, String pcPwd,
			int iRegFlag, IDTCallback callBack, int iSigPort, int iMedRtpPort, int iMedTcpPort) {
		try {
			return IDTNativeApi.IDT_Start(pcIniFile, iMaxCallNum, pcIp, iPort, pcIp, 10001, pcNum, pcPwd, iRegFlag,
					callBack, iSigPort, iMedRtpPort, iMedTcpPort);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_Start------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 退出
	// 输入:
	// 无
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_Exit() {
		try {
			return IDTNativeApi.IDT_Exit();
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_Exit------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 用户状态查询
	 * 
	 * @return
	 */
	public static int IDT_Status() {
		try {
			return IDTNativeApi.IDT_Status();
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_Status------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 修改用户属性
	 * 
	 * @param cIp
	 * @param iPort
	 * @param cNumber
	 * @param cPwd
	 * @return
	 */
	public static int IDT_ModifyProperty(String cIp, int iPort, String cNumber, String cPwd) {
		try {
			return IDTNativeApi.IDT_ModifyProperty(cIp, iPort, cNumber, cPwd);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_ModifyProperty------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// 启动呼出
	// 输入:
	// cPeerNum: 对方号码
	// SrvType: 业务类型   单呼为16  会议17
	// pAttr: 媒体属性
	// pUsrCtx: 用户上下文
	// pcPwd: 密码,创建或进入会场的密码
	// ucCallOut: 服务器是否直接呼出,0不呼出,1直接呼出
	// ucDelG: 会议结束后,是否删除组,0不删除,1删除
	// 返回:
	// -1: 失败
	// else: 呼叫标识
	// 注意:
	// 如果是组呼(SRV_TYPE_CONF, 语音发送为1,语音接收为0, 视频未定义,或者与语音相同)
	// 1.pcPeerNum为组号码
	// 2.pAttr中,ucAudioSend为1,其余为0
	// 如果是会议:
	// 1.发起会议(SRV_TYPE_CONF, 语音发送为1,语音接收为1)
	// a)被叫号码可以为空,或者用户号码/组号码	
	// b)pcPwd为会议密码
	// c)在CallPeerAnswer时,带回会议的内部号码,为交换机产生的呼叫标识
	// 2.加入会议(SRV_TYPE_CONF_JOIN,语音发送为1,语音接收为1)
	// a)pcPeerNum为1中的c
	// b)pcPwd为1中的b
	public static int IDT_CallMakeOut(String pcPeerNum, int SrvType, MediaAttribute pAttr, long pUsrCtx)// ,
																										// void
																										// *pUsrCtx)
	{
		try {
			return IDTNativeApi.IDT_CallMakeOut(pcPeerNum, SrvType, pAttr, pUsrCtx);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallMakeOut------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 呼入应答 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pAttr
	 *            : 媒体属性
	 * @param pUsrCtx
	 *            : 用户上下文
	 * @return 返回: 0: 成功 -1: 失败
	 */
	public static int IDT_CallAnswer(int ID, MediaAttribute pAttr, long pUsrCtx) {
		try {
			return IDTNativeApi.IDT_CallAnswer(ID, pAttr, pUsrCtx);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallAnswer------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 呼叫释放 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pUsrCtx
	 *            : 用户上下文,通常不用这个,但启动主叫后,没有收到主叫应答
	 * @param uiCause
	 *            : 释放原因值
	 * @return 返回: 0: 成功 -1: 失败
	 */
	public static int IDT_CallRel(int ID, long pUsrCtx, int uiCause) {
		try {
			return IDTNativeApi.IDT_CallRel(ID, pUsrCtx, uiCause);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallRel------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 通话状态下发送号码 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param cNum
	 *            : 发送的号码,ASC字符串形式,有效值为'0'~'9','*','#','A'~'D',16(FLASH)
	 * @return返回: 0: 成功 -1: 失败
	 */
	public static int IDT_CallSendNum(int ID, String cNum) {
		try {
			return IDTNativeApi.IDT_CallSendNum(ID, cNum);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallSendNum------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 话权控制 输入:
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param bWant
	 *            : 是否期望话权，按下就是true，申请话权; 放开就是false，释放话权
	 * @return: 0: 成功 -1: 失败
	 */
	public static int IDT_CallMicCtrl(int ID, boolean bWant) {
		try {
			return IDTNativeApi.IDT_CallMicCtrl(ID, bWant);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallMicCtrl------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param pAInfo
	 *            : 语音信息
	 * @param pVInfo
	 *            : 视频信息
	 * @return返回: 0: 成功 -1: 失败
	 */
	public static int IDT_CallModify(int ID, SDPAudioInfo pAInfo, SDPVideoInfo pVInfo) {
		try {
			return IDTNativeApi.IDT_CallModify(ID, pAInfo, pVInfo);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallModify------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 发送语音数据 输入:????????????????????????????????????????????????
	 * 
	 * @param ID
	 *            : IDT的呼叫ID
	 * @param ucCodec
	 *            : CODEC
	 * @param pucBuf
	 *            : 数据
	 * @param iLen
	 *            : 数据长度
	 * @param uiTs
	 *            : 时戳
	 * @return 返回: 0: 成功 -1: 失败
	 */
	public static int IDT_CallSendAuidoData(int ID, int ucCodec, byte[] pucBuf, int iLen, int uiTs) {
		try {
			return IDTNativeApi.IDT_CallSendAuidoData(ID, ucCodec, pucBuf, iLen, uiTs);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallSendAuidoData------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 发送视频数据
	// 输入:
	// ID: IDT的呼叫ID
	// ucCodec: CODEC
	// pucBuf: 数据
	// iLen: 数据长度
	// IFrame: I帧标识
	// uiTs: 时戳
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_CallSendVideoData(int ID, int ucCodec, byte[] pucHdr, int iHdrLen, byte[] pucBuf, int iLen,
			int IFrame, int uiTs, int uiDatalLen, int uiFlg) {
		try {
			return IDTNativeApi.IDT_CallSendVideoData(ID, ucCodec, pucHdr, iHdrLen, pucBuf, iLen, IFrame, uiTs,
					uiDatalLen, uiFlg);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_CallSendVideoData------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	public static int IDT_ForceRel(String pcPeerNum) {
		try {
			return IDTNativeApi.IDT_ForceRel(pcPeerNum);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_ForceRel------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	public static int IDT_SendIM(String from, String pcPeerNum, String pBuf, int iLen) {
		try {
			return IDTNativeApi.IDT_SendIM(from, pcPeerNum, pBuf, iLen);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_SendIM------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	public static int IDT_SendGpsInfo(long dwDevTimeStamp, // 设备时间戳，秒级
			long dwGPSTimeStamp, // GPS卫星时间戳，秒级
			double fLongitude, // 经度
			double fLatitude, // 纬度
			float forientation, // 方向
			float fvelocity // 速度：千米/小时
	) {
		try {
			return IDTNativeApi.IDT_SendGpsInfo(dwDevTimeStamp, dwGPSTimeStamp, fLongitude, fLatitude, forientation,
					fvelocity);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_SendGpsInfo------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 添加用户
	// 输入:
	// dwSn: 操作序号
	// pUser: 用户信息
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_UAdd(long dwSn, UserData pUser) {
		try {
			return IDTNativeApi.IDT_UAdd(dwSn, pUser);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_UAdd------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 删除用户
	// 输入:
	// dwSn: 操作序号
	// pucNum: 用户号码
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_UDel(long dwSn, String pucNum) {
		try {
			return IDTNativeApi.IDT_UDel(dwSn, pucNum);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_UDel------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 修改用户
	// 输入:
	// dwSn: 操作序号
	// pUser: 用户信息
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_UModify(long dwSn, UserData pUser) {
		try {
			return IDTNativeApi.IDT_UModify(dwSn, pUser);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_UModify------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 查询用户
	// 输入:
	// dwSn: 操作序号
	// pucNum: 用户号码
	// 返回:
	// 0: 成功
	// -1: 失败
	// --------------------------------------------------------------------------------
	public static int IDT_UQuery(long dwSn, String pucNum) {
		try {
			return IDTNativeApi.IDT_UQuery(dwSn, pucNum);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_UQuery------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// --------------------------------------------------------------------------------
	// 查询组内用户信息
	// 输入:
	// dwSn: 操作序号
	// pucNum: 组号码
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_GQueryU(long dwSn, String pucNum) {
		try {
			return IDTNativeApi.IDT_GQueryU(dwSn, pucNum);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_GQueryU------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// 获取IM文件名
	// 输入:
	// dwSn: 消息事务号
	// pcTo: 目的号码
	// dwType: 及时消息类型,IM_TYPE_IMAGE等
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_IMGetFileName(int dwSn, String pcTo, int dwType) {
		try {
			return IDTNativeApi.IDT_IMGetFileName(dwSn, pcTo, dwType);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_IMGetFileName------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// 发送IM消息
	// 输入:
	// dwSn: 消息事务号
	// dwType: 及时消息类型,IM_TYPE_IMAGE等
	// pcTo: 目的号码
	// pcTxt: 文本内容
	// pcFileName: 文件名
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_IMSend(int dwSn, int dwType, String pcTo, String pcTxt, String pcFileName,
			String pcSourceFileName) {
		try {
			return IDTNativeApi.IDT_IMSend(dwSn, dwType, pcTo, pcTxt, pcFileName, pcSourceFileName);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_IMSend------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// 阅读IM消息
	// 输入:
	// dwSn: 消息事务号
	// pucSn: 系统的事务号
	// dwType: 及时消息类型
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_IMRead(int dwSn, String pucSn, int dwType, String pcTo) {
		try {
			return IDTNativeApi.IDT_IMRead(dwSn, pucSn, dwType, pcTo);
		} catch (Throwable e) {
			LwtLog.d("wulin", "IDT_IMRead------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// 上报自己的GPS信息
	// 输入:
	// pGps: GPS信息
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_GpsReport(float longitude, float latitude, float speed, float direction, int year, int month,
			int day, int hour, int minute, int second) {
		try {
			return IDTNativeApi.IDT_GpsReport(longitude, latitude, speed, direction, year, month, day, hour, minute,
					second);
		} catch (Throwable e) {
			LwtLog.d("gps", "IDT_GpsReport------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}

	// 订阅GPS
	// 输入:
	// pucNum: 用户号码或组号码,第一个字符是"##0"表示取消之前所有订阅,"0"表示所有用户
	// ucSubs: 是否订阅,0取消订阅,1订阅
	// 返回:
	// 0: 成功
	// -1: 失败
	public static int IDT_GpsSubs(String pcNum, int ucSubs) {
		try {
			return IDTNativeApi.IDT_GpsSubs(pcNum, ucSubs);
		} catch (Throwable e) {
			LwtLog.d("gps", "IDT_GpsSubs------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}
	
	public static int IDT_SetSurface(Object object) {
		try {
			return IDTNativeApi.IDT_SetSurface(object);
		} catch (Throwable e) {
			LwtLog.d("gps", "IDT_SetSurface------------------此处有bug");
			e.printStackTrace();
			return -1;
		}
	}
}
