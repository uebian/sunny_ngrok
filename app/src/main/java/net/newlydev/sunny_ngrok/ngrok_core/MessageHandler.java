package net.newlydev.sunny_ngrok.ngrok_core;
import org.json.*;
import javax.net.ssl.*;

public abstract class MessageHandler
{
	public boolean islistening;
	public abstract void handleMessage(JSONObject data);
	public abstract Tunnel getTunnel();
	public abstract SSLSocket getSocket();
}
