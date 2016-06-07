package com.ids.idtma;

public class UiCauseConstants {
	public static final int CAUSE_ZERO = 0x00; // null
	public static final int CAUSE_UNASSIGNED_NUMBER = 0x01; // 未分配号码
	public static final int CAUSE_NO_ROUTE_TO_DEST = 0x02; // 无目的路由
	public static final int CAUSE_USER_BUSY = 0x03;// 用户忙
	public static final int CAUSE_ALERT_NO_ANSWER = 0x04;// 用户无应答(人不应答)
	public static final int CAUSE_CALL_REJECTED = 0x05; // 呼叫被拒绝
	public static final int CAUSE_ROUTING_ERROR = 0x06; // 路由错误
	public static final int CAUSE_FACILITY_REJECTED = 0x07; // 设备拒绝
	public static final int CAUSE_ERROR_IPADDR = 0x08; // 错误IP地址过来的呼叫
	public static final int CAUSE_NORMAL_UNSPECIFIED = 0x09; // 通常,未指定
	public static final int CAUSE_TEMPORARY_FAILURE = 0x0A; // 临时错误
	public static final int CAUSE_RESOURCE_UNAVAIL = 0x0B; // 资源不可用
	public static final int CAUSE_INVALID_CALL_REF = 0x0C; // 不正确的呼叫参考号
	public static final int CAUSE_MANDATORY_IE_MISSING = 0x0D; // 必选信息单元丢失
	public static final int CAUSE_TIMER_EXPIRY = 0x0E; // 定时器超时
	public static final int CAUSE_CALL_REJ_BY_USER = 0x0F; // 被用户拒绝
	public static final int CAUSE_CALLEE_STOP = 0x10; // 被叫停止
	public static final int CAUSE_USER_NO_EXIST = 0x11; // 用户不存在
	public static final int CAUSE_MS_UNACCESSAVLE = 0x12; // 不可接入
	public static final int CAUSE_MS_POWEROFF = 0x13; // 用户关机
	public static final int CAUSE_FORCE_RELEASE = 0x14; // 强制拆线
	public static final int CAUSE_HO_RELEASE = 0x15; // 切换拆线
	public static final int CAUSE_CALL_CONFLICT = 0x16; // 呼叫冲突
	public static final int CAUSE_TEMP_UNAVAIL = 0x17; // 暂时无法接通
	public static final int CAUSE_AUTH_ERROR = 0x18; // 鉴权错误
	public static final int CAUSE_NEED_AUTH = 0x19; // 需要鉴权
	public static final int CAUSE_SDP_SEL = 0x1A; // SDP选择错误
	public static final int CAUSE_MS_ERROR = 0x1B; // 媒体资源错误
	public static final int CAUSE_INNER_ERROR = 0x1C; // 内部错误
	public static final int CAUSE_PRIO_ERROR = 0x1D; // 优先级不够
	public static final int CAUSE_SRV_CONFLICT = 0x1E; // 业务冲突
	public static final int CAUSE_NOTREL_RECALL = 0x1F; // 由于业务要求,不释放,启动重呼定时器
	public static final int CAUSE_NO_CALL = 0x20; // 呼叫不存在
	public static final int CAUSE_DUP_REG = 0x21; // 重复注册
	public static final int CAUSE_MG_OFFLINE = 0x22; // MG离线
	public static final int CAUSE_DISP_REQ_QUITCALL = 0x23; // 调度员要求退出呼叫
	public static final int CAUSE_DB_ERROR = 0x24; // 数据库操作错误
	public static final int CAUSE_TOOMANY_USER = 0x25; // 太多的用户
	public static final int CAUSE_SAME_USERNUM = 0x26; // 相同的用户号码
	public static final int CAUSE_SAME_USERIPADDR = 0x27; // 相同的固定IP地址
	public static final int CAUSE_PARAM_ERROR = 0x28; // 参数错误
	public static final int CAUSE_SAME_GNUM = 0x29; // 相同的组号码
	public static final int CAUSE_TOOMANY_GROUP = 0x2A; // 太多的组
	public static final int CAUSE_NO_GROUP = 0x2B; // 没有这个组
	public static final int CAUSE_SAME_USERNAME = 0x2C; // 相同的用户名字
	public static final int CAUSE_OAM_OPT_ERROR = 0x2D; // OAM操作错误
	public static final int CAUSE_INVALID_NUM_FORMAT = 0x2E; // 不正确的地址格式
	public static final int CAUSE_MAX = 0x1fff; // *********
	public static final int CAUSE_EXPIRE_IDT = 0x0000; // IDT定时器超时***********
	public static final int CAUSE_EXPIRE_MC = 0x4000; // MC定时器超时
	public static final int CAUSE_EXPIRE_MG = 0x8000; // MG定时器超时

	public UiCauseConstants() {
		super();
	}

