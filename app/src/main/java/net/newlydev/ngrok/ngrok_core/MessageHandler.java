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

/**
 * 消息处理器
 */
public class MessageHandler
{
	private Tunnel tunnel;
	private String clientid;
	private String serverAddress;
	private int serverPort;
	private int localPort;
	private String localIP;
	private SSLSocket socket;
	private long lastPing;
	public MessageHandler(Tunnel tunnel)
	{
		this.tunnel = tunnel;
		this.socket = tunnel.getSocket();
		this.serverAddress = tunnel.getServerAddress();
		this.serverPort = tunnel.getServerPort();
		this.localPort = tunnel.getLocalPort();
		this.localIP = tunnel.getLocalIP();
	}
	public MessageHandler(String serverAddress, int serverPort, String clientid, int localPort, String localIP,Tunnel tunnel) throws NoSuchAlgorithmException, KeyManagementException, IOException
	{
		this.clientid = clientid;
		this.tunnel=tunnel;
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
	}

	public void close()
	{
		try
		{
				socket.close();
		}
		catch (Exception e)
		{}
		// TODO: Implement this method
	}
	public SSLSocket getSocket()
	{
		return socket;
	}
	public void sendPing() throws IOException
	{
		Function.sendMessage(socket, "{\"Type\":\"Ping\",\"Payload\":{}}");
		lastPing=System.currentTimeMillis();
		// TODO: Implement this method
	}
    public boolean handleMessage(JSONObject json) throws Exception
	{
        String type = json.getString("Type");
        JSONObject payload = json.getJSONObject("Payload");
        switch (type)
		{
			case "Error":
				tunnel.unlinked(payload.getString("info"));
				break;
            case "AuthResp": 
				clientid = payload.getString("ClientId");
				sendPing();
				String error = payload.getString("Error");
				if (error.equals(""))
				{
					Function.sendReqTunnel(tunnel.getSocket(), tunnel);
					//new HeartThread(this).start();
				}
				else
				{
				}
				break;
			case "Pong":
				tunnel.setSpeed((System.currentTimeMillis()- lastPing)/2);
				Thread.sleep(3000);
				sendPing();
				break;
			case "ReqProxy":
				//注册代理需要新的线程和连接
				MessageHandler messagehandler = new MessageHandler(serverAddress, serverPort, clientid, localPort, localIP,tunnel);
				new MessageListeningThread(messagehandler, messagehandler.getSocket()).start();
				messagehandler.sendRegProxy();
				break;
			case "NewTunnel": 
				//String error = payload.getString("Error");
				String reqId = payload.getString("ReqId");
				
				String errorn = payload.getString("Error");
				
				if (!errorn.equals("")) {
					tunnel.unlinked(errorn);
				 }else{
					 String url = payload.getString("Url");
					 tunnel.linked(url);
				 }
				break;

			case "StartProxy": 
				//String url = payload.getString("Url");
				Socket locals = new Socket(localIP, localPort);
				//Thread.sleep(10);
				new SocketDownThread(socket, locals,tunnel).start();
				new SocketUpThread(locals, socket,tunnel).start();
				return true;
		}
		return false;
	}
	
	public void sendRegProxy() throws IOException
	{
		Function.sendMessage(socket, "{\"Type\":\"RegProxy\",\"Payload\":{\"ClientId\":\"" + clientid + "\"}}");
	}
}

