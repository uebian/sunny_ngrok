package net.newlydev.ngrok.ngrok_core;
import java.net.*;
import java.security.cert.*;
import javax.net.ssl.*;
import net.newlydev.ngrok.*;
import org.json.*;

public class Tunnel
{
	private String sunnyid;
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
	//private boolean isopen=false;
	//private String status="err:等待中...";
	private int status=3;//0 ok,1 linking,2 err,3 close
	String errmsg;
	String remoteURL;
	private MainService service;
	private long uploaddata=0;
	private long downloaddata=0;
	private long speed=-1;
	private MessageListeningThread mlt;

    public Tunnel(String serverAddress, int serverPort, String localIP, int localPort, String proto, String subDomain, String hostname,
				  int remotePort, String httpAuth, MainService service, String sunnyid)
	{
		this.localIP = localIP;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
        this.localport = localPort;
        this.proto = proto;
        this.subDomain = subDomain;
        this.hostname = hostname;
        this.remotePort = remotePort;
        this.httpAuth = httpAuth;
		this.service = service;
		this.sunnyid = sunnyid;
    }
	public long getUploadData()
	{
		return uploaddata;
	}
	public ControlConnectMessageHandler getControlConnectMessageHandler()
	{
		if (mlt != null)
		{
			return (ControlConnectMessageHandler)mlt.getMessageHandler();
		}
		else
		{
			return null;
		}
	}

	public long getDownloadData()
	{
		return downloaddata;
	}

	public void setSpeed(long speed)
	{
		this.speed = speed;
		service.notice_tunnel_update();
	}

	public long getSpeed()
	{
		return speed;
	}

	public void putUploadData(long b)
	{
		uploaddata = uploaddata + b;
		service.notice_tunnel_update();
	}

	public void putDownloadData(long b)
	{
		downloaddata = downloaddata + b;
		service.notice_tunnel_update();
	}
	public String getLocalIP()
	{
		return localIP;
	}

	public boolean isOpen()
	{
		if (status == 3)
		{
			return false;
		}
		else
		{
			return true;
		}
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
	public String getSunnyid()
	{
		return sunnyid;
	}
	public String geterrmsg()
	{
		return errmsg;
	}
	private void realunlinked(String reason)
	{

	}
	public void unlinked(String reason)
	{
		speed = -1;
		if (isOpen())
		{
			close();
			status = 2;
			errmsg = reason;
			service.notice_tunnel_update();
			try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e)
			{}
			if (isOpen())
			{
				open();
			}
			//service.notice_tunnel_update();
		}
		service.notice_tunnel_update();
	}
	public void open()
	{
		LogManager.addLogs(new LogManager.Log("I", sunnyid, "隧道启动"));
		new Thread(){
			@Override
			public void run()
			{
				try
				{
					connect();
				}
				catch (Exception e)
				{
					unlinked(e.toString());
				}
			}
		}.start();
	}
	public void close()
	{
		LogManager.addLogs(new LogManager.Log("I", sunnyid, "隧道关闭"));
		speed = -1;
		if (mlt != null)
		{
			((ControlConnectMessageHandler)mlt.getMessageHandler()).close();
		}
		mlt = null;
		socket = null;
		System.gc();
		//isopen = false;
		status = 3;
		service.notice_tunnel_update();
	}

	private void connect() throws Exception
	{
		if (mlt == null)
		{
			status = 1;
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
			ControlConnectMessageHandler mh=new ControlConnectMessageHandler(this);
			//log.debug("Waiting to read message");
			mlt = new MessageListeningThread(mh, socket);
			mlt.start();
		}
    }
	public void linked(String remoteUrl)
	{
		this.remoteURL = remoteUrl;
		status = 0;
		service.notice_tunnel_update();
	}
	public int getStatus()
	{
		return status;
	}
	public String getRemoteUrl()
	{
		return remoteURL;
	}
}
