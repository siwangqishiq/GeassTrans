package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.alibaba.fastjson.JSON;

public class FileSendHandler  extends NetHandler {
	public FileSendHandler(InputStream in, OutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void doSendFiles() throws IOException{
		System.out.println("启动 文件发送功能...");
		sendFilesHeadInfo();
	}
	
	protected void sendFilesHeadInfo() throws IOException{
		String jsonStr = JSON.toJSONString(netWork.sendFileList);
		System.out.println("send filehead infos = "+jsonStr);
		
		byte[] willSendData = jsonStr.getBytes();
		byte[]  buffer = new byte[willSendData.length+1];
		
		//copy
		System.arraycopy(willSendData, 0, buffer, 0, willSendData.length);
		
		out.write(buffer, 0, buffer.length);
		out.flush();
	}
	
}//end class
