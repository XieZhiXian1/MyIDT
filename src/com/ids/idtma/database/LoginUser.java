package com.ids.idtma.database;

public class LoginUser {
	public int _id;
	public String userphone;
	public String password;

	public LoginUser(int _id, String userphone, String password) {
		super();
		this._id = _id;
		this.userphone = userphone;
		this.password = password;
	}
	
	public LoginUser(String userphone, String password) {
		super();
		this.userphone = userphone;
		this.password = password;
	}

	public LoginUser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}


	public String getUserphone() {
		return userphone;
	}

	public void setUserphone(String userphone) {
		this.userphone = userphone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
