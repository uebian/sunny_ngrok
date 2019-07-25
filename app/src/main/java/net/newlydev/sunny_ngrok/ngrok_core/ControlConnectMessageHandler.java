package net.newlydev.sunny_ngrok.ngrok_core;

import java.io.*;
import java.util.*;

import javax.net.ssl.*;

//import org.apache.commons.codec.binary.*;
import org.json.*;

import net.newlydev.sunny_ngrok.*;

/**
 * 消息处理器
 */
public class ControlConnectMessageHandler extends MessageHandler {
    private Tunnel tunnel;
    private String clientid;
    private String serverAddress;
    private int serverPort;
    private int localPort;
    private String localIP;
    private SSLSocket socket;
    private ArrayList<ProxyConnectMessageHandler> proxyConnectList = new ArrayList<ProxyConnectMessageHandler>();
    private ArrayList<String> clientList=new ArrayList<String>();
    private long lastPing;

    public ControlConnectMessageHandler(Tunnel tunnel) {
        islistening = true;
        this.tunnel = tunnel;
        this.socket = tunnel.getSocket();
        this.serverAddress = tunnel.getServerAddress();
        this.serverPort = tunnel.getServerPort();
        this.localPort = tunnel.getLocalPort();
        this.localIP = tunnel.getLocalIP();
    }

    public ArrayList<ProxyConnectMessageHandler> getConnects() {
        return proxyConnectList;
    }
    public ArrayList<String> getClientList()
    {
        return clientList;
    }

    public void close() {
        try {
            for (ProxyConnectMessageHandler pcmh : proxyConnectList) {
                pcmh.locals.close();
                pcmh.socket.close();
            }
        } catch (Exception e) {
        }
        try {
            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        islistening = false;
    }

    public Tunnel getTunnel() {
        return tunnel;
    }

    public SSLSocket getSocket() {
        return socket;
    }

    public void sendPing() throws IOException {
        Function.sendMessage(socket, "{\"Type\":\"Ping\",\"Payload\":{}}");
        lastPing = System.currentTimeMillis();
    }

    public void removeProxyConnect(ProxyConnectMessageHandler pcmh) {
        proxyConnectList.remove(pcmh);
    }

    public void handleMessage(final JSONObject json) {
        new Thread() {
            @Override
            public void run() {
                String type = null;
                JSONObject payload = null;
                try {
                    type = json.getString("Type");
                    payload = json.getJSONObject("Payload");
                } catch (JSONException e) {
                    return;
                }
                switch (type) {
                    case "AuthResp":
                        LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到AuthResp消息"));
                        try {
                            clientid = payload.getString("ClientId");
                            String error = payload.getString("Error");
                            if (error.equals("")) {
                                Function.sendReqTunnel(tunnel.getSocket(), tunnel);
                            } else {
                            }
                        } catch (Exception e) {
                        }
                        try {
                            sendPing();
                        } catch (IOException e) {
                        }
                        break;
                    case "Pong":
                        LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "收到Pong消息"));
                        tunnel.setSpeed((System.currentTimeMillis() - lastPing) / 2);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(5000);
                                    sendPing();
                                } catch (Exception e) {
                                    tunnel.unlinked(e.toString());
                                }
                            }
                        }.start();
                        break;
                    case "ReqProxy":
                        LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到ReqProxy消息"));
                        //注册代理需要新的线程和连接
                        try {
                            ProxyConnectMessageHandler messagehandler = new ProxyConnectMessageHandler(tunnel, clientid, serverAddress, serverPort, localIP, localPort);
                            proxyConnectList.add(messagehandler);
                            new MessageListeningThread(messagehandler, messagehandler.getSocket()).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "NewTunnel":
                        LogManager.addLogs(new LogManager.Log("I", tunnel.getSunnyid(), "收到NewTunnel消息"));
                        //String reqId = payload.getString("ReqId");
                        try {
                            String errorn = payload.getString("Error");
                            if (!errorn.equals("")) {
                                tunnel.unlinked(errorn);
                            } else {
                                String url = payload.getString("Url");
                                tunnel.linked(url);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }.start();

    }
}