package com.longlive.raw;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Anything that can go wrong will go wrong
 *
 * @author Xingjian LONG <longxingjian@kuaishou.com>
 * @date 2023-05-10
 */
public class IOServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8000);
        //接收新连接线程
        new Thread(() -> {
            while (true) {
                try {
                    //（1）阻塞方法获取新连接
                    Socket socket = serverSocket.accept();
                    // （2）为每一个新连接都创建一个新线程，负责读取数据
                    new Thread(() -> {
                        try {
                            int len;
                            byte[] data = new byte[1024];
                            InputStream inputStream = socket.getInputStream();
                            //（3）按字节流方式读取数据
                            while ((len = inputStream.read(data)) != -1) {
                                System.out.println(
                                        "[" + Thread.currentThread().getName() + "]" + new String(data, 0, len));
                            }
                        } catch (IOException e) {
                        }
                    }).start();
                } catch (IOException e) {
                }
            }
        }).start();
    }
}