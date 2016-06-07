package com.ids.idtma.entity;

import java.util.ArrayList;

public class GroupEntity {

	private int id;
	private String name;
	private ArrayList<ContactsEntity> contact;
	private int count;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ContactsEntity> getContact() {
		return contact;
	}

	public void setContact(ArrayList<ContactsEntity> contact) {
		this.contact = contact;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
