package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetHandler {
	public static final int BUFFER_SIZE = 20*1024;//10k
	
	protected InputStream in;
	protected OutputStream out;
	protected NetWork netWork;
	protected Socket socket;
	
	
	public NetHandler(InputStream in,
			OutputStream out,Socket sock,NetWork netWork){
		this.socket = sock;
		this.in = in;
		this.out = out;
		this.netWork = netWork;
	}
	
	public void sendCtlData(byte sendByte) throws IOException {
		byte[] buf = new byte[2];
		buf[0] = sendByte;
		out.write(buf, 0, 1);
		out.flush();
	}
}//end class
