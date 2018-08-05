package net.newlydev.ngrok;
import android.widget.*;
import java.util.*;

public class LogManager
{
	private static ArrayList<Log> logs=new ArrayList<Log>();
	public static class Log
	{
		public int year,month,day,hour,minute,second,millisecoud;
		public String type,tunnelid,msg;
		public long threadid;
		public Log(String type,String tunnelid,String msg)
		{
			Calendar ca = Calendar.getInstance();
			year = ca.get(Calendar.YEAR);//获取年份
			month=ca.get(Calendar.MONTH);//获取月份
			day=ca.get(Calendar.DATE);//获取日
			minute=ca.get(Calendar.MINUTE);//分
			hour=ca.get(Calendar.HOUR_OF_DAY);//小时
			second=ca.get(Calendar.SECOND);//秒
			millisecoud=ca.get(Calendar.MILLISECOND);
			this.type=type;
			this.tunnelid=tunnelid;
			this.msg=msg;
			threadid=Thread.currentThread().getId();
		}
	}
	public static void addLogs(Log log)
	{
		logs.add(0,log);
	}
	public static ArrayList<Log> getLogs()
	{
		return logs;
	}
}
