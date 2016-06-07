package com.ids.idtma.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;

public class DateUtil {

	/**
	 * 时间格式匹配模式 yyyy-MM-dd HH:mm:ss
	 */
	public static final String TIME_PATTERN_0 = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 时间格式匹配模式 HH:mm:ss
	 */
	public static final String TIME_PATTERN_1 = "HH:mm:ss";

	/**
	 * 时间格式匹配模式 yyyyMMddHHmmss
	 */
	public static final String TIME_PATTERN_2 = "yyyyMMddHHmmss";

	/**
	 * 时间格式匹配模式 yyyy-MM-dd
	 */
	public static final String TIME_PATTERN_3 = "yyyy-MM-dd";

	/**
	 * 匹配时间格式 yyyy-MM-dd HH:mm
	 */
	public static final String TIME_PATTERN_4 = "yyyy-MM-dd HH:mm";
	/**
	 * 匹配时间格式 MM/dd HH:mm
	 */
	public static final String TIME_PATTERN_5 = "MM/dd HH:mm";
	
	public static final String TIME_PATTERN_6 = "yyyy-MM-dd-HH-mm-ss";

	public static String dateToString(long time, String pattern) {
		return dateToString(new Date(time), pattern);
	}

	@SuppressLint("SimpleDateFormat")
	public static String dateToString(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	@SuppressLint("SimpleDateFormat")
	public static Date stringToDate(String time, String pattern)
			throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.parse(time);
	}

	public static String secondToString(long second, String pattern) {
		long hour = second / 3600;
		long temp = hour * 3600;
		long min = (second - temp) / 60;
		temp = temp + min * 60;
		long _second = second - temp;
		return pattern.replace("_h", String.valueOf(hour))
				.replace("_m", String.valueOf(min))
				.replace("_s", String.valueOf(_second));
	}

	public static String timeToStr(int num) {
		return num > 9 ? num + "" : "0" + num;
	}

	public static final String FORMAT_SECONDS = "yyyy-MM-dd HH:mm:ss";

	public static final String FORMAT_DAY = "yyyy-MM-dd";

	public static final long ONE_DAY_MILLISECOND = 24 * 60 * 60 * 1000;

	public static Date parseDate(String s) {
		DateFormat df = DateFormat.getDateInstance();
		Date myDate = null;
		try {
			myDate = df.parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return myDate;
	}

	/**
	 * 获取指定日期的当前星期（中国, 如：星期日,星期一,星期二）
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前日期
	 */
	public static String getWeekCS(String sDate) {
		Calendar c = GregorianCalendar.getInstance();
		if (!(sDate == null || sDate.equals(""))) {
			Date date = formatDateStr(sDate, "yyyy-MM-dd");
			c.setTime(date);
		}
		c.setFirstDayOfWeek(Calendar.SUNDAY);
		String[] s = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		return s[c.get(Calendar.DAY_OF_WEEK) - 1];
	}

	/**
	 * 对传入的时间进行格式化
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String formatDate(Date time, String format) {
		if (time == null) {
			time = new Date();
		}
		// 获取当前time
		if (format == null || format.equals("")) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.CHINA);
		String dateString = formatter.format(time);
		return dateString;
	}

	/**
	 * 对传入的时间字串进行格式化，并返回一个Date对象
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间
	 */
	public static Date formatDateStr(String time, String format) {
		if (time == null || time.equals("")) {
			// 获取当前time
			time = formatDate(null, null);
		}
		if (format == null || format.equals("")) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.CHINA);
		ParsePosition pos = new ParsePosition(0);// 从0开始解析
		Date strtodate = formatter.parse(time, pos);
		return strtodate;
	}

	/**
	 * 对传入的时间字串进行格式化
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyy-MM-dd HH:mm:ss"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String formatDateStrToStr(String time, String format) {
		return formatDate(formatDateStr(time, format), format);
	}

	/**
	 * 获取指定日期的中国日期（yyyy年MM月dd日）
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的日期字串
	 */
	public static String getDateCS(Date time) {
		return formatDate(time, "yyyy年MM月dd日");
	}

	/**
	 * 获取指定时间的长字符串形式 "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的时间字串
	 */
	public static String getLongStr(Date time) {
		return formatDate(time, null);
	}

	/**
	 * 获得d天后的现在时刻；长字符串形式 "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param d
	 *            //d天后
	 * @return 格式化后的时间字串
	 */
	public static String getLongStr(int d) {
		Calendar c = GregorianCalendar.getInstance();
		c.add(Calendar.DATE, d);// 获得d天后的现在时刻
		Date time = c.getTime();
		return formatDate(time, null);
	}

