package com.courysky.ycommonutils.bitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.courysky.ycommonutils.LogHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageDisplayManager {
	private static final String TAG = AsyncImageDisplayManager.class.getSimpleName();
	private AsyncImageLoader asyncImageLoader ;
	private String localCacheDir ;
	
	/**
	 * 映射图对应要显示的IamgeView列表
	 */
	private HashMap<String, List<ImageViewSetter>> imageDisplayMap = new HashMap<String, List<ImageViewSetter>>();
	
	public AsyncImageDisplayManager() {
		asyncImageLoader = new AsyncImageLoader();
	}
	/**
	 * This method is asynchronous
	 * @param imageView
	 * @param path
	 * @param bitmapWidth
	 * @param bitmapHeight
	 * @param degree
	 * @param parentView
	 */
	public void displayImage(final ImageView imageView, final String path,
			final int bitmapWidth, final int bitmapHeight, final float degree) {
//		final Object tagPath = imageView.getTag();
//		if (null == tagPath) {
//			imageView.setTag(path);
//		}

//		if (null == imageView.getTag()) {
			imageView.setTag(path);
//		}
		final String tagPath = (String) imageView.getTag();
		LogHelper.d(TAG, "tagPath :"+ tagPath+" || path :"+path);

		
		if (imageDisplayMap.containsKey(path)) {
			ImageViewSetter imageViewSetter = new ImageViewSetter(imageView, degree);
			imageDisplayMap.get(path).add(imageViewSetter);
		} else {
			List<ImageViewSetter> imageViewList = new ArrayList<ImageViewSetter>();
			ImageViewSetter imageViewSetter = new ImageViewSetter(imageView, degree);
			imageViewList.add(imageViewSetter);
			imageDisplayMap.put(path, imageViewList);
		}

		if (null == localCacheDir || localCacheDir.equals("")) {
			Context context = imageView.getContext();
			File cacheDir ;
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				cacheDir = context.getExternalCacheDir();
			} else {
				cacheDir = context.getCacheDir();
			}
			File imgCacheDir = new File(cacheDir, ".image");
			setLocalCacheDir(imgCacheDir.getAbsolutePath());
		}
		Bitmap bitmap = asyncImageLoader.loadImage(path, bitmapWidth, bitmapHeight,degree,
				new AsyncImageLoader.LoadBitmapOverCallback() {
			
			@Override
			public void onLoadBitmapOver(Bitmap _bitmap, String _imgPath) {
				LogHelper.d(TAG, "tagPath :"+ tagPath+" || _imgPath :"+_imgPath);
				ImageView coverImageView = null;
//				if (null == tagPath) {
//					coverImageView = (ImageView) parentView.findViewWithTag(_imgPath);
//				} else {
//					coverImageView = (ImageView) parentView.findViewWithTag(tagPath);
//				}
//				if (null != coverImageView) {
//					coverImageView.setImageBitmap(_bitmap);
//					if (null == _bitmap) {
//						LogHelper.w(TAG, " bitmap is null ! _imgPath :"+_imgPath);
//					}
//				} else {
//					LogHelper.w(TAG, "path not null ,imageView is null:"+_imgPath+ " |tagPath :"+tagPath+" |tagPath :"+(String)imageView.getTag());
//				}
				
				/**
				 * 方案2
				 */
				List<ImageViewSetter> toDisplayImageViewList = imageDisplayMap.get(_imgPath);
				for (ImageViewSetter toDisplayImageView : toDisplayImageViewList) {
					String tag = (String) toDisplayImageView.getImageView().getTag();
					if (tag.equals(_imgPath)) {
						if (null != _bitmap) {
							toDisplayImageView.getImageView().setImageBitmap(_bitmap);
						}
						
					}
				}
				//TODO clear toDisplayImageViewList	
				
			}
		},false);
		if (null != bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	}

	/**
	 * This method is asynchronous
	 * @param imageView
	 * @param path
	 * @param bitmapWidth <=0 means return original image
	 * @param bitmapHeight not useful right now
	 * @param parentView
	 */
	public void displayImage(final ImageView imageView, final String path,
			final int bitmapWidth, final int bitmapHeight) {
		this.displayImage(imageView, path, bitmapWidth, bitmapHeight, 0);
		
	}
	
	public void setLocalCacheDir(String cacheDir) {
		localCacheDir = cacheDir;
		asyncImageLoader.setLocalCacheDir(cacheDir);
	}
	
	public String getLocalCacheDir() {
		localCacheDir = asyncImageLoader.getLocalCacheDir();
		return localCacheDir;
	}
	
	
	private class ImageViewSetter {
		private ImageView imageView;

		private float degree;
		
		public ImageViewSetter(ImageView _imageView, float _degree ) {
			imageView = _imageView;
			degree = _degree;
		}
		
		public ImageView getImageView() {
			return imageView;
		}

		public float getDegree() {
			return degree;
		}
	}
}
