package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xinlan.geasstrans.model.AppConstants;
import com.xinlan.geasstrans.model.FileBean;

public class NetWork {
	private WorkStaus mWorkStatus = WorkStaus.IDLE;
	private NetStatus mStatus = NetStatus.UNCONNECT;

	private ServerSocket mServerSocket;
	private Socket mSocket;
	protected INetWorkCallback mNetCallBack;

	private AtomicBoolean running = new AtomicBoolean(false);

	protected List<FileBean> sendFileList = new ArrayList<FileBean>();
	protected List<FileBean> receiveFileList = new ArrayList<FileBean>();

	private FileSendHandler fileSendHandler;
	private FileReceiveHandler fileReceiveHandler;

	private InputStream in;
	private OutputStream out;

	public interface INetWorkCallback {
		public void onConnectSuccess(final String remote);// 链接成功建立

		public void onConnectFail(Exception e);// 链接建立失败

		public void onRemoteDisconnect();// 与远程的链接断开

		public void onReceiveFilesInfoList(List<FileBean> receiveList);

		public void onReceiveFilesComplete(List<FileBean> list);// 接收文件成功

		public void onSendFilesComplete(List<FileBean> list);// 发送文件成功
		
		public void onStatusChange(NetStatus fromStatus,NetStatus curStatus);//网络工作状态改变

		public void onReceiveFileProgressUpdate(List<FileBean> list, String filename, long cur, 
				long fileSize, long total, long progress,boolean isSend);//发送or接收进度回调
	}// end interface

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

	public NetWork() {
		mWorkStatus = WorkStaus.IDLE;
	}

	public void startServerListen(INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mServerSocket = new ServerSocket(AppConstants.SERVER_PORT);
			// mThreadPool.execute(mAcceptRunnable);
			new Thread(mAcceptRunnable).start();
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
		try {
			in = mSocket.getInputStream();
			out = mSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mNetCallBack != null) {
			mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress().toString());
		}

		changeStatus(NetStatus.CONNECT);
		running.set(true);

		// mThreadPool.execute(mInputRunnable);
		new Thread(mReceiveRunable).start();
		;
	}

	/**
	 * 断开链接
	 */
	public void disConnection() {
		try {
			byte[] buf = new byte[1];
			buf[0] = Protocol.CRL_CLOSE;
			OutputStream out = mSocket.getOutputStream();
			out.write(buf);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		closeNetWork();

		running.set(false);
		changeStatus(NetStatus.UNCONNECT);
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
	public void addSendTask(List<FileBean> list) {
		if (mStatus != NetStatus.CONNECT)
			return;

		sendFileList.addAll(list);
		new Thread(new Runnable() {
			@Override
			public void run() {
				doSendFileTask();
			}
		}).start();
	}

	/**
	 * 发送文件
	 */
	protected void doSendFileTask() {
		try {
			byte[] buf = new byte[2];
			buf[0] = Protocol.CRL_REMOTE_SEND;
			out.write(buf, 0, 2);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void changeStatus(NetStatus status){
		if(this.mStatus == status){
			return;
		}
		
		NetStatus fromeStatus = mStatus;
		mStatus = status;
		if(mNetCallBack!=null){
			mNetCallBack.onStatusChange(fromeStatus, mStatus);
		}//end if
	}

	private Runnable mReceiveRunable = new Runnable() {
		@Override
		public void run() {
			while (running.get()) {
				try {
					System.out.println("启动输入监听线程.......");
					byte[] buf = new byte[2];
					int ret = in.read(buf, 0, 2);
					if (ret < 0)
						continue;
					int mode = buf[0];
					System.out.println("read mode = " + mode);

					if (mode == Protocol.CRL_CLOSE) {
						onRemoteDisconnect();
						break;
					}

					switch (mode) {
					case Protocol.CRL_REMOTE_SEND:// send 进入接收模式
						if (fileReceiveHandler == null) {
							fileReceiveHandler = new FileReceiveHandler(in, out, mSocket, NetWork.this);
						}
						fileReceiveHandler.handleReceiveFiles();
						break;
					case Protocol.CRL_REMOTE_RECEIVE:// receive 进入发送模式
						if (fileSendHandler == null) {
							fileSendHandler = new FileSendHandler(in, out, mSocket, NetWork.this);
						}
						fileSendHandler.doSendFiles();
						break;
					}// end switch
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // end while
		}// end run
	};

}// end class
