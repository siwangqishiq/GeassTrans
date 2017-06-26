package com.xinlan.geasstrans.util;

import com.xinlan.geasstrans.model.AppConstants;

public class VersionUtil {
	public static String getMainTitle(){
		return AppConstants.APP_NAME+"  v_"+AppConstants.VERSION+"."+AppConstants.SUB_VERSION;
	}
	
}//end class
