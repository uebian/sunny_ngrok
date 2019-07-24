package net.newlydev.sunny_ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import net.newlydev.sunny_ngrok.*;
import org.json.*;

public class ProxyConnectMessageHandler extends MessageHandler
{
	Tunnel tunnel;
	String localIP;
	int localPort;
	String clientAddr;
	boolean isstop=false;
	String clientid;
	public Socket locals;
	public SSLSocket socket;
	boolean inputStopped=false,outoutStopped=false;
	@Override
	public SSLSocket getSocket()
	{
		return socket;
	}

	@Override
	public Tunnel getTunnel()
	{
		return tunnel;
	}


	public String getClientAddr(){
		return clientAddr;
	}

	@Override
	public void handleMessage(JSONObject json)
	{
		String type=null;//{"Type":"StartProxy","Payload":{"Url":"tcp:\/\/free.idcfengye.com:10363","ClientAddr":"27.21.120.18:50984"}}
		//JSONObject payload=null;
        try
		{
			type = json.getString("Type");
			//payload = json.getJSONObject("Payload");
		}
		catch (JSONException e)
		{
			return;
		}
        switch (type)
		{
			case "StartProxy": 
				LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "收到StartProxy消息"));
				//String url = payload.getString("Url");
				try {
					clientAddr = json.getJSONObject("Payload").getString("ClientAddr");
				}catch(JSONException e)
				{
					e.printStackTrace();
				}
				try
				{
					locals = new Socket(localIP, localPort);
					new SocketDownThread(this, socket, locals, tunnel).start();
					new SocketUpThread(this, locals, socket, tunnel).start();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					try
					{
						socket.close();
					}
					catch (Exception err)
					{
					}
				}
				LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "停止代理"));
				islistening = false;
				break;
		}
	}


	public void stopInput()
	{
		inputStopped=true;
		if (outoutStopped && !isstop)
		{
			isstop = true;
			LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "代理连接关闭"));
			tunnel.getControlConnectMessageHandler().removeProxyConnect(this);
			try {
				socket.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public void stopOutput()
	{
		outoutStopped=true;
		if (inputStopped && !isstop)
		{
			isstop = true;
			LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "代理连接关闭"));
			tunnel.getControlConnectMessageHandler().removeProxyConnect(this);
			try {
				socket.close();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public ProxyConnectMessageHandler(Tunnel tunnel, String clientid, String serverAddress, int serverPort,  String localIP, int localPort) throws NoSuchAlgorithmException, KeyManagementException, IOException
	{
		islistening = true;
		this.tunnel = tunnel;
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType)
				{
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType)
				{
				}
			}
		};
		SSLContext sslCxt = SSLContext.getInstance("TLSv1.2");
		sslCxt.init(null, trustAllCerts, null);
		SSLSocket socket = (SSLSocket) sslCxt.getSocketFactory().createSocket(serverAddress, serverPort);
		socket.startHandshake();
		this.socket = socket;
		this.localPort = localPort;
		this.localIP = localIP;
		this.clientid = clientid;
		sendRegProxy();
	}

	public void sendRegProxy() throws IOException
	{
		JSONObject data=new JSONObject();
		JSONObject payload=new JSONObject();
		try
		{
			data.put("Type", "RegProxy");
			payload.put("ClientId", clientid);
			data.put("Payload", payload);
		}
		catch (JSONException e)
		{}
		Function.sendMessage(socket, data.toString());
	}
}
