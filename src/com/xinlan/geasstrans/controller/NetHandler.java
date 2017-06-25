package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetHandler {
	protected DataInputStream in;
	protected DataOutputStream out;
	protected NetWork netWork;
	protected Socket socket;
	
	
	public NetHandler(DataInputStream in,
			DataOutputStream out,Socket sock,NetWork netWork){
		this.socket = sock;
		this.in = in;
		this.out = out;
		this.netWork = netWork;
	}
	
	public void writeRespOK() throws IOException {
		out.writeByte(TransProtocol.CRL_RECEIVE_RSP_OK);
		out.flush();
		System.out.println("wirte respon CRL_RECEIVE_RSP_OK");
	}
}//end class
