package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.xinlan.geasstrans.controller.NetWork.INetWorkCallback;
import com.xinlan.geasstrans.exception.FileTransException;
import com.xinlan.geasstrans.model.FileBean;

/**
 * 文件发送
 * 
 * @author panyi
 *
 */
public class FileSendHandler extends NetHandler {
	public FileSendHandler(InputStream in, OutputStream out, Socket sock, NetWork netWork) {
		super(in, out, sock, netWork);
	}

	public void doSendFiles() throws FileTransException {
		try {
			netWork.changeStatus(NetStatus.SEND);

			System.out.println("启动 文件发送功能...");

			sendFilesHeadInfo();

			DataInputStream dataInputStream = new DataInputStream(in);
			DataOutputStream dataOutputStream = new DataOutputStream(out);

			sendFileList(netWork.sendFileList, dataInputStream, dataOutputStream);

			netWork.changeStatus(NetStatus.CONNECT);

			if (netWork.mNetCallBack != null) {
				netWork.mNetCallBack.onSendFilesComplete(netWork.sendFileList);
			}
			netWork.sendFileList.clear();
		} catch (Exception e) {
			throw new FileTransException(e);
		}
	}

	protected void sendFileList(List<FileBean> list, DataInputStream input, DataOutputStream output) throws IOException {
		long total = getListTotalSize(list);
		for (int i = 0; i < list.size(); i++) {
			int index = input.readInt();
			System.out.println("will send index = " + index);
			String sendFileName = input.readUTF();
			System.out.println("will send filename = " + sendFileName);

			copyFileToRemote(output, list.get(i), total, getHasUpdateSize(list));
		} // end for i
	}

	protected void copyFileToRemote(DataOutputStream output, final FileBean module, long total, long hasSendSize) throws IOException {
		System.out.println("start to copy  " + module.getName() + "...");
		FileInputStream fis = null;

		INetWorkCallback callback = netWork.mNetCallBack;

		try {
			int len;
			byte[] buf = new byte[BUFFER_SIZE];
			long current = 0;
			fis = new FileInputStream(new File(module.getPath()));
			while ((len = fis.read(buf, 0, BUFFER_SIZE)) != -1) {
				output.write(buf, 0, len);
				current += len;

				if (callback != null) {
					//System.out.println("文件 = " + current + "  /" + module.getSize() + "    总的 " + total + " / " + (hasSendSize + current));
					callback.onFileProgressUpdate(module, module.getName(), current, module.getSize(), hasSendSize + current, total, false);
				}
			} // end while
			out.flush();
		} finally {
			fis.close();
		}
		module.setCurProgress(module.getSize());
		System.out.println(" file " + module.getName() + "copy complete!");
	}

	protected void sendFilesHeadInfo() throws Exception {
		String jsonStr = JSON.toJSONString(netWork.sendFileList);
		System.out.println("send filehead infos = " + jsonStr);

		byte[] willSendData = jsonStr.getBytes();
		byte[] buffer = new byte[willSendData.length + 1];

		// copy
		System.arraycopy(willSendData, 0, buffer, 0, willSendData.length);

		out.write(buffer, 0, buffer.length);
		out.flush();
	}

}// end class
