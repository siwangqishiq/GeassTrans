package com.xinlan.geasstrans.exception;
/**
 * 文件传输异常
 * @author panyi
 *
 */
public class FileTransException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public FileTransException(Exception e){
		super(e);
	}
}//end class
