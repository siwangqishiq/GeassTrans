package com.xinlan.geasstrans.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.xinlan.geasstrans.controller.RWConfigFile;
import com.xinlan.geasstrans.model.AppConstants;
import com.xinlan.geasstrans.model.FileModule;
import com.xinlan.geasstrans.util.VersionUtil;

public class MainView {

	public static final int VIEW_WIDTH = 500;
	public static final int VIEW_HEIGHT = 800;

	public static final int PADDING = 10;

	protected JFrame mMainFrame;

	protected JPanel mHeadPanel;
	protected JTextField mServerIPText;
	protected JButton mConnectBtn;
	protected JButton mListenBtn;
	protected JButton mCancelWorkBtn;

	protected JPanel mBodyPanel;

	protected JPanel mFileSelectPanel;
	protected JScrollPane mFileListScrollPanel;
	protected JLabel  mFileListText;
	protected JButton mSelectFileBtn;
	protected JButton mSendFileBtn;
	
	protected JLabel mStatusLabel;
	
	private List<FileModule> mSelectedList = new ArrayList<FileModule>(2);

	public static void main(String agrs[]) {
		new MainView().execute();
	}

	public void execute() {
		initUI();
	}

	private void initUI() {
		mMainFrame = new JFrame();
		mMainFrame.setSize(VIEW_WIDTH, VIEW_HEIGHT);
		mMainFrame.setTitle(VersionUtil.getMainTitle());
		mMainFrame.setLocation(400, 200);
		mMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mMainFrame.setLayout(new BorderLayout());

		initHeadUI();

		initBodyUI();

		setHeadPanelEnable(true);

		addListener();

		mMainFrame.setVisible(true);
		mMainFrame.setResizable(false);
	}

	private void initHeadUI() {
		mHeadPanel = new JPanel();
		mHeadPanel.setLayout(new BorderLayout());
		mServerIPText = new JTextField();
		mServerIPText.setText(RWConfigFile.readKey(AppConstants.LAST_CONNECT_ADDRESS));
		mHeadPanel.add(mServerIPText, BorderLayout.CENTER);

		JPanel headBtnsPanel = new JPanel();
		mConnectBtn = new JButton("链接");
		headBtnsPanel.add(mConnectBtn);
		mListenBtn = new JButton("监听");
		headBtnsPanel.add(mListenBtn);
		mCancelWorkBtn = new JButton("断开链接");
		headBtnsPanel.add(mCancelWorkBtn);
		mCancelWorkBtn.setVisible(false);

		mHeadPanel.add(headBtnsPanel, BorderLayout.EAST);

		mMainFrame.add(mHeadPanel, BorderLayout.NORTH);
	}

	private void initBodyUI() {
		// bodyPanel
		mBodyPanel = new JPanel();
		mMainFrame.add(mBodyPanel, BorderLayout.CENTER);

		mBodyPanel.setLayout(new BoxLayout(mBodyPanel, BoxLayout.Y_AXIS));

		mFileSelectPanel = new JPanel();
		mFileSelectPanel.setLayout(new BoxLayout(mFileSelectPanel, BoxLayout.Y_AXIS));
		mBodyPanel.add(mFileSelectPanel);

		mFileListText = new JLabel();
		mFileListScrollPanel = new JScrollPane(mFileListText);
		mFileSelectPanel.add(mFileListText);

		mSelectFileBtn = new JButton("选择文件");
		mSendFileBtn = new JButton("发送文件");

		mFileSelectPanel.add(mSelectFileBtn);
		mFileSelectPanel.add(mSendFileBtn);
		
		
		mStatusLabel = new JLabel();
		mStatusLabel.setText("未连接");
		mBodyPanel.add(mStatusLabel);
	}

	private void addListener() {
		mMainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		mListenBtn.addActionListener(new ActionListener() {//监听
			@Override
			public void actionPerformed(ActionEvent e) {
				setHeadPanelEnable(false);
			}
		});

		mConnectBtn.addActionListener(new ActionListener() {// 链接
			@Override
			public void actionPerformed(ActionEvent e) {
				setHeadPanelEnable(false);
			}
		});

		mCancelWorkBtn.addActionListener(new ActionListener() {// 断开链接
			@Override
			public void actionPerformed(ActionEvent e) {
				setHeadPanelEnable(true);
			}
		});
		
		mSelectFileBtn.addActionListener(new ActionListener(){//选择文件按钮
			@Override
			public void actionPerformed(ActionEvent e) {
				addFileToTrans();
			}
		});
	}

	protected void setHeadPanelEnable(boolean enable) {
		if (enable) {
			mServerIPText.setVisible(true);
			mConnectBtn.setVisible(true);
			mListenBtn.setVisible(true);
			mCancelWorkBtn.setVisible(false);
		} else {
			mServerIPText.setVisible(false);
			mConnectBtn.setVisible(false);
			mListenBtn.setVisible(false);
			mCancelWorkBtn.setVisible(true);
		}

		RWConfigFile.writeKey(AppConstants.LAST_CONNECT_ADDRESS, mServerIPText.getText().trim());
	}
	
	/**
	 * 选择文件 添加到待发送列表中
	 */
	private void addFileToTrans(){
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.showDialog(new JLabel(), "选择传输文件");
		File selectFile = chooser.getSelectedFile();
		
	}

	protected void setBodyPanelEnable(boolean enable) {
		setPanelEnable(mBodyPanel, enable);
	}

	public static void setPanelEnable(JPanel panel, boolean enable) {
		if (panel == null)
			return;
		List<Component> lists = listChildComponents(panel);
		for (Component com : lists) {
			com.setEnabled(enable);
		} // end for each
	}

	private static List<Component> listChildComponents(JPanel panel) {
		int count = panel.getComponentCount();
		List<Component> components = new ArrayList<Component>(count);
		for (int i = 0; i < count; i++) {
			if (panel.getComponent(i) instanceof JPanel) {
				components.addAll(listChildComponents((JPanel) panel.getComponent(i)));
			} else {
				components.add(panel.getComponent(i));
			}
		} // end for i
		return components;
	}

}// end class
