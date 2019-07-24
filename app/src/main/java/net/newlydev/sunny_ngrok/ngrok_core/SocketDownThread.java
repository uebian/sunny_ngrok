package net.newlydev.sunny_ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

/**
 * 代理消息
 */
public class SocketDownThread extends Thread
{
    //Logger log = LoggerFactory.getLogger(HealthCheckWorker.class);
    private InputStream in;
	private Socket inputSocket;
	private Socket outputSocket;
    private OutputStream out;
	Tunnel tunnel;
	ProxyConnectMessageHandler pcmh;
    public SocketDownThread(ProxyConnectMessageHandler pcmh,SSLSocket input, Socket output,Tunnel tunnel) throws IOException
	{
        this.in = new BufferedInputStream(input.getInputStream());
        this.out = new BufferedOutputStream(output.getOutputStream());
		this.inputSocket = input;
		this.outputSocket = output;
		this.tunnel=tunnel;
		this.pcmh=pcmh;
    }
	@Override
    public void run()
	{
        // 线程运行函数,循环读取返回数据,并发送给相关客户端
		byte[] buf = new byte[4096];
		while (true)
		{
			try
			{
				int len = in.read(buf);
				if (len > 0)
				{
					out.write(buf, 0, len);
					tunnel.putDownloadData(len);
					out.flush();
				}
				else
				{
					break;
				}
			}
			catch (IOException e)
			{
				break;
			}
		}
		try {
			inputSocket.shutdownInput();
		}catch (IOException e)
		{
			e.printStackTrace();
		}
		pcmh.stopInput();
    }
}

