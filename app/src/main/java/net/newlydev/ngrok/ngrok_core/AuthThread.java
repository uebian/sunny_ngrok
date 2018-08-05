package net.newlydev.ngrok.ngrok_core;

import android.os.*;
import android.util.*;
import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;
import net.newlydev.ngrok.*;
import org.json.*;

public class AuthThread extends Thread
{

	ArrayList<String> clientid=new ArrayList<String>();
	ArrayList<String> authdata=new ArrayList<String>();
	Handler callback;

	public AuthThread(ArrayList<String> clientids, Handler callback) throws Exception
	{
		this.callback = callback;
		this.clientid=clientids;
	}
	@Override
	public void run()
	{
		for(int i=0;i<clientid.size();i++){
			checkonetunne(i);
			Message msg=new Message();
			Bundle data=new Bundle();
			data.putInt("type",1);
			data.putInt("progress",i+1);
			msg.setData(data);
			callback.sendMessage(msg);
			LogManager.addLogs(new LogManager.Log("I",clientid.get(i),"隧道信息获取成功"));
		}
		Message msg=new Message();
		Bundle data=new Bundle();
		data.putInt("type",0);
		data.putStringArrayList("data",authdata);
		msg.setData(data);
		callback.sendMessage(msg);
	}
	private void checkonetunne(int i)
	{
		String id=clientid.get(i);
		String host = "www.ngrok.cc";
		int port = 443;
		Socket client=null;
		BufferedOutputStream bos=null;
		DataInputStream bis=null;
		try
		{
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			client = (SSLSocket) sslsocketfactory.createSocket(host, port);
			bos = new BufferedOutputStream(client.getOutputStream());
			bis = new DataInputStream(client.getInputStream());

		}
		catch (Exception e)
		{
			
			authdata.add("err:无法连接到www.ngrok.cc:" + e.toString() + "\n检查你的网络连接?");
			try
			{
				client.close();
			}
			catch (Exception eee)
			{}
			return;
		}
		String header = "POST " + "/api/clientid/clientid/" + id + " HTTP/1.1" + "\r\n";
		header += "Content-Type: text/html" + "\r\n";
		header += "Host:" + host + "\r\n";
		header += "\r\n";
		try
		{
			bos.write(header.getBytes("UTF-8"));
			bos.flush();
		}
		catch (IOException e)
		{
			
			authdata.add("err:向www.ngrok.cc发送数据时出错:" + e.toString() + "\n检查你的网络连接?");
			try
			{
				client.close();
			}
			catch (Exception eee)
			{}
			return;
		}
		String data="err";
		try
		{
			while (true)
			{
				String tmp=bis.readLine();
				if (tmp.startsWith("{") && tmp.endsWith("}"))
				{
					data = tmp;
					break;
				}
			}
		}
		catch (IOException e)
		{
			Log.v("ngrok", "get data error:" + e.toString());
		}
		try
		{
			client.close();
		}
		catch (IOException e)
		{}
		if (data.equals("err"))
		{
			authdata.add("err:www.ngrok.cc没有返回数据，请联系开发者");
			return;
		}
		try
		{
			JSONObject authData=new JSONObject(data);
			authData.put("sunnyid",id);
			if (authData.getInt("status") != 200)
			{
				authdata.add("err:没有通过服务器的验证，检查你的输入？\n来自服务器的消息:\n" + Utils.unicodeToString(authData.getString("msg")));
			}
			else
			{
				authdata.add(authData.toString());
			}
		}
		catch (JSONException e)
		{
			authdata.add("err:www.ngrok.cc返回的数据格式未知，请联系开发者");
			return;
		}
	}

}
