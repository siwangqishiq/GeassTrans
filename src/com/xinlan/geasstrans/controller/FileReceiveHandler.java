package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import com.alibaba.fastjson.JSONArray;
import com.xinlan.geasstrans.controller.NetWork.INetWorkCallback;
import com.xinlan.geasstrans.exception.FileTransException;
import com.xinlan.geasstrans.model.FileBean;

/**
 * 文件接收
 * 
 * @author panyi
 *
 */
public class FileReceiveHandler extends NetHandler {

	public FileReceiveHandler(InputStream in, OutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void handleReceiveFiles() throws Exception {
		try {
			netWork.changeStatus(NetStatus.RECEIVE);

			System.out.println("启动 文件接收...");
			sendCtlData(Protocol.CRL_REMOTE_RECEIVE);// 发送控制字段 让远端也进入文件接收模式
			readFileHeadInfo();

			// start to receive the file data
			List<FileBean> list = netWork.receiveFileList;
			DataInputStream dataInputStream = new DataInputStream(in);
			DataOutputStream dataOutputStream = new DataOutputStream(out);
			receiveFileList(list, dataInputStream, dataOutputStream);

			// 接收文件成功
			netWork.changeStatus(NetStatus.CONNECT);

			if (netWork.mNetCallBack != null) {
				netWork.mNetCallBack.onReceiveFilesComplete(list);
			}
			netWork.receiveFileList.clear();
		} catch (Exception e) {
			throw new FileTransException(e);
		}
	}

	protected void receiveFileList(List<FileBean> list, DataInputStream input, DataOutputStream output) throws IOException {
		long total = getListTotalSize(list);
		for (int i = 0; i < list.size(); i++) {
			output.writeInt(i);// index
			output.writeUTF(list.get(i).getName());// filename
			output.flush();
			receiveFile(input, list.get(i), total, getHasUpdateSize(list));
		} // end for i
	}

	protected void receiveFile(DataInputStream input, final FileBean module, long total, long hasGetSize) throws IOException {
		System.out.println("start to recie file " + module.getName() + " ...");
		FileOutputStream fos = null;
		byte[] buf = new byte[BUFFER_SIZE];
		int len;
		long current = 0;

		INetWorkCallback callback = netWork.mNetCallBack;
		try {
			fos = new FileOutputStream(new File(module.getName()));
			while ((len = input.read(buf, 0, BUFFER_SIZE)) != -1) {
				fos.write(buf, 0, len);
				current += len;
				// System.out.println("receive " + current + " / " + module.getSize());
				if (callback != null) {
					//System.out.println("文件 = " + current + "  /" + module.getSize() + "    总的 " + total + " / " + (hasGetSize + current));
					callback.onFileProgressUpdate(module, module.getName(), current, module.getSize(), hasGetSize + current, total, false);
				}

				if (current >= module.getSize())// 文件已经接收完毕
					break;
			} // end while
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		module.setCurProgress(module.getSize());
		System.out.println(module.getName() + " receive complete! ");
	}

	public void readFileHeadInfo() throws Exception {
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = in.read(buffer, 0, buffer.length);
		System.out.println("read file data len = " + len);
		if (len < 0)
			throw new Exception("receive file info data error");

		String headInfoStr = new String(buffer, 0, len);
		System.out.println("receive head Info = " + headInfoStr);

		netWork.receiveFileList = JSONArray.parseArray(headInfoStr, FileBean.class);

		if (netWork.mNetCallBack != null) {// UI callback
			netWork.mNetCallBack.onReceiveFilesInfoList(netWork.receiveFileList);
		} // end if
	}

}// end class
