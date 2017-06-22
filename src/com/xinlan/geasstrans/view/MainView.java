package com.xinlan.geasstrans.view;

import javax.swing.JFrame;

import com.xinlan.geasstrans.util.VersionUtil;

public class MainView {
	
	public static final int VIEW_WIDTH = 500;
	public static final int VIEW_HEIGHT = 800;
	
	protected JFrame mainFrame;
	
	public static void main(String agrs[]){
		new MainView().execute();
	}
	
	public void execute(){
		mainFrame = new JFrame();
		mainFrame.setSize(VIEW_WIDTH, VIEW_HEIGHT);
		mainFrame.setTitle(VersionUtil.getMainTitle());
		mainFrame.setVisible(true);
		mainFrame.setResizable(false);
	}
	
	
}//end class
