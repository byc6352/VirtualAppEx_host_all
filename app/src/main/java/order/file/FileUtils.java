/**
 *
 */
package order.file;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import utils.StringUtils;
/**
 * @author byc
 *
 */

public class FileUtils {
	/******************** 存储相关常量 ********************/
	/**
	 * Byte与Byte的倍数
	 */
	public static final int BYTE = 1;
	/**
	 * KB与Byte的倍数
	 */
	public static final int KB = 1024;
	/**
	 * MB与Byte的倍数
	 */
	public static final int MB = 1048576;
	/**
	 * GB与Byte的倍数
	 */
	public static final int GB = 1073741824;
	/**
	 * 判断SD卡是否被挂载
	 *
	 * @return
	 */
	public static boolean isSDCardMounted() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	/**
	 * 获取根目录
	 *
	 * @return
	 */
	public static String getRootDir() {
		return "/";
	}
	/**
	 * 获取SD卡的根目录
	 *
	 * @return
	 */
	public static String getSDCardBaseDir() {
		if (isSDCardMounted()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}
	/**
	 * 获取相册的根目录
	 *
	 * @return
	 */
	public static String getPhotoDir() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
	}
	/**
	 * 获取sd卡的大小
	 *
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getSDAllSizeKB() {
		if (isSDCardMounted()) {
			// get path of sdcard
			File path = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(path.getPath());
			// get single block size(Byte)
			long blockSize = sf.getBlockSize();
			// 获取所有数据块数
			long allBlocks = sf.getBlockCount();
			// 返回SD卡大小
			return (allBlocks * blockSize) / 1024; // KB
		}
		return 0;
	}

	/**
	 * 获取sd卡可用大小
	 *
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getSDAvalibleSizeKB() {
		if (isSDCardMounted()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(path.getPath());
			long blockSize = sf.getBlockSize();
			long avaliableSize = sf.getAvailableBlocks();
			return (avaliableSize * blockSize) / 1024;// KB
		}
		return 0;
	}

	/**
	 * 获取文件或文件夹的大小
	 *
	 * @param path
	 *            文件或文件夹的路径
	 * @return
	 */
	public static long getFileSize(String path) {
		return getFileSize(new File(path));
	}

