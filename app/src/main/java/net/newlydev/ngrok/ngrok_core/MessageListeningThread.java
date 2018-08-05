package net.newlydev.ngrok.ngrok_core;

import java.io.*;
import java.nio.*;
import javax.net.ssl.*;
import net.newlydev.ngrok.*;
import org.json.*;

public class MessageListeningThread extends Thread
{
	MessageHandler mh;
	SSLSocket socket;
	public MessageListeningThread(MessageHandler mh, SSLSocket socket)
	{
		this.mh = mh;
		this.socket = socket;
	}
	@Override
	public void run()
	{
		try
		{
			byte[] hLen_old = new byte[8];
			byte[] strByte;
			BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
			while (mh.islistening)
			{
				/*while (bis.available() >= 8)
				{
				}*/
				int i = bis.read(hLen_old);
				if (i == -1)
				{
					return;
				}
				byte[] hLen = new byte[hLen_old.length]; 
				for (int j = 0; j < hLen_old.length; j++)
				{ // 反转后数组的第一个元素等于源数组的最后一个元素：
					hLen[j] = hLen_old[hLen_old.length - j - 1];
				}
				//ArrayUtils.reverse(hLen);
				int strLen = ((Long) ByteBuffer.wrap(hLen).getLong()).intValue();
				//log.debug("Reading message with length: {}", strLen);
				strByte = new byte[strLen];
				int readCount = 0;
				while (readCount < strLen)
				{
					int read = bis.read(strByte, readCount, strLen - readCount);
					if (read == -1)
					{
						return;
					}
					readCount += read;
				}
				JSONObject json = new JSONObject(new String(strByte, "UTF-8"));
				mh.handleMessage(json);
			}
			LogManager.addLogs(new LogManager.Log("V",mh.getTunnel().getSunnyid(),"消息接受线程退出"));
			//socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if(mh instanceof ControlConnectMessageHandler)
			{
				mh.getTunnel().unlinked(e.toString());
			}
		}catch(OutOfMemoryError oom)
		{
			mh.getTunnel().unlinked("发生了OOM错误");
		}
	}
	public MessageHandler getMessageHandler()
	{
		return mh;
	}
}
