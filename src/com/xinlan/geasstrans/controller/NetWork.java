package com.xinlan.geasstrans.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

import com.alibaba.fastjson.JSON;
import com.xinlan.geasstrans.model.AppConstants;
import com.xinlan.geasstrans.model.FileModule;

public class NetWork {
	private WorkStaus mWorkStatus = WorkStaus.IDLE;
	private NetStatus mStatus = NetStatus.UNCONNECT;
	
	private ServerSocket mServerSocket;
	private Socket mSocket;
	private INetWorkCallback mNetCallBack;

	private AtomicBoolean running = new AtomicBoolean(false);

	protected List<FileModule> sendFileList = new ArrayList<FileModule>();
	
	private FileSendHandler fileSendHandler;
	private FileReceiveHandler fileReceiveHandler;
	
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

	private Runnable mReceiveRunable = new Runnable() {
		@Override
		public void run() {
			while (running.get()) {
				try {
					DataInputStream in = new DataInputStream(mSocket.getInputStream());
					DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
					System.out.println("启动输入监听线程.......");
					byte mode = in.readByte();
					System.out.println("read mode = "+mode);

					if (mode == TransProtocol.CRL_CLOSE) {
						onRemoteDisconnect();
						break;
					}

					switch (mode) {
					case TransProtocol.CRL_SEND:// send
						if(fileReceiveHandler==null){
							fileReceiveHandler = new FileReceiveHandler(in,out,mSocket,NetWork.this);
						}
						fileReceiveHandler.handleReceiveFiles();
						break;
					case TransProtocol.CRL_RECEIVE://receive
						
						break;
					}// end switch
				} catch (Exception e) {
					e.printStackTrace();
				}
			}// end while
		}// end run
	};

	public NetWork() {
		mWorkStatus = WorkStaus.IDLE;
	}

	public void startServerListen(INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mServerSocket = new ServerSocket(AppConstants.SERVER_PORT);
			//mThreadPool.execute(mAcceptRunnable);
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
		if (mNetCallBack != null) {
			mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress()
					.toString());
		}

		mStatus = NetStatus.CONNECT;
		running.set(true);

		//mThreadPool.execute(mInputRunnable);
		new Thread(mReceiveRunable).start();;
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
		new Thread(new Runnable(){
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
			if(fileSendHandler==null){
				DataInputStream in = new DataInputStream(mSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
				
				fileSendHandler = new FileSendHandler(in,out,mSocket,this);
			}
			fileSendHandler.doSendFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}// end class
