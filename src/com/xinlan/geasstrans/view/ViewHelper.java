package com.xinlan.geasstrans.view;

import java.util.List;
import javax.swing.JLabel;
import com.xinlan.geasstrans.model.FileBean;
import com.xinlan.geasstrans.util.FileUtil;

public class ViewHelper {

	public static void refresViewList(JLabel label, List<FileBean> list,boolean isSend) {
		if (label == null || list == null)
			return;
		
		StringBuffer sb = new StringBuffer("<html>");
		if(!isSend){
			sb.append("将要接收的文件列表:<br/>");
		}
		for (FileBean module : list) {
			sb.append(module.getName()).
			append("    "+FileUtil.convertFileSize(module.getSize())).
			append("<br/> ");
		}
		sb.append("</html>");
		label.setText(sb.toString());
	}
	
}// end class
