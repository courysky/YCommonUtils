package com.courysky.ycommonutils.bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.courysky.ycommonutils.LogHelper;
import com.courysky.ycommonutils.file.FileUtil;
import com.courysky.ycommonutils.net.FileDownloader;
import com.courysky.ycommonutils.ui.UIUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {
	private static final String TAG = AsyncImageLoader.class.getSimpleName();
	
	private byte[] lock = new byte[0];
	private byte[] cacheLock = new byte[0];
	
	private String localCacheDir ;

	private static HashMap<String, SoftReference<Bitmap>> sImageCacheMap;

	private List<String> taskList = new ArrayList<String>();
	
	//TODO [yaojian] shut down
	private ExecutorService executorService;
	
	public AsyncImageLoader() {
		sImageCacheMap = new HashMap<String, SoftReference<Bitmap>>();
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			//TODO [yaojian] 更改cache目录
			localCacheDir = Environment.getExternalStorageDirectory()
					+ File.separator + ".ycommon" + File.separator + "cache";
		} else {
			localCacheDir = Environment.getDownloadCacheDirectory()+File.separator + ".ycommon" + File.separator + "cache";
		}
		executorService = Executors.newFixedThreadPool(12);
	}
	
	/**
	 * @param _path
	 * @param _loadBitmapOverCallback
	 * @return
	 */
	public Bitmap loadImage(final String _path, final LoadBitmapOverCallback _loadBitmapOverCallback) {
		return this.loadImage(_path, 0, 0, 0, _loadBitmapOverCallback, false);
	}
	/**
	 * @param _path
	 * @param _thumbWidth <=0 means return original image
	 * @param _thumbHeight not useful right now
	 * @param _loadBitmapOverCallback
	 * @return
	 */
	public Bitmap loadImage(final String _path,final int _thumbWidth, final int _thumbHeight
			, final LoadBitmapOverCallback _loadBitmapOverCallback) {
		return this.loadImage(_path, _thumbWidth, _thumbHeight, 0, _loadBitmapOverCallback, false);
	}
	

	
	/**
	 * This method is asynchronous
	 * @param _path
	 * @param _thumbWidth <=0 means return original image
	 * @param _thumbHeight not useful right now
	 * @param _loadBitmapOverCallback
	 * @param _isJustFromCache
	 * @return
	 */
	public Bitmap loadImage(final String _path,final int _thumbWidth, final int _thumbHeight
			,final float _degree , final LoadBitmapOverCallback _loadBitmapOverCallback, boolean _isJustFromCache) {
		LogHelper.v(TAG, "--- loadBitmap :"+_path);
		synchronized (lock) {
			// XXX [yaojian] _path + _thumbWidth + _thumbHeight 组成 key
			if (taskList.contains(_path)) {
				LogHelper.i(TAG, "task already contain :"+_path);
				return null;
			} else {
				LogHelper.i(TAG, "add task :"+_path);
				taskList.add(_path);
			}
//			if (sImageCacheMap.size() > 100) {
//				LogHelper.w(TAG, "sImageCacheMap size : "+sImageCacheMap.size());
//				synchronized (cacheLock) {
//					
//					
//					
//					for (SoftReference<Bitmap> reference : sImageCacheMap.values()) {
//						Bitmap bitmap = reference.get();
//						if (bitmap != null && !bitmap.isRecycled()) {
//							bitmap.recycle();
//							bitmap = null;
//						}
//					}
//					sImageCacheMap.clear();
//				}
//				System.gc();
//			}
			
		}
		Bitmap defultBitmap = null;
		if (sImageCacheMap.containsKey(_path)) {
			LogHelper.v(TAG, "sImageCacheMap containsKey :"+_path);
			SoftReference<Bitmap> softReference = sImageCacheMap.get(_path);
			Bitmap cacheBitmap = softReference.get();
			if (null != cacheBitmap && !cacheBitmap.isRecycled()) {
//				if ( !cacheBitmap.isRecycled()) {
					taskList.remove(_path);
					return cacheBitmap;
//				} else {
//					LogHelper.w(TAG, "Bitmap bitmap is recyled !!!");
//					synchronized (cacheLock) {
//						sImageCacheMap.remove(_path);
//					}
//				}
			} else {
				synchronized (cacheLock) {
					sImageCacheMap.remove(_path);
				}
			}
		}
		if(_isJustFromCache){
			taskList.remove(_path);
			return null;
		}
		
		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				LogHelper.d(TAG, "--- handleMessage path:"+_path+" " + " Bitmap :"+msg.obj);
				Bitmap bitmap = (Bitmap)msg.obj;
				
				if (null != _loadBitmapOverCallback) {
					_loadBitmapOverCallback.onLoadBitmapOver(bitmap, _path);
				}
				
				boolean isRemoveSuccess = taskList.remove(_path);
				LogHelper.d(TAG, "remove :" + _path+ " "+isRemoveSuccess);
			}
		};
		
