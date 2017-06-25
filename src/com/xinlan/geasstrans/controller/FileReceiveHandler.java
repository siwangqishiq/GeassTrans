package com.xinlan.geasstrans.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.alibaba.fastjson.JSONArray;
import com.xinlan.geasstrans.model.FileModule;

public class FileReceiveHandler extends NetHandler {
	
	public FileReceiveHandler(InputStream in, OutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void handleReceiveFiles() throws Exception{
		System.out.println("启动 文件接收...");
		sendCtlData(TransProtocol.CRL_REMOTE_RECEIVE);//发送控制字段 让远端也进入文件接收模式
		readFileHeadInfo();
		
		//TODO start to receive the file data
		
	}
	
	public void readFileHeadInfo() throws Exception{
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = in.read(buffer, 0, buffer.length);
		System.out.println("read file data len = "+len);
		if(len < 0 )
			throw new Exception("receive file info data error");
		
		String headInfoStr = new String(buffer,0,len);
		System.out.println("receive head Info = "+headInfoStr);
		
		netWork.receiveFileList = JSONArray.parseArray(headInfoStr, FileModule.class);
		
		if(netWork.mNetCallBack!=null){//UI callback
			netWork.mNetCallBack.onReceiveFilesInfoList(netWork.receiveFileList);
		}//end if
	}

}//end class
