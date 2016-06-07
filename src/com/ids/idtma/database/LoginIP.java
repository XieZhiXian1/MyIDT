package com.ids.idtma.database;

public class LoginIP {
	public int _id;
	public String ip_custom_name;
	public String ip_address;
	public int ip_port;

	public LoginIP() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoginIP(int _id, String ip_custom_name, String ip_address, int ip_port) {
		super();
		this._id = _id;
		this.ip_custom_name = ip_custom_name;
		this.ip_address = ip_address;
		this.ip_port = ip_port;
	}
	
	public LoginIP(String ip_custom_name, String ip_address, int ip_port) {
		super();
		this.ip_custom_name = ip_custom_name;
		this.ip_address = ip_address;
		this.ip_port = ip_port;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getIp_custom_name() {
		return ip_custom_name;
	}

	public void setIp_custom_name(String ip_custom_name) {
		this.ip_custom_name = ip_custom_name;
	}

	public String getIp_address() {
		return ip_address;
	}

	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}

	public int getIp_port() {
		return ip_port;
	}

	public void setIp_port(int ip_port) {
		this.ip_port = ip_port;
	}

}
