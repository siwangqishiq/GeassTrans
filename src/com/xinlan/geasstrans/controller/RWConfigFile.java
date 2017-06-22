package com.xinlan.geasstrans.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.xinlan.geasstrans.model.AppConstants;

/**
 * 
 * @author panyi
 *
 */
public class RWConfigFile {
	public static String readKey(String key) {
		String value = null;
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(getConfigFile());
			prop.load(fis);
			
			value = prop.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreamSlient(fis);
		}
		return value == null ? "127.0.0.1" : value;
	}

	public static void writeKey(String key, String value) {
		Properties prop = new Properties();
		FileOutputStream oFile = null;
		prop.setProperty(key, value);
		try {
			oFile = new FileOutputStream(getConfigFile());
			prop.store(oFile, "add");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStreamSlient(oFile);
		}
	}
	
	private static File getConfigFile() throws IOException{
		File file = new File(AppConstants.CONFIG_FILE);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	private static void closeStreamSlient(Closeable ins) {
		try {
			if (ins != null) {
				ins.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}// end class
