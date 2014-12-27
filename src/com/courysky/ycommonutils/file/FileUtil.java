package com.courysky.ycommonutils.file;

import java.io.File;

import android.content.Context;

public class FileUtil {
	 private static final String TAG = FileUtil.class.getSimpleName();
	 
	 
	 private static FileUtil sFileUtil;
	 private Context mContext;
	 
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
		 int index = uri.lastIndexOf("/");
		 
		 String uriDir = uri.substring(0, index);
		 int lastSecondIndex = uriDir.lastIndexOf("/");
		 String fileName_prefix = uriDir.substring(lastSecondIndex);
		 
		 String fileName = fileName_prefix+"_"+uri.substring(index + 1); 
//		 cacheFile = new File(dir, fileName);  
		 cacheFile = new File(targetDir, ""+Math.abs(uri.hashCode()));  
		 return cacheFile;
	}
	 
}
