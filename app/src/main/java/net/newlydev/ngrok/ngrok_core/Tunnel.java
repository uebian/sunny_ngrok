package net.newlydev.ngrok.ngrok_core;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;
import org.json.*;
import net.newlydev.ngrok.*;
import org.apache.http.impl.execchain.*;

public class Tunnel
{
	private SSLSocket socket;
    private int localport;
	private String localIP;
    private String proto;
    private String subDomain;
    private String hostname;
    private int remotePort;
    private String httpAuth;
	private String serverAddress;
	private int serverPort;
	private boolean isopen=false;
	private String status="err:等待中...";
	private MainService service;
	private long uploaddata=0;
	private long downloaddata=0;
	private long speed=-1;
	private boolean closeforever=false;
	private MessageListeningThread mlt;

    public Tunnel(String serverAddress, int serverPort, String localIP,int localPort, String proto, String subDomain, String hostname,
				  int remotePort, String httpAuth, MainService service)
	{
		this.localIP=localIP;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
        this.localport = localPort;
        this.proto = proto;
        this.subDomain = subDomain;
        this.hostname = hostname;
        this.remotePort = remotePort;
        this.httpAuth = httpAuth;
		this.service = service;
		new Thread(){
			@Override
			public void run()
			{
				while (!closeforever)
				{
					if (isopen && status.startsWith("err:"))
					{

						try
						{
							close();
							isopen = true;
							connect();
						}
						catch (Exception e)
						{
							unlinked(e.toString());
						}

					}
					try
					{
						Thread.sleep(3000);
					}
					catch (InterruptedException e)
					{}
				}
			}
		}.start();
    }
	public long getUploadData()
	{
		return uploaddata;
	}
	
	public long getDownloadData()
	{
		return downloaddata;
	}
	
	public void setSpeed(long speed)
	{
		this.speed=speed;
		service.notice_tunnel_update();
	}
	
	public long getSpeed()
	{
		return speed;
	}
	
	public void putUploadData(long b)
	{
		uploaddata=uploaddata+b;
		service.notice_tunnel_update();
	}
	
	public void putDownloadData(long b)
	{
		downloaddata=downloaddata+b;
		service.notice_tunnel_update();
	}
	public String getLocalIP()
	{
		// TODO: Implement this method
		return localIP;
	}

	public boolean isOpen()
	{
		return isopen;
	}
    public int getLocalPort()
	{
        return localport;
    }

    public void setLocalPort(int localport)
	{
        this.localport = localport;
    }

    public String getProto()
	{
        return proto;
    }

    public void setProto(String proto)
	{
        this.proto = proto;
    }

    public String getSubDomain()
	{
        return subDomain;
    }

    public void setSubDomain(String subDomain)
	{
        this.subDomain = subDomain;
    }

    public String getHostname()
	{
        return hostname;
    }

    public void setHostname(String hostname)
	{
        this.hostname = hostname;
    }

    public int getRemotePort()
	{
        return remotePort;
    }

    public void setRemotePort(int remotePort)
	{
        this.remotePort = remotePort;
    }

    public String getHttpAuth()
	{
        return httpAuth;
    }

    public void setHttpAuth(String httpAuth)
	{
        this.httpAuth = httpAuth;
    }
	public SSLSocket getSocket()
	{
		return socket;
	}
	public String getServerAddress()
	{
		return serverAddress;
	}
	public int getServerPort()
	{
		return serverPort;
	}
	public void unlinked(String reason)
	{
		speed=-1;
		if (isOpen())
		{
			mlt = null;
			status = "err:" + reason;
			//service.notice_tunnel_update();
		}
		service.notice_tunnel_update();
	}
	public void open()
	{
		isopen = true;
	}
	public void close()
	{
		try
		{
			socket.close();
		}
		catch (Exception e)
		{}
		isopen = false;

		if (mlt != null)
		{
			mlt.closeforever = true;
		}
		mlt = null;
		socket = null;
		System.gc();
		status = "info:(已关闭)";
		service.notice_tunnel_update();
	}
	public void closeforever()
	{
		try
		{
			socket.close();
		}
		catch (Exception e)
		{}
		isopen = false;
		closeforever = true;
		isopen = false;
		if (mlt != null)
		{
			mlt.closeforever = true;
		}
		mlt = null;
		socket = null;
		System.gc();
	}
	private void connect() throws Exception
	{
		status = "info:(正在连接)";
		service.notice_tunnel_update();
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
        socket = (SSLSocket) sslCxt.getSocketFactory().createSocket(serverAddress, serverPort);
		socket.startHandshake();
		JSONObject request = new JSONObject();
		request.put("Type", "Auth");
		JSONObject payload = new JSONObject();
		payload.put("Version", "2");
		payload.put("MmVersion", "1.7");
		payload.put("User", "");
		payload.put("Password", "");
		payload.put("OS", "darwin");
		payload.put("Arch", "amd64");
		payload.put("ClientId", "");
		request.put("Payload", payload);
		Function.sendMessage(socket, request.toString());
		MessageHandler mh=new MessageHandler(this);
		//log.debug("Waiting to read message");
		mlt = new MessageListeningThread(mh, socket);
		mlt.start();
		//MessageHandler.handleMessage(json,l);
		//log.debug("Read message: {}", json.toJSONString());
		//json.toString();

		//log.error("Occurred some exception", e);

		//private final SocketFactory socketFactory;
    }
	public void linked(String remoteUrl)
	{
		this.status = "succ:" + remoteUrl;
		service.notice_tunnel_update();
	}
	public String getStatus()
	{
		return status;
	}
}
