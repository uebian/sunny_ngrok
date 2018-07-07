package net.newlydev.ngrok;

import android.app.*;
import android.content.*;
import java.util.regex.*;

public class Utils
{
	public static String unicodeToString(String str) {

		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			String group = matcher.group(2);
			ch = (char) Integer.parseInt(group, 16);
			String group1 = matcher.group(1);
			str = str.replace(group1, ch + "");
		}
		return str;
	}
	public static boolean isMainServiceRunning(Context context) {
	    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("net.newlydev.ngrok.MainService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
