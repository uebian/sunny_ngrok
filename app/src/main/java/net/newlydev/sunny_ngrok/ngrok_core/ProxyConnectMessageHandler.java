package net.newlydev.sunny_ngrok.ngrok_core;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;

import javax.net.ssl.*;

import net.newlydev.sunny_ngrok.*;

import org.json.*;

public class ProxyConnectMessageHandler extends MessageHandler {
    Tunnel tunnel;
    String localIP;
    int localPort;
    String clientAddr = null;
    boolean isstop = false;
    String clientid;
    public Socket locals;
    public SSLSocket socket;
    boolean inputStopped = false, outoutStopped = false;

    @Override
    public SSLSocket getSocket() {
        return socket;
    }

    @Override
    public Tunnel getTunnel() {
        return tunnel;
    }


    public String getClientAddr() {
        return clientAddr;
    }

    @Override
    public void handleMessage(JSONObject json) {
        String type = null;//{"Type":"StartProxy","Payload":{"Url":"tcp:\/\/free.idcfengye.com:10363","ClientAddr":"27.21.120.18:50984"}}
        //JSONObject payload=null;
        try {
            type = json.getString("Type");
            //payload = json.getJSONObject("Payload");
        } catch (JSONException e) {
            return;
        }
        switch (type) {
            case "StartProxy": {
                LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "收到StartProxy消息"));
                try {
                    clientAddr = json.getJSONObject("Payload").getString("ClientAddr");
                    tunnel.getControlConnectMessageHandler().getClientList().add(clientAddr);
                    islistening = false;
                    locals = new Socket(localIP, localPort);
                    new SocketDownThread(this, socket, locals, tunnel).start();
                    new SocketUpThread(this, locals, socket, tunnel).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (Exception err) {
                    }
                }
                break;
            }
            default:
            {
                LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "无法解析的数据类型："+type));
            }
        }
    }


    public void stopInput() {
        inputStopped = true;
        if (outoutStopped && !isstop) {
           close();
        }
    }

    public void stopOutput() {
        outoutStopped = true;
        if (inputStopped && !isstop) {
           close();
        }
    }

    public void close()
    {
        isstop = true;
        LogManager.addLogs(new LogManager.Log("V", tunnel.getSunnyid(), "代理连接关闭"));
        tunnel.getControlConnectMessageHandler().removeProxyConnect(this);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tunnel.getControlConnectMessageHandler().getClientList().remove(clientAddr);
    }
    public ProxyConnectMessageHandler(Tunnel tunnel, String clientid, String serverAddress, int serverPort, String localIP, int localPort) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        islistening = true;
        this.tunnel = tunnel;
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
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
        this.clientid = clientid;
        sendRegProxy();
    }

    public void sendRegProxy() throws IOException {
        JSONObject data = new JSONObject();
        JSONObject payload = new JSONObject();
        try {
            data.put("Type", "RegProxy");
            payload.put("ClientId", clientid);
            data.put("Payload", payload);
        } catch (JSONException e) {
        }
        Function.sendMessage(socket, data.toString());
    }
}
