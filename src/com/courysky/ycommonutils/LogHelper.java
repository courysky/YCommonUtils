package com.courysky.ycommonutils;

import android.util.Log;

/**
 * @author Courysky E-mail:courysky@live.cn
 * version create time :2014年12月27日 下午5:11:48
 */
public class LogHelper {
	private static boolean isDebug = true;


	public static void v(String tag, String msg) {
		if (isDebug) {
			Log.v(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			Log.i(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug) {
			Log.d(tag, msg);
		}
	}
	
	public static void w(String tag, String msg) {
		if (isDebug) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			Log.e(tag, msg);
		}
	}

	public static void wtf(String tag, String msg) {
		if (isDebug) {
			Log.wtf(tag, msg);
		}
	}
	
	public static boolean isDebug() {
		return isDebug;
	}

	public static void setDebug(boolean isDebug) {
		LogHelper.isDebug = isDebug;
	}
}