	/**
	 * 获取文件或文件夹的大小
	 *
	 * @param file
	 *            文件或文件夹
	 * @return
	 */
	public static long getFileSize(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				long size = 0;
				for (File subFile : file.listFiles()) {
					size += getFileSize(subFile);
				}
				return size;
			} else {
				return file.length();
			}
		} else {
			throw new IllegalArgumentException("File does not exist!");
		}
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param path
	 *            文件或文件夹的路径
	 */
	public static void deleteFile(String path) {
		deleteFile(new File(path));
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param file
	 *            文件或文件夹
	 */
	public static void deleteFile(File file) {
		if (!file.exists()) {
			Log.d("The file to be deleted does not exist! File's path is: ",
					file.getPath());
		} else {
			deleteFileRecursively(file);
		}
	}

	/**
	 * 删除文件或文件夹
	 *
	 * @param file
	 *            文件或文件夹
	 */
	private static void deleteFileRecursively(File file) {
		if (file.isDirectory()) {
			for (String fileName : file.list()) {
				File item = new File(file, fileName);
				if (item.isDirectory()) {
					deleteFileRecursively(item);
				} else {
					if (!item.delete()) {
						Log.d("Failed in recursively deleting a file, file's path is: ",
								item.getPath());
					}
				}
			}
			if (!file.delete()) {
				Log.d("Failed in recursively deleting a directory, directories' path is: ",
						file.getPath());
			}
		} else {
			if (!file.delete()) {
				Log.d("Failed in deleting this file, its path is: ",
						file.getPath());
			}
		}
	}
	//------------------------------------------------------------------------------------------------
	/**
	 * 根据文件路径获取文件
	 *
	 * @param filePath 文件路径
	 * @return 文件
	 */
	public static File getFileByPath(String filePath) {
		try{
			return StringUtils.isSpace(filePath) ? null : new File(filePath);
		}catch(NullPointerException e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 判断文件是否存在
	 *
	 * @param filePath 文件路径
	 * @return {@code true}: 存在<br>{@code false}: 不存在
	 */
	public static boolean isFileExists(String filePath) {
		return isFileExists(getFileByPath(filePath));
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param file 文件
	 * @return {@code true}: 存在<br>{@code false}: 不存在
	 */
	public static boolean isFileExists(File file) {
		try{
			return file != null && file.exists();
		}catch(SecurityException e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * byte单位转换（单位：unit）
	 *
	 * @param size 大小
	 * @param unit <ul>
	 *             <li>{@link ConstUtils#BYTE}: 字节</li>
	 *             <li>{@link ConstUtils#KB}  : 千字节</li>
	 *             <li>{@link ConstUtils#MB}  : 兆</li>
	 *             <li>{@link ConstUtils#GB}  : GB</li>
	 *             </ul>
	 * @return 大小以unit为单位
	 */
	public static double byte2Unit(long size, int unit) {
		switch (unit) {
			case BYTE:
			case KB:
			case MB:
			case GB:
				return (double) size / unit;
		}
		return -1;
	}

	/**
	 * 获取文件大小
	 * <p>例如：getFileSize(filePath, ConstUtils.MB); 返回文件大小单位为MB</p>
	 *
	 * @param filePath 文件路径
	 * @param unit     <ul>
	 *                 <li>{@link ConstUtils#BYTE}: 字节</li>
	 *                 <li>{@link ConstUtils#KB}  : 千字节</li>
	 *                 <li>{@link ConstUtils#MB}  : 兆</li>
	 *                 <li>{@link ConstUtils#GB}  : GB</li>
	 *                 </ul>
	 * @return 文件大小以unit为单位
	 */
	public static double getFileSize(String filePath, int unit) {
		return getFileSize(getFileByPath(filePath), unit);
	}

	/**
	 * 获取文件大小
	 * <p>例如：getFileSize(file, ConstUtils.MB); 返回文件大小单位为MB</p>
	 *
	 * @param file 文件
	 * @param unit <ul>
	 *             <li>{@link ConstUtils#BYTE}: 字节</li>
	 *             <li>{@link ConstUtils#KB}  : 千字节</li>
	 *             <li>{@link ConstUtils#MB}  : 兆</li>
	 *             <li>{@link ConstUtils#GB}  : GB</li>
	 *             </ul>
	 * @return 文件大小以unit为单位
	 */
	public static double getFileSize(File file, int unit) {
		if (!isFileExists(file)) return -1;
		try{
			return byte2Unit(file.length(), unit);
		}catch(SecurityException e){
			e.printStackTrace();
			return -1;
		}
	}
	/**
	 * 获取文件流
	 * <p>例如：getRandomFile(file); 返回RandomAccessFile文件流</p>
	 *
	 * @param file 文件
	 * @return RandomAccessFile文件流
	 */
	public static RandomAccessFile getRandomFile(File file,String mode) {
		try{
			RandomAccessFile fileOutStream = new RandomAccessFile(file, mode);
			return fileOutStream;
		}catch(IllegalArgumentException e){
			e.printStackTrace();
			return null;
		}catch(FileNotFoundException e){
			e.printStackTrace();
			return null;
		}catch(SecurityException e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 判断文件是否存在
	 *
	 * @param filePath 文件路径
	 * @return {@code true}: 存在<br>{@code false}: 不存在
	 */
	public static boolean fileExists(String filename) {
		try{
			File file=new File(filename);
			boolean b=file.exists();
			return b;
		}catch(NullPointerException e){
			e.printStackTrace();
			return false;
		}catch(SecurityException e){
			e.printStackTrace();
			return false;
		}
	}
}
