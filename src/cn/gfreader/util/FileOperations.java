package cn.gfreader.util;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileOperations {

	public static String getFilenameFromPath(String pathandname) {
		int start = pathandname.lastIndexOf("/");
		int end = pathandname.lastIndexOf(".");
		if (start != -1 && end != -1) {
			return pathandname.substring(start + 1, end);
		} else {
			return null;
		}
	}

	public static String GetMd5(String FilePath) {
		char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		FileInputStream fis = null;
		String sString;
		char str[] = new char[16 * 2];
		int k = 0;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(FilePath);
			byte[] buffer = new byte[2048];
			int length = -1;
			// long s = System.currentTimeMillis();
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			byte[] b = md.digest();

			for (int i = 0; i < 16; i++) {
				byte byte0 = b[i];
				str[k++] = hexdigits[byte0 >>> 4 & 0xf];
				str[k++] = hexdigits[byte0 & 0xf];
			}
			fis.close();
			sString = new String(str);

			return sString;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
