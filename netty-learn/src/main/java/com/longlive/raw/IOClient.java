package com.longlive.raw;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

/**
 * Anything that can go wrong will go wrong
 *
 * @author Xingjian LONG <longxingjian@kuaishou.com>
 * @date 2023-05-10
 */
public class IOClient {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    Socket socket = new Socket("127.0.0.1", 8000);
                    while (true) {
                        try {
                            socket.getOutputStream().write((new Date() + ": hello world").getBytes());
                            Thread.sleep(2000);
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e) {
                }
            }).start();
        }
    }
}