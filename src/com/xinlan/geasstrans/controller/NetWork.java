package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.xinlan.geasstrans.model.AppConstants;

public class NetWork {
	private WorkStaus mWorkStatus = WorkStaus.IDLE;

	private ExecutorService mThreaPool;

	private ServerSocket mServerSocket;
	private Socket mSocket;
	private INetWorkCallback mNetCallBack;

	private Runnable mAcceptRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				mSocket = mServerSocket.accept();
				
				if (mNetCallBack != null) {
					mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress().toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
				if (mNetCallBack != null) {
					mNetCallBack.onConnectFail(e);
				}
			}
		}// end run
	};

	public NetWork() {
		mThreaPool = Executors.newFixedThreadPool(2);
		mWorkStatus = WorkStaus.IDLE;
	}

	public void startServerListen(INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mServerSocket = new ServerSocket(AppConstants.SERVER_PORT);
			
			mThreaPool.execute(mAcceptRunnable);
			mWorkStatus = WorkStaus.SERVER;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startConnect(String serverIp, INetWorkCallback callback) {
		setNetWorkAction(callback);
		try {
			mSocket = new Socket(serverIp, AppConstants.SERVER_PORT);

			if (mNetCallBack != null) {
				mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress().toString());
			}

			mWorkStatus = WorkStaus.CLIENT;
		} catch (IOException e) {
			e.printStackTrace();
			if (mNetCallBack != null) {
				mNetCallBack.onConnectFail(e);
			}
		}
	}

	/**
	 * 断开链接
	 */
	public void disConnection() {
		closeNetWork();
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

	public void setNetWorkAction(INetWorkCallback callback) {
		this.mNetCallBack = callback;
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
	}// end interface

}// end class
