package com.ids.idtma.entity;

public class CalllogEntity {

	private int id;
	private String name;// 名称
	private String number;// 号码
	private String date;// 日期
	private int type;// 来电:1,拨出:2,未接:3
	private int count;// 通话次数

	public static enum CalllogType {
		IN(1), OUT(2), MISSED(3);

		private int value = 0;

		public int value() {
			return this.value;
		}

		private CalllogType(int value) {
			this.value = value;
		}

		public static CalllogType valueOf(int value) {
			switch (value) {
			case 1:
				return IN;
			case 2:
				return OUT;
			case 3:
				return MISSED;
			default:
				return null;
			}
		}
	}

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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
