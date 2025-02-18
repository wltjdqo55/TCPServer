package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.file.Files.newOutputStream;

public class TCPTestServer {
  private static final int port = 3333;
  private static final Object lock = new Object();

  private static final BlockingQueue<String> q = new LinkedBlockingQueue<>();
  private static final Path FILE_PATH = Path.of("file");

  public static void main(String[] args) {
    new Thread(TCPTestServer::writeToFile).start();

    try {
      OutputStream outputStream = newOutputStream(FILE_PATH, java.nio.file.StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (true) {
        Socket socket = serverSocket.accept();
        new Thread(() -> handleClient(socket)).start();
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void handleClient(Socket socket) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        q.put(line);
      }
      System.err.println("end of client");
    } catch (IOException | InterruptedException e) {
      System.err.println("error : " + e.getMessage());
    }
  }

  private static void writeToFile () {
    try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
      while (true) {
        String data = q.take();
        writer.write(data + "\n");
        writer.flush();
        System.err.println("file save : " + data);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
