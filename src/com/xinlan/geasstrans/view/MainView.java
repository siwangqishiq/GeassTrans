package com.xinlan.geasstrans.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.xinlan.geasstrans.controller.NetStatus;
import com.xinlan.geasstrans.controller.NetWork;
import com.xinlan.geasstrans.controller.NetWork.INetWorkCallback;
import com.xinlan.geasstrans.controller.RWConfigFile;
import com.xinlan.geasstrans.exception.FileTransException;
import com.xinlan.geasstrans.model.AppConstants;
import com.xinlan.geasstrans.model.FileBean;
import com.xinlan.geasstrans.util.FileUtil;
import com.xinlan.geasstrans.util.VersionUtil;

public class MainView {

	public static final int VIEW_WIDTH = 500;
	public static final int VIEW_HEIGHT = 400;

	public static final int PADDING = 10;

	protected JFrame mMainFrame;

	protected JPanel mHeadPanel;
	protected JTextField mServerIPText;
	protected JButton mConnectBtn;
	protected JButton mListenBtn;
	protected JButton mCancelWorkBtn;

	protected JPanel mBodyPanel;

	protected JPanel mFileSelectPanel;
	protected JLabel mFileListText;
	protected JButton mSelectFileBtn;
	protected JButton mSendFileBtn;
	protected JButton mCancelSendBtn;

	protected JLabel mStatusLabel;
	protected JProgressBar mProgressBar;

	private List<FileBean> mSelectedList = new ArrayList<FileBean>(10);
	private List<FileBean> mReceiveList = new ArrayList<FileBean>();

	private NetWork mNetWork;

	public static void main(String agrs[]) {
		new MainView().execute();
	}

	public void execute() {
		initUI();
		mNetWork = new NetWork();
	}

	private void asServer() {
		mStatusLabel.setText("启动服务 等待连接...");
		mNetWork.startServerListen(new INetWorkCallback() {
			@Override
			public void onConnectSuccess(String remote) {
				// System.out.println(remote+"建立连接");
				setPanelEnable(mBodyPanel, true);
			}

			@Override
			public void onConnectFail(Exception e) {
				// System.out.println("连接失:"+e);
				setHeadPanelEnable(true);
				setPanelEnable(mBodyPanel, false);
			}

			@Override
			public void onRemoteDisconnect() {
				setHeadPanelEnable(true);
				setPanelEnable(mBodyPanel, false);
			}

			@Override
			public void onReceiveFilesInfoList(List<FileBean> receiveList) {
				handleOnReceiveFilesInfo(receiveList);
			}

			@Override
			public void onReceiveFilesComplete(List<FileBean> list) {
				setPanelEnable(mBodyPanel, true);

				resetListUI();
			}

			@Override
			public void onSendFilesComplete(List<FileBean> list) {
				setPanelEnable(mBodyPanel, true);

				resetListUI();
			}

			@Override
			public void onStatusChange(NetStatus fromStatus, NetStatus curStatus) {
				updateByStatus(curStatus);
			}

			@Override
			public void onFileProgressUpdate(FileBean fileBean, String filename, long cur, long fileSize, long total, long progress, boolean isSend) {
				updateProgressUI(fileSize, cur, total, progress, filename, isSend ? "接收文件" : "发送文件");
			}

			@Override
			public void onTransError(FileTransException e) {

			}
		});

		setHeadPanelEnable(false);
	}

	private void asClient() {
		String serverIp = mServerIPText.getText().trim();
		mNetWork.startConnect(serverIp, new INetWorkCallback() {
			@Override
			public void onConnectSuccess(String remote) {
				// System.out.println("与服务端"+remote+"建立连接");
				setHeadPanelEnable(false);
				setPanelEnable(mBodyPanel, true);
			}

			@Override
			public void onConnectFail(Exception e) {
				// System.out.println("连接服务端失败 : "+e);
				setHeadPanelEnable(true);
				setPanelEnable(mBodyPanel, false);
			}

			@Override
			public void onRemoteDisconnect() {
				setHeadPanelEnable(true);
				setPanelEnable(mBodyPanel, false);
			}

			@Override
			public void onReceiveFilesInfoList(List<FileBean> receiveList) {
				handleOnReceiveFilesInfo(receiveList);
			}

			@Override
			public void onReceiveFilesComplete(List<FileBean> list) {
				resetListUI();
			}

			@Override
			public void onSendFilesComplete(List<FileBean> list) {
				resetListUI();
			}

			@Override
			public void onStatusChange(NetStatus fromStatus, NetStatus curStatus) {
				updateByStatus(curStatus);
			}

			@Override
			public void onFileProgressUpdate(FileBean fileBean, String filename, long cur, long fileSize, long progress, long total, boolean isSend) {
				updateProgressUI(fileSize, cur, progress, total, filename, isSend ? "接收文件" : "发送文件");
			}

			@Override
			public void onTransError(FileTransException e) {

			}
		});
	}

	private void resetListUI() {
		mReceiveList.clear();
		refreshReceiveListFileUI();

		mSelectedList.clear();
		refreshSendListFileUI();

		mProgressBar.setValue(0);
	}

	private void updateProgressUI(long thisFileTotal, long thisFileCurrent, long curProgress, long total, String filename, String prefix) {
		float fileProgress = 100 * ((float) thisFileCurrent) / thisFileTotal;
		mStatusLabel.setText(prefix + filename + ": " + String.format("%.2f", fileProgress) + "%");

		float progress = 100 * ((float) curProgress) / total;
		mProgressBar.setValue((int) progress);
	}

