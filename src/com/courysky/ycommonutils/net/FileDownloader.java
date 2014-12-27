package com.courysky.ycommonutils.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.courysky.ycommonutils.LogHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * @author JianYao
 * 2012/6/1
 */
public class FileDownloader {

	/**下载成功*/
	public static final int DOWN_SUCCESS = 0;
	/**连接错误*/
	public static final int CONNECT_EROOR = 1;
	private static FileDownloader sFileUtils;
	private boolean mIsStopDownload = false;
	
	private OnDownloadOverListerner mOnDownloadOverListerner;
	private OnDownloadingListerner mOnDownloadingListerner;
	private OnDownloadStartListerner mOnDownloadStartListerner;
	
//	private FileUtils() {
//	}
//
//	public static FileUtils getInstance() {
//		if (null == sFileUtils) {
//			sFileUtils = new FileUtils();
//		}
//		return sFileUtils;
//	}

	/**
	 * Down load file to local.
	 * @param originationUrlStr
	 * @param targetPath
	 * @throws IOException
	 */
/*	public void downloadFile(String originationUrlStr, String targetPath)
			throws IOException {
		String filename = originationUrlStr.substring(originationUrlStr
				.lastIndexOf("/") + 1);
		String tempFileName = filename + "_temp";
		File tarFolder = new File(targetPath);
		if (!tarFolder.exists()) {
			tarFolder.mkdirs();
		}
		File tempFile = new File(targetPath+File.separator+tempFileName);
		RandomAccessFile tempAccessFile = new RandomAccessFile(tempFile, "rw");
		
		URL url = new URL(originationUrlStr);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		// 设置连接超时时间
		httpURLConnection.setConnectTimeout(10000);
		// 设置读取数据超时时间
		httpURLConnection.setReadTimeout(15000);
		// 下载的起始位�?
		long startPosition = getDownloadStartPosition(tempAccessFile);
		httpURLConnection.setRequestProperty("Range", "bytes="+startPosition+"-");
		int contentLength = httpURLConnection.getContentLength();
		
		httpURLConnection.connect();
		if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK
				&& httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
			httpURLConnection.disconnect();
			return;
		}
		InputStream inputStream = httpURLConnection.getInputStream();

//		File targetFolder = new File(targetPath+File.separator);
//		if (!targetFolder.exists()) {
//			targetFolder.mkdir();
//		}
		File targetFile = new File(targetPath
				+ File.separator + filename);
		RandomAccessFile targetAccessFile = new RandomAccessFile(targetFile, "rw");
		if(targetAccessFile.length() < contentLength )
		{
			targetAccessFile.seek(startPosition);

			int len = 0;
			byte[] bytes = new byte[1024];
			if (null!=mOnDownloadStartListerner) {
				mOnDownloadStartListerner.onDownLoadStart();
			}
			while((len = inputStream.read(bytes))!=-1)
			{
				targetAccessFile.write(bytes, 0, len);
				startPosition += len;
				tempAccessFile.seek(0);
				tempAccessFile.writeLong(startPosition);
				if (null != mOnDownloadingListerner) {
					mOnDownloadingListerner.onDownloading();
				}
			}
			if (len == -1) {
				tempAccessFile.close();
				targetAccessFile.close();
				tempFile.delete();
				if (null!=mOnDownloadOverListerner) {
					mOnDownloadOverListerner.onDownloadOver();
				}
			}
		} else {
			if (null!=mOnDownloadOverListerner) {
				mOnDownloadOverListerner.onDownloadOver();
			}
		}
	}*/
	
	public void stopDownload() {
		mIsStopDownload = true;
	}
	
