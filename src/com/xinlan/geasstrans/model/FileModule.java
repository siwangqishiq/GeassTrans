package com.xinlan.geasstrans.model;

import java.io.File;

/**
 * 传输文件数据
 * @author panyi
 *
 */
public class FileModule {
	@Override
	public String toString() {
		return "FileModule [path=" + path + ", name=" + name + ", size=" + size
				+ ", curProgress=" + curProgress + "]";
	}

	private String path;
	private String name;
	private long size;
	private long curProgress = 0;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getCurProgress() {
		return curProgress;
	}
	public void setCurProgress(long curProgress) {
		this.curProgress = curProgress;
	}
	
	public static FileModule create(final String path){
		File file = new File(path);
		FileModule module = new FileModule();
		module.setPath(file.getAbsolutePath());
		module.setCurProgress(0);
		module.setName(file.getName());
		module.setSize(file.length());
		
		return module;
	}
	
}//end class
