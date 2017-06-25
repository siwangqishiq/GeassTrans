package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileReceiveHandler extends NetHandler {

	public FileReceiveHandler(DataInputStream in, DataOutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void handleReceiveFiles() throws IOException, InterruptedException{
		System.out.println("接收文件功能启动");
		writeRespOK();//接收端准备就绪 可以发送文件数据
		readFileHeadInfo();
	}
	
	public void readFileHeadInfo() throws IOException{
		System.out.println("prepaer to read file head infos");
		String str = in.readUTF();
		System.out.println("headsInfo = "+str);
	}
	
	
	
//	protected void handleReceiveFiles() {
//		try {
//			writeRespOK(out);
//			
//			//接受filesInfo数据
//			InputStream in = mSocket.getInputStream();
//			
//			byte[] buffer = new byte[10*1024];
//			int len = in.read(buffer);
//			
//			String headInfoStr = new String(buffer);
//			System.out.println("receive file head info = "+headInfoStr);
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}//end class