	/**
	 * 获取指定时间的短字符串形式 "yyyy-MM-dd"
	 * 
	 * @param time
	 *            要格式化的时间，如果为null则取当前时间
	 * @return 格式化后的时间字串
	 */
	public static String getShortStr(Date time) {
		return formatDate(time, "yyyy-MM-dd");
	}

	/**
	 * 获取指定时间的短字符串形式
	 * 
	 * @param time
	 *            要格式化的时间，如果为空则取当前时间
	 * @param format
	 *            返回值的形式，为空则按"yyyyMMdd"格式化时间
	 * @return 格式化后的时间字串
	 */
	public static String getDateStr(Date time, String format) {
		if (time == null) {
			time = new Date();
		}
		if (format == null || format.equals("")) {
			format = "yyyyMMdd";
		}
		return formatDate(time, format);
	}

	/**
	 * 将字符串转换为一般时间的长格式:yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getLongDate(String strDate) {
		return formatDateStr(strDate, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 将字符串转换为一般时间的短格式;yyyy-MM-dd
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getShortDate(String strDate) {
		return formatDateStr(strDate, "yyyy-MM-dd");
	}

	/**
	 * 将字符串转换为一般时间的短格式;yyyy-MM-dd
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static Date getDate(String strDate, String format) {
		return formatDateStr(strDate, format);
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 * 
	 * @param strDate
	 *            要格式化的时间，如果为空则取当前时间
	 * @return 格式化后的时间
	 */
	public static String getStrTimeShort(String strDate) {
		return formatDateStrToStr(strDate, "HH:mm:ss");
	}

	/**
	 * 将日期字符串加天数转换成新日期字符串
	 * 
	 * @param strDate
	 *            原日期字符串:
	 * @param days
	 *            增加的天数
	 * @return (yyyy-M-d)添加后的时间
	 */
	public static String addDate(String strDate, int days) {
		String tmpDate = formatDateStrToStr(strDate, "yyyy-MM-dd");
		String[] date = tmpDate.split("-"); // 将要转换的日期字符串拆分成年月日
		int year, month, day;
		year = Integer.parseInt(date[0]);
		month = Integer.parseInt(date[1]) - 1;
		day = Integer.parseInt(date[2]);
		GregorianCalendar d = new GregorianCalendar(year, month, day);
		d.add(Calendar.DATE, days);
		Date dd = d.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String adddate = df.format(dd);
		return adddate;
	}

