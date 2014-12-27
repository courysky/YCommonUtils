package com.courysky.ycommonutils.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;

import com.courysky.ycommonutils.LogHelper;

import android.util.Log;

public class HttpHelper extends Observable{
	private static final String TAG = HttpHelper.class.getSimpleName();
	private static HttpHelper sHelper;
	
	private HttpClient mHttpClient;
	private RequestListener mRequestListener;

//	public enum Action {
//		login, register, trackDescription
//		
//	}
	
	private HttpHelper() {
		mHttpClient = new DefaultHttpClient();
	}
	public static HttpHelper getInstance() {
		if (null == sHelper) {
			sHelper = new HttpHelper();
		}
		return sHelper;
	}
	
	/**
	 * TODO [yaojian ] we may return void
	 * @param _action
	 * @param _entryStr
	 * @param _observer
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String post(String _action, String _entryStr, Observer _observer) {
		if (null != _observer) {
//			observerList.add(_observer);
			addObserver(_observer);
			
		}
		String responseContent = null;
		int statusCode = -1;
		try {
			HttpPost httpPost = new HttpPost(_action);//_action
			if (null != _entryStr && !_entryStr.equals("")) {
				StringEntity stringEntity;
				stringEntity = new StringEntity(_entryStr, HTTP.UTF_8);
				httpPost.setEntity(stringEntity);
			}
			LogHelper.i(TAG, httpPost.getURI().toString());
			// 请求超时
			mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
            // 读取超时
//			mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000    );
			HttpResponse response = mHttpClient.execute(httpPost);
			if (null!= response ){
				statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					/**
					 * 处理返回数据
					 */
					HttpEntity entity = response.getEntity();
					BufferedReader reader =  new BufferedReader(
							new InputStreamReader(entity.getContent(), HTTP.UTF_8));
					StringBuffer buffer = new StringBuffer();
					String line = "";
					while( (line = reader.readLine()) != null ) {
						buffer.append(line);
					}
					responseContent = buffer.toString();
					LogHelper.i(TAG,"ResContent :"+responseContent);
				} else {
					
				}
			} else {
				
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			try {
				this.setChanged();
				this.notifyObservers(new HttpResponseContent(_action, responseContent, statusCode));
				this.deleteObserver(_observer);
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			} finally {
				if (null != mRequestListener) {
					mRequestListener.onPostOver(_action);
				}
//			}
		}
		return responseContent;
	}
	
	public class HttpResponseContent {
		private int statusCode;
		private String action;
		private String responseText;
		
		public HttpResponseContent(String _action, String _responseText, int _statusCode) {
			action = _action;
			responseText = _responseText;
			statusCode = _statusCode;
		}
		
		public String getAction() {
			return action;
		}
		public void setAction(String action) {
			this.action = action;
		}
		
		public String getResponseText() {
			return responseText;
		}
		public void setResponseText(String responseText) {
			this.responseText = responseText;
		}
		
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
	}
	
	public RequestListener getRequestListener() {
		return mRequestListener;
	}
	public void setRequestListener(RequestListener mRequestListener) {
		this.mRequestListener = mRequestListener;
	}
	
	protected interface RequestListener {
		void onPostOver(String _action);
	}
}

