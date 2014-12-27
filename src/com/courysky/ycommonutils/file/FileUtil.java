package com.courysky.ycommonutils.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class FileUtil {
	 private static final String TAG = FileUtil.class.getSimpleName();
	 private static FileUtil sFileUtil;
	 
	 private FileUtil(){
		 
	 }
	 
	 public static FileUtil getInstance() {
		if (null == sFileUtil) {
			sFileUtil = new FileUtil();
		}
		return sFileUtil;
	}
	 
	 public void cleanUpFolder(String dir) {
			File dirFile = new File(dir);
			if (dirFile.exists()) {
				if(dirFile.isDirectory()){
					File[] childFiles = dirFile.listFiles();
					for (File file : childFiles) {
						if (file.isFile()) {
							file.delete();
						} else {
							cleanUpFolder(file.getAbsolutePath());
							file.delete();
						}
					}
				}
			}
			
		}
	 
	 public File getCacheFile(String uri, String targetDir) {
		 File cacheFile = null;
		 File dirFile = new File(targetDir);
		 if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
//		 String dir = context.getCacheDir().getAbsolutePath();
//		 int index = uri.lastIndexOf("/");
//		 
//		 String uriDir = uri.substring(0, index);
//		 int lastSecondIndex = uriDir.lastIndexOf("/");
//		 String fileName_prefix = uriDir.substring(lastSecondIndex);
//		 
//		 String fileName = fileName_prefix+"_"+uri.substring(index + 1); 
//		 cacheFile = new File(dir, fileName);  
		 cacheFile = new File(targetDir, ""+Math.abs(uri.hashCode()));  
		 return cacheFile;
	}
	 
	 /**
	  * bitmap保存到文件
	  * 
	  * @param path
	  * @param bit
	  * @return
	  */
	 public static File saveBitmap(String path, Bitmap bit) {
		 
		 File file = new File(path);
		 try {
			 if (!file.getParentFile().exists()) {
				 if (!file.getParentFile().mkdirs()) {
					 return null;
				 }
			 }
			 if (file.exists()) {
				 file.delete();
			 }
			 file.createNewFile();
			 if (bitmapToFile(path, bit)) {
				 return file;
			 } else {
				 return null;
			 }
		 } catch (IOException e) {
			 e.printStackTrace();
			 return null;
		 }finally{
//			 if(bit!=null&&!bit.isRecycled()){
//				 bit.recycle();
//				 bit = null;
//			 }
		 }
	 }
	 
	 public static boolean bitmapToFile(String path, Bitmap bit) {
		 OutputStream m_fileOutPutStream = null;
		 try {
			 m_fileOutPutStream = new FileOutputStream(path);//
		 } catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 bit.compress(CompressFormat.JPEG, 100, m_fileOutPutStream);// 图片质量
		 try {
			 m_fileOutPutStream.flush();
			 m_fileOutPutStream.close();
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
			 return false;
		 }
		 return true;
		 
	 }
}
