package com.lh.client.service;

import com.lh.util.CommUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * 封装客户端与服务器建立的连接以及输入、输出流
 */

public class Connect2Server {
    private static final Integer PORT;
    private static final String IP;
    private Socket client;
    private InputStream in;
    private OutputStream out;

    static {
        Properties properties = CommUtil.loadProperties("socket.properties");
        PORT = Integer.valueOf(properties.getProperty("port"));
        IP = properties.getProperty("ip");

    }

    public Connect2Server(){
        //与服务器建立连接

        try {
            client = new Socket(IP,PORT);
            this.in = client.getInputStream();
            this.out = client.getOutputStream();
        } catch (IOException e) {
            System.err.println("与服务器建立连接失败");
            e.printStackTrace();
        }

    }

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }
}
