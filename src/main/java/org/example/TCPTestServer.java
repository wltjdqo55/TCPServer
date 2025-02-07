package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class TCPTestServer {
  private static final int port = 3333;
  private static final Object lock = new Object();

  private static final List<Socket> sockets = new LinkedList<Socket>();
  public static void main(String[] args) {
    new Thread(()->{
      while (true) {
        for (Socket socket : sockets) {
          System.out.println("socket:" + socket);
          handleClient(socket);
        }
      }
    }).start();

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        Socket socket = serverSocket.accept();
//        sockets.add(socket);
        new Thread(() -> handleClient(socket)).start();
      }
    } catch (IOException e) {
      System.out.println("서버 오류 : " + e.getMessage());
    }
  }

  private static void handleClient (Socket socket) {
    try (InputStream is = socket.getInputStream()) {
      while (is.available() > 0) {
        outer:
        {
          synchronized (lock) {
            int r = is.read();
            for (; r > -1; r = is.read()) {
              System.err.write(r);
              if (r == '\n') {
//                  Thread.sleep(1);
                break outer;
              }
            }
          }
        }
      }

      System.err.println("end of client");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