	/**
	 * //获取当前的时间
	 */
	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * //获取昨日日期
	 */
	public static String getYesterdayStr() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return getDateStr(cal.getTime(), FORMAT_DAY);
	}

	/**
	 * //获取指定日期的下一日
	 */
	public static Date getNextDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.getTime() + ONE_DAY_MILLISECOND);
		return cal.getTime();
	}

	/**
	 * 用当前日期作为文件名,一般不会重名 取到的值是从当前时间的字符串格式,带有毫秒,建议作为记录id 。一秒是一千毫秒。
	 * 如：20080403212345508
	 */
	public static String getDateId() {
		return formatDate(null, "yyyyMMddHHmmssSSS");
	}

	/**
	 * 用当前日期作为文件名,一般不会重名 取到的值是从当前时间的字符串格式。 如：20080403212345508
	 */
	public static String getDateDir() {
		return formatDate(null, "yyyyMMddHHmmss");
	}

	/**
	 * 提取一个月中的最后一天
	 * 
	 * @param day
	 * @return
	 */
	public static Date getLastDate(long day) {
		Date date = new Date();
		long date_3_hm = date.getTime() - 3600000 * 34 * day;
		Date date_3_hm_date = new Date(date_3_hm);
		return date_3_hm_date;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return 字符串 yyyy-MM-dd
	 */
	public static String getStringToday() {
		return formatDate(null, "yyyy-MM-dd");
	}

	/**
	 * 得到现在小时
	 */
	public static String getHour() {
		String dateString = formatDate(null, "yyyy-MM-dd HH:mm:ss");
		String hour = dateString.substring(11, 13);
		return hour;
	}

	/**
	 * 得到现在分钟
	 * 
	 * @return
	 */
	public static String getMinute() {
		String dateString = formatDate(null, "yyyy-MM-dd HH:mm:ss");
		String min = dateString.substring(14, 16);
		return min;
	}

	/**
	 * 二个小时时间间的差值,必须保证二个时间都是"HH:mm"的格式，返回字符型的分钟
	 */
	public static String getTwoHour(String st1, String st2) {
		String[] kk = null;
		String[] jj = null;
		kk = st1.split(":");
		jj = st2.split(":");
		if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
			return "0";
		} else {
			double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1])
					/ 60;
			double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1])
					/ 60;
			if ((y - u) > 0) {
				return y - u + "";
			} else {
				return "0";
			}
		}
	}

	/**
	 * 得到二个日期间的间隔天数
	 */
	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		long day = 0;
		try {
			java.util.Date date = myFormatter.parse(sj1);
			java.util.Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return "";
		}
		return day + "";
	}

	/**
	 * 时间前推或后推分钟,其中JJ表示分钟.
	 * 
	 * @param sj1
	 *            指定时间
	 * @param jj
	 *            前推或后推分钟,其中JJ表示分钟
	 * @return
	 */
	public static String getPreTime(String sj1, String jj) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.CHINA);
		String mydate1 = "";
		try {
			Date date1 = format.parse(sj1);
			long Time = (date1.getTime() / 1000) + Integer.parseInt(jj) * 60;
			date1.setTime(Time * 1000);
			mydate1 = format.format(date1);
		} catch (Exception e) {
		}
		return mydate1;
	}

	/**
	 * 判断是否润年
	 * 
	 * @param ddate
	 * @return
	 */
	public static boolean isLeapYear(String ddate) {
		/**
		 * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
		 * 3.能被4整除同时能被100整除则不是闰年
		 */
		Date d = formatDateStr(ddate, "yyyy-MM-dd");
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(d);
		int year = gc.get(Calendar.YEAR);
		if ((year % 400) == 0) {
			return true;
		} else if ((year % 4) == 0) {
			if ((year % 100) == 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * 获取一个月的最后一天
	 * 
	 * @param date
	 * @return
	 */
	public static String getEndDateOfMonth(String date) {
		String str = formatDateStrToStr(date, "yyyy-MM-dd");
		str = date.substring(0, 8);
		String month = date.substring(5, 7);
		int mon = Integer.parseInt(month);
		if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8
				|| mon == 10 || mon == 12) {
			str += "31";
		} else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
			str += "30";
		} else {
			if (isLeapYear(date)) {
				str += "29";
			} else {
				str += "28";
			}
		}
		return str;
	}

	/**
	 * 判断二个时间是否在同一个周
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameWeekDates(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		if (0 == subYear) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2
					.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
			// 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2
					.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		} else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
			if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2
					.get(Calendar.WEEK_OF_YEAR)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 产生周序列,即得到当前时间所在的年度是第几周
	 * 
	 * @return
	 */
	public static String getYearWeek() {
		Calendar c = Calendar.getInstance();
		// Calendar c = Calendar.getInstance(Locale.CHINA);
		String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
		if (week.length() == 1) {
			week = "0" + week;
		}
		String year = Integer.toString(c.get(Calendar.YEAR));
		return year + week;
	}

	/**
	 * 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号
	 * 
	 * @param sdate
	 * @param num
	 *            0-6，分别表示周一到周六
	 * @return
	 */
	public static String getWeek(String sdate, String num) {
		// 再转换为时间
		Date dd = formatDateStr(sdate, "yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(dd);
		if (num.equals("1")) // 返回星期一所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else if (num.equals("2")) // 返回星期二所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		} else if (num.equals("3")) // 返回星期三所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		} else if (num.equals("4")) // 返回星期四所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		} else if (num.equals("5")) // 返回星期五所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		} else if (num.equals("6")) // 返回星期六所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		} else if (num.equals("0")) // 返回星期日所在的日期
		{
			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		}
		return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(c
				.getTime());
	}

	/**
	 * 两个时间之间的天数
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static long getDays(String date1, String date2) {
		if (date1 == null || date1.equals("")) {
			return 0;
		}
		if (date2 == null || date2.equals("")) {
			return 0;
		}
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CHINA);
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	/**
	 * 取得数据库主键 生成格式为yyyyMMddhhmmssSSS+k位随机数
	 * 
	 * @param k
	 *            表示是取几位随机数，可以自己定
	 */
	public static String getSerialNo(int k) {
		return getDateId() + getRandom(k);
	}

	private static long seed = 1;

	/**
	 * 返回一个随机数
	 * 
	 * @param i
	 *            表示是取几位随机数，可以自己定
	 * @return
	 */
	public static String getRandom(int i) {
		Random jjj = new Random(seed);
		seed++;
		String jj = "";
		for (int k = 0; k < i; k++) {
			jj += jjj.nextInt(9);
		}
		return jj;
	}

	/**
	 * 
	 * @param args
	 */
	public static boolean isRightDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",
				Locale.CHINA);
		if (date == null || date.equals("")) {
			return false;
		}
		if (date.length() > 10) {
			sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA);
		} else {
			sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		}
		try {
			Date dt = sdf.parse(date);
			return dt == null ? false : true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/***************************************************************************
	 * //nd=1表示返回的值中包含年度 //yf=1表示返回的值中包含月份 //rq=1表示返回的值中包含日期 //format表示返回的格式 1
	 * 以年月日中文返回 2 以横线-返回 // 3 以斜线/返回 4 以缩写不带其它符号形式返回 // 5 以点号.返回
	 **************************************************************************/
	public static String getStringDateMonth(String sdate, String nd, String yf,
			String rq, String format) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",
				Locale.US);
		String dateString = formatter.format(currentTime);
		String s_nd = dateString.substring(0, 4); // 年份
		String s_yf = dateString.substring(5, 7); // 月份
		String s_rq = dateString.substring(8, 10); // 日期
		String sreturn = "";
		if (sdate == null || sdate.equals("") || !isRightDate(sdate)) { // 处理空值情况
			if (nd.equals("1")) {
				sreturn = s_nd;
				// 处理间隔符
				if (format.equals("1")) {
					sreturn = sreturn + "年";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理月份
			if (yf.equals("1")) {
				sreturn = sreturn + s_yf;
				if (format.equals("1")) {
					sreturn = sreturn + "月";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理日期
			if (rq.equals("1")) {
				sreturn = sreturn + s_rq;
				if (format.equals("1")) {
					sreturn = sreturn + "日";
				}
			}
		} else {
			// 是一个合法的日期值，则先将其转换为标准的时间格式
			sdate = formatDateStrToStr(sdate, "yyyy-MM-dd");
			s_nd = sdate.substring(0, 4); // 年份
			s_yf = sdate.substring(5, 7); // 月份
			s_rq = sdate.substring(8, 10); // 日期
			if (nd.equals("1")) {
				sreturn = s_nd;
				// 处理间隔符
				if (format.equals("1")) {
					sreturn = sreturn + "年";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理月份
			if (yf.equals("1")) {
				sreturn = sreturn + s_yf;
				if (format.equals("1")) {
					sreturn = sreturn + "月";
				} else if (format.equals("2")) {
					sreturn = sreturn + "-";
				} else if (format.equals("3")) {
					sreturn = sreturn + "/";
				} else if (format.equals("5")) {
					sreturn = sreturn + ".";
				}
			}
			// 处理日期
			if (rq.equals("1")) {
				sreturn = sreturn + s_rq;
				if (format.equals("1")) {
					sreturn = sreturn + "日";
				}
			}
		}
		return sreturn;
	}

	public static String getNextMonthDay(String sdate, int m) {
		sdate = formatDateStrToStr(sdate, "yyyy-MM-dd");
		int year = Integer.parseInt(sdate.substring(0, 4));
		int month = Integer.parseInt(sdate.substring(5, 7));
		month = month + m;
		if (month < 0) {
			month += 12;
			year--;
		} else if (month > 12) {
			month -= 12;
			year++;
		}
		String smonth = "";
		if (month < 10) {
			smonth = "0" + month;
		} else {
			smonth = "" + month;
		}
		return year + "-" + smonth + "-01";
	}

	/**
	 * 得到当前服务器时间
	 * 
	 * @return 字符串 yyyyMMdd
	 */
	public static String getcurrenttime() {
		String datatime = new SimpleDateFormat("yyyyMMdd", Locale.US)
				.format(Calendar.getInstance().getTime());
		return datatime;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// System.out.println(getDateDir());
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		System.out.println("The YEAR is: " + calendar.get(Calendar.YEAR));
		System.out.println("The MONTH is: " + calendar.get(Calendar.MONTH));
		System.out.println("The DAY is: " + calendar.get(Calendar.DATE));
		System.out.println("The HOUR is: " + calendar.get(Calendar.HOUR));
		System.out.println("The MINUTE is: " + calendar.get(Calendar.MINUTE));
		System.out.println("The SECOND is: " + calendar.get(Calendar.SECOND));
		System.out.println("The AM_PM indicator is: "
				+ calendar.get(Calendar.AM_PM));
		System.out.println(getDateStr(new Date(), "yyyyMMddww"));
		System.out.println(getYearWeek());
		System.out.println(new Date(2011 - 1900, 5, 18));
		System.out.println(isSameWeekDates(new Date(), new Date(2011, 5, 18)));
	}

}