	public String getDataType(int uiCause) {
		String uiCauseStatusSummary = "未捕捉";
		switch (uiCause) {
		case CAUSE_ZERO:
			uiCauseStatusSummary = "正常";
			break;
		case CAUSE_UNASSIGNED_NUMBER:
			uiCauseStatusSummary = "未分配号码";
			break;
		case CAUSE_NO_ROUTE_TO_DEST:
			uiCauseStatusSummary = "无目的路由";
			break;
		case CAUSE_USER_BUSY:
			uiCauseStatusSummary = " 用户忙";
			break;
		case CAUSE_ALERT_NO_ANSWER:
			uiCauseStatusSummary = "用户无应答(人不应答)";
			break;
		case CAUSE_CALL_REJECTED:
			uiCauseStatusSummary = "呼叫被拒绝";
			break;
		case CAUSE_ROUTING_ERROR:
			uiCauseStatusSummary = "路由错误";
			break;
		case CAUSE_FACILITY_REJECTED:
			uiCauseStatusSummary = "设备拒绝";
			break;
		case CAUSE_ERROR_IPADDR:
			uiCauseStatusSummary = "错误IP地址过来的呼叫";
			break;
		case CAUSE_NORMAL_UNSPECIFIED:
			uiCauseStatusSummary = "通常,未指定";
			break;
		case CAUSE_TEMPORARY_FAILURE:
			uiCauseStatusSummary = "临时错误";
			break;
		case CAUSE_RESOURCE_UNAVAIL:
			uiCauseStatusSummary = "资源不可用";
			break;
		case CAUSE_INVALID_CALL_REF:
			uiCauseStatusSummary = "不正确的呼叫参考号";
			break;
		case CAUSE_MANDATORY_IE_MISSING:
			uiCauseStatusSummary = "必选信息单元丢失";
			break;
		case CAUSE_TIMER_EXPIRY:
			uiCauseStatusSummary = "定时器超时";
			break;
		case CAUSE_CALL_REJ_BY_USER:
			uiCauseStatusSummary = "被用户拒绝";
			break;
		case CAUSE_CALLEE_STOP:
			uiCauseStatusSummary = "被叫停止";
			break;
		case CAUSE_USER_NO_EXIST:
			uiCauseStatusSummary = "用户不存在";
			break;
		case CAUSE_MS_UNACCESSAVLE:
			uiCauseStatusSummary = "不可接入";
			break;
		case CAUSE_MS_POWEROFF:
			uiCauseStatusSummary = "用户关机";
			break;
		case CAUSE_FORCE_RELEASE:
			uiCauseStatusSummary = "强制拆线";
			break;
		case CAUSE_HO_RELEASE:
			uiCauseStatusSummary = "切换拆线";
			break;
		case CAUSE_CALL_CONFLICT:
			uiCauseStatusSummary = "呼叫冲突";
			break;
		case CAUSE_TEMP_UNAVAIL:
			uiCauseStatusSummary = "暂时无法接通";
			break;
		case CAUSE_AUTH_ERROR:
			uiCauseStatusSummary = "鉴权错误";
			break;
		case CAUSE_NEED_AUTH:
			uiCauseStatusSummary = "需要鉴权";
			break;
		case CAUSE_SDP_SEL:
			uiCauseStatusSummary = "SDP选择错误";
			break;
		case CAUSE_MS_ERROR:
			uiCauseStatusSummary = "媒体资源错误";
			break;
		case CAUSE_INNER_ERROR:
			uiCauseStatusSummary = "内部错误";
			break;
		case CAUSE_PRIO_ERROR:
			uiCauseStatusSummary = "优先级不够";
			break;
		case CAUSE_SRV_CONFLICT:
			uiCauseStatusSummary = "业务冲突";
			break;
		case CAUSE_NOTREL_RECALL:
			uiCauseStatusSummary = "由于业务要求,不释放,启动重呼定时器";
			break;
		case CAUSE_NO_CALL:
			uiCauseStatusSummary = "呼叫不存在";
			break;
		case CAUSE_DUP_REG:
			uiCauseStatusSummary = "重复注册";
			break;
		case CAUSE_MG_OFFLINE:
			uiCauseStatusSummary = "MG离线";
			break;
		case CAUSE_DISP_REQ_QUITCALL:
			uiCauseStatusSummary = "调度员要求退出呼叫";
			break;
		case CAUSE_DB_ERROR:
			uiCauseStatusSummary = "数据库操作错误";
			break;
		case CAUSE_TOOMANY_USER:
			uiCauseStatusSummary = "太多的用户";
			break;
		case CAUSE_SAME_USERNUM:
			uiCauseStatusSummary = "相同的用户号码";
			break;
		case CAUSE_SAME_USERIPADDR:
			uiCauseStatusSummary = "相同的固定IP地址";
			break;
		case CAUSE_PARAM_ERROR:
			uiCauseStatusSummary = "参数错误";
			break;
		case CAUSE_SAME_GNUM:
			uiCauseStatusSummary = "相同的组号码";
			break;
		case CAUSE_TOOMANY_GROUP:
			uiCauseStatusSummary = "太多的组";
			break;
		case CAUSE_NO_GROUP:
			uiCauseStatusSummary = "没有这个组";
			break;
		case CAUSE_SAME_USERNAME:
			uiCauseStatusSummary = "相同的用户名字";
			break;
		case CAUSE_OAM_OPT_ERROR:
			uiCauseStatusSummary = "OAM操作错误";
			break;
		case CAUSE_INVALID_NUM_FORMAT:
			uiCauseStatusSummary = "不正确的地址格式";
			break;
		case CAUSE_EXPIRE_MC:
			uiCauseStatusSummary = "MC定时器超时";
			break;
		case CAUSE_EXPIRE_MG:
			uiCauseStatusSummary = "MG定时器超时";
			break;
		default:
			break;
		}
		return uiCauseStatusSummary;
	}
}