	/**
	 * Down load file to local.
	 * @param originationUrlStr
	 * @param targetPathStr
	 * @param filename
	 * @throws IOException
	 */
	public void downloadFile(String originationUrlStr, String targetPathStr, String filename)
			throws IOException {
		LogHelper.v("Download","origination add :"+originationUrlStr);
		LogHelper.v("Download","local add :"+targetPathStr+filename);
		
		/**
		 * 验证文件名是否合法
		 */
		String tempFileName = filename + "_info";
		File tarFolder = new File(targetPathStr);
		if (!tarFolder.exists()) {
			tarFolder.mkdirs();
		}
		File tempFile = new File(targetPathStr+File.separator+tempFileName);
		File targetFile = new File(targetPathStr
				+ File.separator + filename+"_tmp");
		if (!tempFile.exists()) {
			if (targetFile.exists()) {
				targetFile.delete();
			}
		}
		RandomAccessFile tempAccessFile = new RandomAccessFile(tempFile, "rw");
		
		URL url = new URL(originationUrlStr);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		// 设置连接超时时间
		httpURLConnection.setConnectTimeout(10000);
		// 设置读取数据超时时间
		httpURLConnection.setReadTimeout(90000);
		// 下载的起始位�?
		long startPosition = 0;
//		if(tempAccessFile.length() != 0)
//		{
			startPosition = getDownloadStartPosition(tempAccessFile);
//		}
		httpURLConnection.setRequestProperty("Range", "bytes="+startPosition+"-");
		int contentLength = httpURLConnection.getContentLength();
		
		httpURLConnection.connect();
		if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK
				&& httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
			httpURLConnection.disconnect();
			return;
		}
		InputStream inputStream = httpURLConnection.getInputStream();

//		File targetFolder = new File(targetPathStr+File.separator);
//		if (!targetFolder.exists()) {
//			targetFolder.mkdir();
//		}
		
		RandomAccessFile targetAccessFile = new RandomAccessFile(targetFile, "rw");
		if(targetAccessFile.length() < contentLength)
		{
			targetAccessFile.seek(startPosition);

			int len = 0;
			byte[] bytes = new byte[1024];
			if (null!=mOnDownloadStartListerner) {
				mOnDownloadStartListerner.onDownLoadStart();
			}
			while((len = inputStream.read(bytes))!=-1 && !mIsStopDownload)
			{
				targetAccessFile.write(bytes, 0, len);
				startPosition += len;
				tempAccessFile.seek(0);
				tempAccessFile.writeLong(startPosition);
				if (null != mOnDownloadingListerner) {
					mOnDownloadingListerner.onDownloading((int)startPosition, contentLength);
				}
			}
			if (len == -1) {
				tempAccessFile.close();
				targetAccessFile.close();
				targetFile.renameTo(new File(targetPathStr + File.separator + filename));
				tempFile.delete();
				if (null!=mOnDownloadOverListerner) {
					mOnDownloadOverListerner.onDownloadOver();
				}
			}
		} else {
			tempFile.delete();
			if (null!=mOnDownloadOverListerner) {
				mOnDownloadOverListerner.onDownloadOver();
			}
		}
	}
	
	/**
	 * @return
	 * @throws IOException 
	 */
	private long getDownloadStartPosition(RandomAccessFile raf) throws IOException {
		long startPositionL = 0;
		raf.seek(0);
		if (raf.length() != 0) {
			startPositionL = raf.readLong();
		}
		return startPositionL;
	}
	
	public Bitmap downloadBitmap(String originationUrlStr) throws IOException {
		Bitmap bitmap = null ;
		InputStream inputStream;
		URL url;
		HttpURLConnection httpURLConnection;
		url = new URL(originationUrlStr);
		httpURLConnection = (HttpURLConnection) url.openConnection();
		inputStream = httpURLConnection.getInputStream();
		bitmap = BitmapFactory.decodeStream(inputStream);
		inputStream.close();
		return bitmap;
	}
	
	public void setmOnDownloadStartListerner(
			OnDownloadStartListerner mOnDownloadStartListerner) {
		this.mOnDownloadStartListerner = mOnDownloadStartListerner;
	}
	public void setmOnDownloadingListerner(
			OnDownloadingListerner mOnDownloadingListerner) {
		this.mOnDownloadingListerner = mOnDownloadingListerner;
	}
	public void setmOnDownloadOverListerner(
			OnDownloadOverListerner mOnDownloadOverListerner) {
		this.mOnDownloadOverListerner = mOnDownloadOverListerner;
	}
	
	public interface OnDownloadOverListerner{
		abstract void onDownloadOver();
	}
	
	public interface OnDownloadingListerner{
		abstract void onDownloading(int downSize, int totalSize);
	}

	public interface OnDownloadStartListerner{
		abstract void onDownLoadStart();
	}
}
