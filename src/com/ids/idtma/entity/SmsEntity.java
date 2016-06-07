package com.ids.idtma.entity;

public class SmsEntity {
    //电话号码
	private String phone_number;
	//内容
	private String sms_content;
	//信息条数
	private int sms_count;
	//0:all 1:inBox 2:sent 3:draft4:outBox 5:failed
	private int sms_type;
	//创建时间
	private String create_time;
	//是否已读
    //0:not read 1:read; default is 0
	private int read;
	private String sms_resource_url;
	//0:not  1:text 2:gps position 3:image 4:voice or weixin 4:vedio
	private int sms_resource_type;
	private String sms_resource_name;
	private int sms_resource_time_length;
	private int sms_resource_rs_ok;
	private String target_phone_number;
	private String owner_phone_number;
	private int is_group_message;
	private int uiCause;
	//id
	private int _id;
	//layout的id
	private int layoutID;

	public SmsEntity() {
	}

	public int get_id() {
		return _id;
	}

	public int getSms_resource_type() {
		return sms_resource_type;
	}

	public void setSms_resource_type(int sms_resource_type) {
		this.sms_resource_type = sms_resource_type;
	}

	
	public int getSms_resource_time_length() {
		return sms_resource_time_length;
	}

	public void setSms_resource_time_length(int sms_resource_time_length) {
		this.sms_resource_time_length = sms_resource_time_length;
	}

	public String getSms_resource_name() {
		return sms_resource_name;
	}

	public void setSms_resource_name(String sms_resource_name) {
		this.sms_resource_name = sms_resource_name;
	}

	public String getSms_resource_url() {
		return sms_resource_url;
	}

	public void setSms_resource_url(String sms_resource_url) {
		this.sms_resource_url = sms_resource_url;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getSms_content() {
		return sms_content;
	}

	public void setSms_content(String sms_content) {
		this.sms_content = sms_content;
	}

	public int getSms_count() {
		return sms_count;
	}

	public void setSms_count(int sms_count) {
		this.sms_count = sms_count;
	}

	public int getSms_type() {
		return sms_type;
	}

	public void setSms_type(int sms_type) {
		this.sms_type = sms_type;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}
	
	public int getSms_resource_rs_ok() {
		return sms_resource_rs_ok;
	}

	public void setSms_resource_rs_ok(int sms_resource_rs_ok) {
		this.sms_resource_rs_ok = sms_resource_rs_ok;
	}
	
	public String getTarget_phone_number() {
		return target_phone_number;
	}

	public void setTarget_phone_number(String target_phone_number) {
		this.target_phone_number = target_phone_number;
	}

	public int getLayoutID() {
		return layoutID;
	}

	public void setLayoutID(int layoutID) {
		this.layoutID = layoutID;
	}

	public String getOwner_phone_number() {
		return owner_phone_number;
	}

	public void setOwner_phone_number(String owner_phone_number) {
		this.owner_phone_number = owner_phone_number;
	}

	public int getIs_group_message() {
		return is_group_message;
	}

	public void setIs_group_message(int is_group_message) {
		this.is_group_message = is_group_message;
	}

	public int getUiCause() {
		return uiCause;
	}

	public void setUiCause(int uiCause) {
		this.uiCause = uiCause;
	}
	
}
