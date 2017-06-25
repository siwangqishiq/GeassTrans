package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.alibaba.fastjson.JSON;

public class FileSendHandler  extends NetHandler {

	public FileSendHandler(DataInputStream in, DataOutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void doSendFiles() throws IOException{
		System.out.println("send file perpare data request");
		startSendFileRequest();
		// 读取remote的响应
		byte response = in.readByte();
		System.out.println("response = "+response);
		
		sendFilesInfo();//发送文件头描述数据
	}
	
	private void startSendFileRequest() throws IOException{
		out.writeByte(TransProtocol.CRL_SEND);
		out.flush();
	}
	
	

	/**
	 * 写入文件头
	 */
	private void sendFilesInfo() throws IOException{
		// send sendFileList
		String jsonStr = JSON.toJSONString(netWork.sendFileList);
		System.out.println("send file info = "+jsonStr);
		out.writeUTF(jsonStr);
//		sendBytesData(buf);
	}
	
}//end class
