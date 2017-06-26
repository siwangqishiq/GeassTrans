package com.xinlan.geasstrans.controller;

public class Protocol {
	public static final byte CRL_CLOSE = 1;//控制字段 关闭链接
	public static final byte CRL_REMOTE_SEND = 2;//控制字段  文件发送
	public static final byte CRL_REMOTE_RECEIVE = 3;//控制字段  接收文件
	
	public static final byte CRL_DATA = 4;//数据传输
	public static final byte CRL_RECEIVE_RSP_OK = 5;//接收文件响应
	public static final byte CRL_RECEIVE_RSP_REFUSE = 6;//接收文件响应 错误
}//end class
