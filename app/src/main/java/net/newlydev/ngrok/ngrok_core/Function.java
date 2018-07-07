package net.newlydev.ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import org.json.*;

public class Function
{
	public static void sendMessage(Socket socket, String str) throws IOException
	{

		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		byte[] len = ByteBuffer.allocate(8).putLong(str.length()).array();
		for (int start = 0, end = len.length - 1; start < end; start++, end--)
		{
			byte temp = len[end];
			len[end] = len[start];
			len[start] = temp;
		}

		ByteBuffer wrap = ByteBuffer.allocate(str.length() + 8);
		byte[] array = wrap.put(len).put(str.getBytes()).array();
		bos.write(array);
		bos.flush();

    }

	public static void sendReqTunnel(Socket socket,Tunnel tunnel) throws JSONException, IOException
	{
		JSONObject reuqest = new JSONObject();
		reuqest.put("Type", "ReqTunnel");

		JSONObject payload = new JSONObject();
		String reqId = UUID.randomUUID().toString()
			.toLowerCase().replace("-", "")
			.substring(0, 16);
		payload.put("ReqId", reqId);
		payload.put("Protocol", tunnel.getProto());
		if (tunnel.getProto().equals("tcp"))
		{
			payload.put("RemotePort", tunnel.getRemotePort());
		}
		else
		{
			payload.put("Subdomain", tunnel.getSubDomain());
			payload.put("HttpAuth", tunnel.getHttpAuth());
			payload.put("Hostname", tunnel.getHostname());
		}
		reuqest.put("Payload", payload);
		sendMessage(socket,reuqest.toString());
	}
    
}
