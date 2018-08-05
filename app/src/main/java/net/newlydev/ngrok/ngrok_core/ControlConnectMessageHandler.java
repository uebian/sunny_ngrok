package net.newlydev.ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import javax.net.ssl.*;
import net.newlydev.ngrok.ngrok_core.*;
import org.apache.commons.codec.binary.*;
import org.json.*;
import java.security.cert.*;
import java.security.*;
import android.os.health.*;
import java.security.acl.*;
import net.newlydev.ngrok.*;

/**
 * 消息处理器
 */
public class ControlConnectMessageHandler extends MessageHandler
{
	private Tunnel tunnel;
	private String clientid;
	private String serverAddress;
	private int serverPort;
	private int localPort;
	private String localIP;
	private SSLSocket socket;
	private ArrayList<ProxyConnectMessageHandler> proxyconnectlist=new ArrayList<ProxyConnectMessageHandler>();
	private long lastPing;
	public ControlConnectMessageHandler(Tunnel tunnel)
	{
		islistening = true;
		this.tunnel = tunnel;
		this.socket = tunnel.getSocket();
		this.serverAddress = tunnel.getServerAddress();
		this.serverPort = tunnel.getServerPort();
		this.localPort = tunnel.getLocalPort();
		this.localIP = tunnel.getLocalIP();
	}

	public void close()
	{
		try
		{
			for (ProxyConnectMessageHandler pcmh:proxyconnectlist)
			{
				pcmh.locals.close();
				pcmh.socket.close();
			}
		}catch (Exception e){
			
		}
		try{
			socket.close();
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
		islistening=false;
		// TODO: Implement this method
	}
	public Tunnel getTunnel()
	{
		return tunnel;
	}
	public SSLSocket getSocket()
	{
		return socket;
	}
	public void sendPing() throws IOException
	{
		Function.sendMessage(socket, "{\"Type\":\"Ping\",\"Payload\":{}}");
		lastPing = System.currentTimeMillis();
		// TODO: Implement this method
	}
	public void removeProxyConnect(ProxyConnectMessageHandler pcmh)
	{
		proxyconnectlist.remove(pcmh);
	}
    public void handleMessage(JSONObject json)
	{
		String type=null;
		JSONObject payload=null;
        try
		{
			type = json.getString("Type");
			payload = json.getJSONObject("Payload");
		}
		catch (JSONException e)
		{
			return;
		}
        switch (type)
		{
            case "AuthResp": 
				LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到AuthResp消息"));
				try
				{

					clientid = payload.getString("ClientId");
					String error = payload.getString("Error");
					if (error.equals(""))
					{
						Function.sendReqTunnel(tunnel.getSocket(), tunnel);
					}
					else
					{
					}
				}
				catch (Exception e)
				{

				}
				try
				{
					sendPing();
				}
				catch (IOException e)
				{}
				break;
			case "Pong":
				LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "收到Pong消息"));



				tunnel.setSpeed((System.currentTimeMillis() - lastPing) / 2);
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(3000);
							sendPing();
						}
						catch (Exception e)
						{
							tunnel.unlinked(e.toString());
						}
						
					}
				}.start();
				


				break;
			case "ReqProxy":
				LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到ReqProxy消息"));
				//注册代理需要新的线程和连接
				try
				{
					ProxyConnectMessageHandler messagehandler = new ProxyConnectMessageHandler(tunnel, clientid, serverAddress, serverPort, localIP, localPort);
					proxyconnectlist.add(messagehandler);
					new MessageListeningThread(messagehandler, messagehandler.getSocket()).start();
					//messagehandler.sendRegProxy();
				}
				catch (Exception e)
				{

				}
				break;
			case "NewTunnel": 
				LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到NewTunnel消息"));
				//String reqId = payload.getString("ReqId");
				try
				{
					String errorn = payload.getString("Error");

					if (!errorn.equals(""))
					{
						tunnel.unlinked(errorn);
					}
					else
					{
						String url = payload.getString("Url");
						tunnel.linked(url);
					}
				}
				catch (Exception e)
				{

				}
				break;

		}
	}


}