	private void updateByStatus(NetStatus curStatus) {
		switch (curStatus) {
		case UNCONNECT:// 未连接状态
			mStatusLabel.setText("未建立连接");
			break;
		case CONNECT:// 连接建立状态
			mStatusLabel.setText("连接建立 等待任务");
			break;
		case SEND:// 发送状态
			mStatusLabel.setText("发送文件");
			break;
		case RECEIVE:// 接收状态
			mStatusLabel.setText("接收文件");
			break;
		default:
			break;
		}// end switch
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
		// mMainFrame.setResizable(false);

		setPanelEnable(mBodyPanel, false);
	}

	private void initHeadUI() {
		mHeadPanel = new JPanel();
		mHeadPanel.setLayout(new BorderLayout());
		mServerIPText = new JTextField(20);
		mServerIPText.setText(RWConfigFile.readKey(AppConstants.LAST_CONNECT_ADDRESS));
		mHeadPanel.add(mServerIPText, BorderLayout.CENTER);

		JPanel headBtnsPanel = new JPanel();
		mConnectBtn = new JButton("连接");
		headBtnsPanel.add(mConnectBtn);
		mListenBtn = new JButton("监听");
		headBtnsPanel.add(mListenBtn);
		mCancelWorkBtn = new JButton("断开连接");
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
		mFileSelectPanel.add(mFileListText);

		mSelectFileBtn = new JButton("选择文件");
		mSendFileBtn = new JButton("发送文件");
		mSendFileBtn.setVisible(false);
		mCancelSendBtn = new JButton("取消选择");

		mFileSelectPanel.add(mSelectFileBtn);
		mFileSelectPanel.add(mSendFileBtn);
		mFileSelectPanel.add(mCancelSendBtn);

		mStatusLabel = new JLabel();
		mStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));
		mStatusLabel.setText("未连接");
		mBodyPanel.add(mStatusLabel);

		mProgressBar = new JProgressBar();
		mProgressBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		mBodyPanel.add(mProgressBar);
		mProgressBar.setMinimum(0);
		mProgressBar.setMaximum(100);
		mProgressBar.setValue(0);
	}

	private void addListener() {
		mMainFrame.addWindowListener(new WindowAdapter() {// 退出
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});

		mListenBtn.addActionListener(new ActionListener() {// 监听
			@Override
			public void actionPerformed(ActionEvent e) {
				// setHeadPanelEnable(false);
				asServer();
			}
		});

		mConnectBtn.addActionListener(new ActionListener() {// 连接
			@Override
			public void actionPerformed(ActionEvent e) {
				// setHeadPanelEnable(false);
				asClient();
			}
		});

		mCancelWorkBtn.addActionListener(new ActionListener() {// 断开连接
			@Override
			public void actionPerformed(ActionEvent e) {
				mNetWork.disConnection();
				setHeadPanelEnable(true);
				setPanelEnable(mBodyPanel, false);
			}
		});

		mSelectFileBtn.addActionListener(new ActionListener() {// 选择文件按钮
			@Override
			public void actionPerformed(ActionEvent e) {
				addFileToTrans();
			}
		});

		mCancelSendBtn.addActionListener(new ActionListener() {// 取消选中的文件
			@Override
			public void actionPerformed(ActionEvent e) {
				mSelectedList.clear();
				refreshSendListFileUI();
			}
		});

		mSendFileBtn.addActionListener(new ActionListener() {// 发送文件
			@Override
			public void actionPerformed(ActionEvent e) {
				doSendFiles();
			}
		});
	}

	/**
	 * do send files
	 */
	private void doSendFiles() {
		if (mSelectedList.size() == 0)
			return;

		// setHeadPanelEnable(false);
		// mCancelWorkBtn.setEnabled(false);
		mNetWork.addSendTask(mSelectedList);
	}

	private void refreshSendListFileUI() {
		ViewHelper.refresViewList(mFileListText, mSelectedList, true);
		mSendFileBtn.setVisible(!(mSelectedList.size() == 0));
	}

	private void refreshReceiveListFileUI() {
		ViewHelper.refresViewList(mFileListText, mReceiveList, false);
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

	private void closeWindow() {
		if (mNetWork != null) {
			mNetWork.closeNetWork();
		}

		System.exit(0);
	}

	/**
	 * 选择文件 添加到待发送列表中
	 */
	private void addFileToTrans() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		String lastOpenDir = RWConfigFile.readKey(AppConstants.LAST_OPEN_FOLDER);
		if (lastOpenDir != null) {
			File dir = new File(lastOpenDir);
			if (dir.exists()) {
				chooser.setCurrentDirectory(dir);
			}
		}

		chooser.showDialog(new JLabel(), "选择传输文件");

		File selectFile = chooser.getSelectedFile();
		// System.out.println("selectFile ---> "+selectFile.getAbsolutePath());
		if (selectFile != null) {
			mSelectedList.add(FileBean.create(selectFile.getAbsolutePath()));
			refreshSendListFileUI();

			RWConfigFile.writeKey(AppConstants.LAST_OPEN_FOLDER, selectFile.getParent());
		}
	}

	private void handleOnReceiveFilesInfo(List<FileBean> list) {
		mReceiveList.addAll(list);
		refreshReceiveListFileUI();
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
