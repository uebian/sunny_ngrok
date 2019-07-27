package net.newlydev.sunny_ngrok.ngrok_core;

import net.newlydev.sunny_ngrok.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

public class SocketSwapThread extends Thread {
    private InputStream in;
    private Socket inputSocket;
    private Socket outputSocket;
    private OutputStream out;
    Tunnel tunnel;
    private boolean upload;
    ProxyConnectMessageHandler pcmh;

    public SocketSwapThread(ProxyConnectMessageHandler pcmh, Socket input, Socket output, Tunnel tunnel, boolean upload) throws IOException {
        this.in = new BufferedInputStream(input.getInputStream());
        this.out = new BufferedOutputStream(output.getOutputStream());
        this.inputSocket = input;
        this.outputSocket = output;
        this.tunnel = tunnel;
        this.pcmh = pcmh;
        this.upload = upload;
    }

    @Override
    public void run() {
        // 线程运行函数,循环读取返回数据,并发送给相关客户端
        byte buf[] = new byte[Utils.getBufSize()];
        while (true) {
            try {
                int len = in.read(buf);
                if (len > 0) {
                    out.write(buf, 0, len);
                    if (upload) {
                        tunnel.putUploadData(len);
                    } else {
                        tunnel.putDownloadData(len);
                    }
                    out.flush();
                } else if(len<0){
                    break;
                }
            } catch(SocketTimeoutException e)
            {
                try {
                    int len = in.available();
                    len=in.read(buf,0,len);
                    if (len > 0) {
                        out.write(buf, 0, len);
                        if (upload) {
                            tunnel.putUploadData(len);
                        } else {
                            tunnel.putDownloadData(len);
                        }
                        out.flush();
                        continue;
                    } else if(len<0){
                        break;
                    }
                }catch(IOException err)
                {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
        try {
            if (upload) {
                outputSocket.shutdownOutput();
            } else {
                inputSocket.shutdownInput();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(upload)
        {
            pcmh.stopOutput();
        }else {
            pcmh.stopInput();
        }
    }
}
