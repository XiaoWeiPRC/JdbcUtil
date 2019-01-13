package com.asniie.utils;

/*
 * Created by XiaoWei on 2019/1/9.
 */
public class LogUtil {
	public static String TAG = "LogUtil : ";

	public static void debug(Object obj) {
		if (obj instanceof Throwable) {
			Throwable throwable = (Throwable) obj;
			throwable.printStackTrace();
		} else {
			System.out.println(TAG + format(obj));
		}
	}

	private static String format(Object obj) {
		if (obj == null) {
			return "你传入了一个Null";
		}

		return String.valueOf(obj);
	}
}
