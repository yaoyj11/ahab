package org.renci.ahab.libtransport.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class StaticUtil {

	/**
	 * Read a text file from a given path
	 * @param path
	 * @return
	 */
	public static String readTextFile(File path) {
		try {
			FileInputStream is = new FileInputStream(path);
			BufferedReader bin = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = bin.readLine()) != null) {
				sb.append(line);
				// re-add line separator
				sb.append(System.getProperty("line.separator"));
			}
	
			bin.close();
	
			return sb.toString();
	
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Generate a file name of a file in a user home directory and return
	 * corresponding file
	 * @param pathStr
	 * @return
	 */
	public static File getUserFileName(String pathStr) {
		File f;

		if (pathStr.startsWith("~/")) {
			pathStr = pathStr.replaceAll("~/", "/");
			f = new File(System.getProperty("user.home"), pathStr);
		}
		else {
			f = new File(pathStr);
		}
		return f;
	}
}