//		new Thread(new LoadBitmapRunnable(_path, _thumbWidth, _thumbHeight, _degree,
//				handler, _loadBitmapOverCallback)).start();
		Runnable thread = new LoadBitmapRunnable(_path, _thumbWidth, _thumbHeight, _degree,
				handler, _loadBitmapOverCallback);
		executorService.execute(thread);
		
		return defultBitmap;
	}
	
	public String getLocalCacheDir() {
		return localCacheDir;
	}

	public void setLocalCacheDir(String localCacheDir) {
		this.localCacheDir = localCacheDir;
	}
	
	private class LoadBitmapRunnable implements Runnable {
		private final String path;
		private final Handler handler;
		private final int thumbWidth;
		private final int thumbHeight;
		private float degree;
		private LoadBitmapOverCallback loadBitmapOverCallback;
		
		public LoadBitmapRunnable(String _path, Handler _handler,LoadBitmapOverCallback callBack) {
			this(_path, 250, 250 ,0 , _handler, callBack);
		}
		
		public LoadBitmapRunnable(String _path,int _thumbWidth, int _thumbHeight, float _degree, Handler _handler, LoadBitmapOverCallback callBack) {
			path = _path;
			handler = _handler;
			loadBitmapOverCallback = callBack;
			thumbWidth = _thumbWidth;
			thumbHeight = _thumbHeight;
			degree = _degree;
		}
		
		@Override
		public void run() {
			LogHelper.v(TAG, "--- LoadBitmapRunnable run path :"+path+" cur thread :"+ Thread.currentThread().getName());
			Bitmap bitmap = null;
			
			try {
				if (path == null) {
					LogHelper.e(TAG, "path is null");
				} else {
				    /*
				     * XXX [yaojian] data: url http://flysnowxf.iteye.com/blog/1271810
				     * http://narutolby.iteye.com/blog/1473568
				     */
					if (path.startsWith("http://")) {
						/** 加载网络图片 */
//						URL myFileUrl = new URL(path);  
//			            HttpURLConnection conn = (HttpURLConnection) myFileUrl  
//			                    .openConnection();  
//			            conn.setDoInput(true);  
//			            conn.connect();  
			            
			            File cacheFile = FileUtil.getInstance().getCacheFile(path, localCacheDir);
			            
			            int index = path.lastIndexOf("/");  
			   		 	String fileName = cacheFile.getName();//path.substring(index + 1); 
			            if (!cacheFile.exists() || cacheFile.length()<1) {
			            	FileDownloader fileUtils = new FileDownloader();
			            	fileUtils.downloadFile(path, localCacheDir, fileName);
			            	
//			            	InputStream is = conn.getInputStream();  
//			            	BufferedOutputStream bos = null;
//			            	bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
//			            	
//			            	byte[] buf = new byte[1024];
//			            	int len = 0;
//			            	while( (len = is.read(buf, 0, len)) != -1) {
//			            		bos.write(buf, 0, len);
//			            	}
//			            	bos.flush();
////			            	bitmap = BitmapFactory.decodeStream(is);  
//			            	is.close();  
//			            	bos.close();
						} else {
							
						}
			            LogHelper.i(TAG, "image download finished." + path);
			            if (cacheFile.exists()) {
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = true;
							
							BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
							
							LogHelper.i(TAG, "bitmap options.height : "+options.outHeight + " options.outWidth :"+ options.outWidth);
							int realWidth = options.outWidth;
							
							
							options.inPurgeable = true;
							options.inInputShareable = true; 
							
							options.inPreferredConfig = Bitmap.Config.RGB_565;
							
							options.inJustDecodeBounds = false;
//							options.outHeight = 300;
//							options.outWidth = 300;
//							options.inJustDecodeBounds = false;
							if (thumbWidth>0) {
								options.inSampleSize = realWidth / thumbWidth;
							}
							InputStream is = new FileInputStream(new File(cacheFile.getAbsolutePath()));
							bitmap = BitmapFactory.decodeStream(is, null, options);

							ExifInterface exif = new ExifInterface(cacheFile.getAbsolutePath());
							String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//							int degree;
							int orientationInt = Integer.parseInt(orientation);
							switch (orientationInt) {
							case ExifInterface.ORIENTATION_NORMAL:
								degree = 0;
								break;
							case ExifInterface.ORIENTATION_ROTATE_90:{
								degree = 90;
								}
								break;
							case ExifInterface.ORIENTATION_ROTATE_180:{
								degree = 180;
							}
								break;
							case ExifInterface.ORIENTATION_ROTATE_270:{
								degree = 270;
							}
								break;
							default:{
								degree = 0;
							}
								break;
							}
//							if (degree != 0) {
//								Matrix matrix = new Matrix();
//								matrix.postRotate(degree);
//								Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,
//										0, 0,
//										bitmap.getWidth(),
//										bitmap.getHeight(), matrix, true);
//								bitmap = rotatedBitmap;
//							}
							
							
							try {
								is.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							LogHelper.e(TAG, "Path does not exist ! "+path);
						}
			            
					} else {
						/** 加载本地图片 */

						File imgFile = new File(path);
						if (imgFile.exists()) {
//							ExifInterface exifInterface = new ExifInterface(path);
//							exifInterface.getAttribute(ExifInterface.)
//							bitmap = Bitmap.createFromPath(path);
							
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = true;
							
							BitmapFactory.decodeFile(path, options);
							
							LogHelper.i(TAG, "bitmap options.height : "+options.outHeight + " options.outWidth :"+ options.outWidth);
							int realWidth = options.outWidth;
							
							options.inPurgeable = true;
							options.inInputShareable = true; 
							
							options.inPreferredConfig = Bitmap.Config.RGB_565;
							
							options.inJustDecodeBounds = false;
//						options.outHeight = 300;
//						options.outWidth = 300;
//						options.inJustDecodeBounds = false;
							

							/**
							 * 若thumb存在则直接读取 TODO[yaojian]图片一次没保存完?
							 */
							StringBuilder thumbDir = new StringBuilder(localCacheDir);
							thumbDir.append(File.separator).append(".thumb").append(thumbWidth).append("x").append(thumbHeight);
							File thumbPath = FileUtil.getInstance().getCacheFile(path, thumbDir.toString());
							if (thumbPath.exists() && thumbPath.length()>0) {

								if (thumbWidth>0) {
									options.inSampleSize = 1;
								}
								InputStream is = new FileInputStream(thumbPath);
								bitmap = BitmapFactory.decodeStream(is, null, options);
								
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {

								if (thumbWidth>0) {
									options.inSampleSize = realWidth / thumbWidth;
								}
								
								
								InputStream is = new FileInputStream(new File(path));
								bitmap = BitmapFactory.decodeStream(is, null, options);
								/**
								 * cache thumbnail  根据压缩情况考虑是否有必要
								 */
								if (null != bitmap && options.inSampleSize != 1) {
									FileUtil.saveBitmap(thumbPath.getAbsolutePath(), bitmap);
								}
								
								
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
							}
							
							
							
						} else {
							LogHelper.e(TAG, "Path does not exist ! "+path);
						}
					}
					LogHelper.d(TAG, "degree :"+degree);
//					UIUtil.rotateBitmap(bitmap, degree);
					Bitmap rotatedBitmap = UIUtil.rotateBitmap(bitmap, degree);
					SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(rotatedBitmap);
					synchronized (cacheLock) {
					sImageCacheMap.put(path, softReference);
					Message msg = handler.obtainMessage(0, rotatedBitmap);
					handler.sendMessage(msg);
					}
				}
				
			} catch (OutOfMemoryError e) {
				LogHelper.e(TAG, " e :"+e.getMessage());
				if(bitmap != null ){
					bitmap = null;
				}
				System.gc();
				taskList.remove(path);
//				Looper.prepare();
//				loadBitmap(path, loadBitmapOverCallback);
//				bitmap = BitmapFactory.decodeFile(path);
				
			}
			catch (FileNotFoundException e) {
				LogHelper.e(TAG, "File not found e:"+e.getMessage());
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public interface LoadBitmapOverCallback{
		public void onLoadBitmapOver(Bitmap _Bitmap, String _imgPath);
	}
	public static HashMap<String, SoftReference<Bitmap>> getsImageCacheMap() {
		return sImageCacheMap;
	}
	
	public class LoadImageTask {
		private String path;
		private int thumbWidth;
		private int thumbHeight;
		public LoadImageTask(String _path,int _thumbWidth,int _thumbHeight) {
			path = _path;
			thumbHeight = _thumbHeight;
			thumbWidth = _thumbWidth;
		}
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public int getThumbWidth() {
			return thumbWidth;
		}
		public void setThumbWidth(int thumbWidth) {
			this.thumbWidth = thumbWidth;
		}
		public int getThumbHeight() {
			return thumbHeight;
		}
		public void setThumbHeight(int thumbHeight) {
			this.thumbHeight = thumbHeight;
		}

		@Override
		public boolean equals(Object o) {
			boolean isEqual = false;
			if (o == this) {
				return true;
			}
			if (o instanceof LoadImageTask) {
				LoadImageTask anotherLoadImageTask = (LoadImageTask) o;
				if (path.endsWith(anotherLoadImageTask.path)
						&& (thumbWidth == anotherLoadImageTask.thumbWidth)
						&& (thumbHeight == anotherLoadImageTask.thumbHeight)) {
					isEqual = true;
				}
				
			}
			return isEqual;
		}
	}
}
