package com.courysky.ycommonutils.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import android.text.TextUtils;
import android.util.Log;

import com.courysky.ycommonutils.LogHelper;
import com.google.gson.Gson;

public class HttpManager implements HttpHelper.RequestListener{
	private static final String TAG = HttpManager.class.getSimpleName();
	
	
	private volatile static HttpManager sHttpManager;
	private static Map<String,String> sRequestTask;
	private byte[] lock = new byte[0];
	
	private HttpManager() {
		LogHelper.i(TAG, "---- HttpManager() ----");
		sRequestTask = Collections.synchronizedMap(new HashMap<String, String>());
	}
	
	public static HttpManager getInstance() {
		if (null == sHttpManager) {
			synchronized (HttpManager.class) {
				if (null == sHttpManager) {
					sHttpManager = new HttpManager();
				}
			}
		}
		return sHttpManager;
	}
	
	/**
	 * This method is asynchronous.
	 * @param _action
	 * @param _entryStr
	 * @param _observer
	 * @param _tag , can be empty
	 */
	public void post(final String _action, final String _entryStr, final Observer _observer, final String _tag) {
		LogHelper.v(TAG, "--- post ："+_action+"_tag :"+_tag);
		final String tag ;
		if(TextUtils.isEmpty(_tag)){
			tag = _action;
		} else {
			tag = _tag;
		}
		synchronized (lock) {
			if (sRequestTask.containsKey(tag)) {
				LogHelper.w(TAG, "already contain params :"+ tag+" action :" + _action);
				return ;
			} else {
				LogHelper.i(TAG, "add task :"+_action + " _entryStr :"+_entryStr);
				sRequestTask.put(_action, tag);
			}
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpHelper.getInstance().setRequestListener(sHttpManager);
				HttpHelper.getInstance().post(_action, _entryStr, _observer, tag);
			}
		}).start();
	}
	
	/**
	 * This method is asynchronous.
	 * @param _action
	 * @param _gsonObject
	 * @param _observer
	 * @param _tag
	 */
	public void post(final String _action, final Object _gsonObject, final Observer _observer, final String _tag) {
		Gson gson = new Gson();
		String entryStr = gson.toJson(_gsonObject);
		this.post(_action, entryStr, _observer, _tag);
	}
	/**
	 * This method is asynchronous.
	 * @param _action
	 * @param _gsonObject
	 * @param _observer
	 */
	public void post(final String _action, final Object _gsonObject, final Observer _observer) {
		this.post(_action, _gsonObject, _observer, null);
	}
	
	
	// XXX [yaojian] maybe not useful
	class HttpRequestData {
		String action;
		String entryStr;
		Observer observer;
		@Override
		public boolean equals(Object o) {
			boolean isEqual = false;
			if (null == o) {
				
			} else {
				if (o == this) {
					return true;
				} else if (o instanceof HttpRequestData) {
					if (action.equals( ((HttpRequestData)o).action) ){
						isEqual = true;
					} else {
						
					}
				}
			}
			return isEqual;
		}
		
	}

	@Override
	public void onPostOver(String _action, String _key) {
		String value = sRequestTask.remove(_key);
		LogHelper.i(TAG, "remove :"+value+" _key :"+_key);
	}
}
