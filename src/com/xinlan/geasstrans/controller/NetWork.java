package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xinlan.geasstrans.model.AppConstants;
import com.xinlan.geasstrans.model.FileModule;

public class NetWork {
	public static final int BUFF_SIZE = 1024 * 1024;// 1K

	private WorkStaus mWorkStatus = WorkStaus.IDLE;
	private NetStatus mStatus = NetStatus.UNCONNECT;

	private ExecutorService mThreadPool;

	private ServerSocket mServerSocket;
	private Socket mSocket;
	private INetWorkCallback mNetCallBack;

	private AtomicBoolean running = new AtomicBoolean(false);

	private List<FileModule> sendFileList = new ArrayList<FileModule>();

	private Runnable mAcceptRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				mSocket = mServerSocket.accept();
				establishConnection();
			} catch (IOException e) {
				e.printStackTrace();
				if (mNetCallBack != null) {
					mNetCallBack.onConnectFail(e);
				}
			}
		}// end run
	};

	private Runnable mInputRunnable = new Runnable() {
		@Override
		public void run() {
			byte[] buf = new byte[2];

			while (running.get()) {
				try {
					InputStream inputStream = mSocket.getInputStream();
					int len = inputStream.read(buf);

					if (buf[0] == TransProtocol.CRL_CLOSE) {
						onRemoteDisconnect();
						break;
					}

					switch (buf[0]) {
					case TransProtocol.CRL_SEND:// send
						handleReceiveFiles();
						break;
					}// end switch
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}// end while
		}// end run
	};

	public NetWork() {
		mThreadPool = Executors.newFixedThreadPool(3);
		mWorkStatus = WorkStaus.IDLE;
	}

	public void startServerListen(INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mServerSocket = new ServerSocket(AppConstants.SERVER_PORT);

			mThreadPool.execute(mAcceptRunnable);
			mWorkStatus = WorkStaus.SERVER;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startConnect(String serverIp, INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mSocket = new Socket(serverIp, AppConstants.SERVER_PORT);

			mWorkStatus = WorkStaus.CLIENT;
			establishConnection();
		} catch (IOException e) {
			e.printStackTrace();
			if (mNetCallBack != null) {
				mNetCallBack.onConnectFail(e);
			}
		}
	}

	protected void establishConnection() {
		if (mNetCallBack != null) {
			mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress()
					.toString());
		}

		mStatus = NetStatus.CONNECT;
		running.set(true);

		mThreadPool.execute(mInputRunnable);
	}

	/**
	 * 断开链接
	 */
	public void disConnection() {
		try {
			byte[] buf = new byte[1];
			buf[0] = TransProtocol.CRL_CLOSE;
			OutputStream out = mSocket.getOutputStream();
			out.write(buf);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		closeNetWork();

		running.set(false);
		mStatus = NetStatus.UNCONNECT;
		mWorkStatus = WorkStaus.IDLE;
	}

	public void closeNetWork() {
		try {
			if (mSocket != null && !mSocket.isClosed()) {
				mSocket.close();
			}

			if (mServerSocket != null && !mServerSocket.isClosed()) {
				mServerSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void onRemoteDisconnect() {
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mNetCallBack != null) {
			mNetCallBack.onRemoteDisconnect();
		}
	}

	public void setNetWorkAction(INetWorkCallback callback) {
		this.mNetCallBack = callback;
	}

	/**
	 * 添加文件发送任务
	 * 
	 * @param list
	 */
	public void addSendTask(List<FileModule> list) {
		if (mStatus != NetStatus.CONNECT)
			return;

		sendFileList.addAll(list);
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				doSendFileTask();
			}
		});
	}

	/**
	 * 发送文件
	 */
	protected void doSendFileTask() {
		try {
			byte[] buffer = new byte[1024];
			buffer[0] = TransProtocol.CRL_SEND;

			// 发送文件请求
			OutputStream out = mSocket.getOutputStream();
			out.write(buffer);

			// 读取remote的响应
			int result = mSocket.getInputStream().read(buffer);
			if (result == -1 || buffer[0] != TransProtocol.CRL_RECEIVE_RSP_OK)
				return;
			
			sendFilesInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 接收文件
	 */
	protected void handleReceiveFiles() {
		try {
			OutputStream out = mSocket.getOutputStream();
			writeReceiveFileResp(out);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeReceiveFileResp(OutputStream output) throws IOException {
		byte[] buf = new byte[1];
		buf[0] = TransProtocol.CRL_RECEIVE_RSP_OK;
		output.write(buf);
	}
	
	/**
	 * 写入文件头
	 */
	private void sendFilesInfo(){
		// send sendFileList
		
	}

	public interface INetWorkCallback {
		/**
		 * 链接成功建立
		 * 
		 * @param remote
		 */
		public void onConnectSuccess(final String remote);

		/**
		 * 链接建立失败
		 * 
		 * @param e
		 */
		public void onConnectFail(Exception e);

		/**
		 * 与远程的链接断开
		 */
		public void onRemoteDisconnect();
		
		/**
		 * 
		 * @param receiveList
		 */
		public void onReceiveFilesInfoList(List<FileModule> receiveList);
	}// end interface

}// end class
