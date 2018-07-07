package net.newlydev.ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

/**
 * 代理消息
 */
public class SocketUpThread extends Thread
{
    //Logger log = LoggerFactory.getLogger(HealthCheckWorker.class);

    private InputStream in;
	Socket inputSocket;
	Socket outputSocket;
    private OutputStream out;
	Tunnel  tunnel;

    public SocketUpThread(Socket input, SSLSocket output,Tunnel tunnel) throws IOException
	{
        this.in = new BufferedInputStream(input.getInputStream());
        this.out = new BufferedOutputStream(output.getOutputStream());
		this.inputSocket = input;
		this.outputSocket = output;
		this.tunnel=tunnel;
    }
	@Override
    public void run()
	{
        // 线程运行函数,循环读取返回数据,并发送给相关客户端

		byte[] buf = new byte[1024];
		while (true)
		{
			try
			{
				int len = in.read(buf);
				//if (log.isTraceEnabled())
				//    log.tracef("%s read %s bytes", name, len);
				if (len > 0)
				{
					out.write(buf, 0, len);
					tunnel.putUploadData(len);
					//count += len;
					out.flush();
				}
				else if (len < 0)
				{
					//log.debug("break at len="+len);
					break;
				}
			}
			catch (IOException e)
			{
				//log.debug("break at IOException");
				break;
			}
		}
		try
		{
			outputSocket.shutdownOutput();
		}
		catch (IOException e)
		{}
    }
}

