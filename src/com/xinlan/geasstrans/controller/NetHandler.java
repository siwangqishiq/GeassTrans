package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import com.xinlan.geasstrans.model.FileBean;

public class NetHandler {
	public static final int BUFFER_SIZE = 20 * 1024;// 20k

	public static final int FILE_BUFFER_SIZE = 1024 * 20;// 20k

	protected InputStream in;
	protected OutputStream out;
	protected NetWork netWork;
	protected Socket socket;

	public NetHandler(InputStream in, OutputStream out, Socket sock, NetWork netWork) {
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

	public static long getListTotalSize(List<FileBean> list) {
		if (list == null)
			return 0;
		long totalSize = 0;
		for (FileBean bean : list) {
			totalSize += bean.getSize();
		} // end for each
		return totalSize;
	}

	public static long getHasUpdateSize(List<FileBean> list) {
		if (list == null)
			return 0;
		long totalSize = 0;
		for (FileBean bean : list) {
			totalSize += bean.getCurProgress();
		} // end for each
		return totalSize;
	}
}// end class
