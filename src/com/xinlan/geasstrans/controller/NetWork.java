package com.xinlan.geasstrans.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xinlan.geasstrans.model.AppConstants;

public class NetWork {
	private WorkStaus mWorkStatus = WorkStaus.IDLE;
	private NetStatus mStatus = NetStatus.UNCONNECT;

	private ExecutorService mThreaPool;

	private ServerSocket mServerSocket;
	private Socket mSocket;
	private INetWorkCallback mNetCallBack;
	
	private AtomicBoolean running =new AtomicBoolean(false);

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
	
	private Runnable mInputRunnable = new Runnable(){
		@Override
		public void run() {
			byte[] buf = new byte[1];
			
			while(running.get()){
				try {
					InputStream inputStream = mSocket.getInputStream();
					inputStream.read(buf);
					
					if(buf[0] == TransProtocol.CRL_CLOSE){
						onRemoteDisconnect();
						break;	
					}
					
					switch(buf[0]){
					case TransProtocol.CRL_SEND://send
						break;
					}//end switch
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}//end while
		}// end run
	};
	
	public NetWork() {
		mThreaPool = Executors.newFixedThreadPool(3);
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
			
			mWorkStatus = WorkStaus.CLIENT;
			establishConnection();
		} catch (IOException e) {
			e.printStackTrace();
			if (mNetCallBack != null) {
				mNetCallBack.onConnectFail(e);
			}
		}
	}
	
	protected void establishConnection(){
		if (mNetCallBack != null) {
			mNetCallBack.onConnectSuccess(mSocket.getRemoteSocketAddress().toString());
		}
		
		mStatus = NetStatus.CONNECT;
		running.set(true);
		
		mThreaPool.execute(mInputRunnable);
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
	
	protected void onRemoteDisconnect(){
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(mNetCallBack!=null){
			mNetCallBack.onRemoteDisconnect();
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
		
		/**
		 * 与远程的链接断开
		 */
		public void onRemoteDisconnect();
	}// end interface

}// end class
