package com.courysky.ycommonutils.ui;

import java.lang.ref.SoftReference;

import com.courysky.ycommonutils.LogHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class UIUtil {
    private final static String TAG = UIUtil.class.getSimpleName();
    
	private static int sScreenWidth ;
	private static int sScreenHeight ;
	private static int[] sScreenSize = new int[2];
	public static int getsScreenHeight(Context context) {
		if (sScreenHeight>0) {
			
		} else {
//			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); 
//			sScreenWidth = wm.getDefaultDisplay().getWidth(); 
//			sScreenHeight = wm.getDefaultDisplay().getHeight(); 
		    
		    DisplayMetrics dm = context.getResources().getDisplayMetrics();  
            sScreenWidth = dm.widthPixels;  
            sScreenHeight = dm.heightPixels;  
            LogHelper.i(TAG, "屏幕尺寸2：宽度 = " + sScreenWidth + "高度 = " + sScreenHeight + "密度 = " + dm.densityDpi);
		}
		return sScreenHeight;
	}

	/**
	 */
    public static int getScreenWidth(Context context){
        //LINK http://bLogHelper.csdn.net/yanzi1225627/article/details/17199323
    	if (sScreenWidth>0) {
			
		} else {
//			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); 
//			sScreenWidth = wm.getDefaultDisplay().getWidth(); 
//			sScreenHeight = wm.getDefaultDisplay().getHeight(); 
			
			DisplayMetrics dm = context.getResources().getDisplayMetrics();  
			sScreenWidth = dm.widthPixels;  
			sScreenHeight = dm.heightPixels;  
	        LogHelper.i(TAG, "屏幕尺寸2：宽度 = " + sScreenWidth + "高度 = " + sScreenHeight + "密度 = " + dm.densityDpi);
		}
		return sScreenWidth;
    }
    
    public static int dip2px(Context context, float dipValue){              
        final float scale = context.getResources().getDisplayMetrics().density;                   
        return (int)(dipValue * scale + 0.5f);           
    }              
    public static int px2dip(Context context, float pxValue){                  
        final float scale = context.getResources().getDisplayMetrics().density;                   
        return (int)(pxValue / scale + 0.5f);           
    }
    
    public static int[] getDisplay (Context c){
		if (sScreenSize [0] >0) {
			return sScreenSize;
		} else {
			WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE); 
			int width = wm.getDefaultDisplay().getWidth(); 
			int height = wm.getDefaultDisplay().getHeight(); 
			int[] size = new int[]{width,height};
			return size;
		}
	}

    
    public static Bitmap rotateBitmap(Bitmap bitmap, float degree){
//		ExifInterface exif = new ExifInterface(path);
//		String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//		int degree;
//		int orientationInt = Integer.parseInt(orientation);
//		switch (orientationInt) {
//		case ExifInterface.ORIENTATION_NORMAL:
//			degree = 0;
//			break;
//		case ExifInterface.ORIENTATION_ROTATE_90:{
//			degree = 90;
//			}
//			break;
//		case ExifInterface.ORIENTATION_ROTATE_180:{
//			degree = 180;
//		}
//			break;
//		case ExifInterface.ORIENTATION_ROTATE_270:{
//			degree = 270;
//		}
//			break;
//		default:{
//			degree = 0;
//		}
//			break;
//		}
		if (degree != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inPurgeable = true;
//			options.inInputShareable = true; 
//			
//			options.inPreferredConfig = Bitmap.Config.RGB_565;
//			
//			options.inJustDecodeBounds = false;
			
//			Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,
//					0, 0,
//					bitmap.getWidth(),
//					bitmap.getHeight(), matrix, true);
			SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(
					Bitmap.createBitmap(bitmap,
					0, 0,
					bitmap.getWidth(),
					bitmap.getHeight(), matrix, true));
			bitmap.recycle();
			System.gc();
//			Bitmap.Config config = rotatedBitmap.getConfig();
//			BitmapFactory.Options options = rotatedBitmap.getConfig();
//			options.inPurgeable = true;
//			options.inInputShareable = true; 
//			
//			options.inPreferredConfig = Bitmap.Config.RGB_565;
//			
//			options.inJustDecodeBounds = false;
			
			return softReference.get();
		} else {
			return bitmap;
		}
		
		
	}
}
