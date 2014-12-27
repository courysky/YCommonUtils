package com.courysky.ycommonutils.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import android.util.Log;

import com.google.gson.Gson;

public class HttpManager implements HttpHelper.RequestListener{
	private static final String TAG = HttpManager.class.getSimpleName();
	
	
	private static HttpManager sHttpManager;
	private static List<String> sRequestTask;
	private byte[] lock = new byte[0];
	
	private HttpManager() {
		sRequestTask = new ArrayList<String>();
	}
	
	public static HttpManager getInstance() {
		if (null == sHttpManager) {
			sHttpManager = new HttpManager();
		}
		return sHttpManager;
	}
	
	/**
	 * This method is asynchronous.
	 * @param _action
	 * @param _entryStr
	 * @param _observer
	 */
	public void post(final String _action, final String _entryStr, final Observer _observer) {
		Log.v(TAG, "--- post ："+_action);
		synchronized (lock) {
			if (sRequestTask.contains(_action)) {
				Log.w(TAG, "already contain params :"+ _action);
				return ;
			} else {
				Log.i(TAG, "add task :"+_action + " _entryStr :"+_entryStr);
				sRequestTask.add(_action);
			}
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpHelper.getInstance().setRequestListener(sHttpManager);
				HttpHelper.getInstance().post(_action, _entryStr, _observer);
			}
		}).start();
	}
	
	/**
	 * This method is asynchronous.
	 * @param _action
	 * @param _gsonObject
	 * @param _observer
	 */
	public void post(final String _action, final Object _gsonObject, final Observer _observer) {
		Gson gson = new Gson();
		String entryStr = gson.toJson(_gsonObject);
		this.post(_action, entryStr, _observer);
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
	public void onPostOver(String _action) {
		sRequestTask.remove(_action);
	}
}
