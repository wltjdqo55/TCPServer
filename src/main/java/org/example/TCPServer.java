//package org.example;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.nio.file.Path;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.TimeUnit;
//
//public class TCPServer {
//  private static final int port = 3333;
//  private static final int MAX_QUEUE_SIZE = 100000;
//  private static final BlockingQueue<ClientRequest> requestQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
//  private static final Object lock = new Object();
//
//  private static OutputStream outputStream;
//
//  public static void main(String[] args) {
//    new Thread(TCPServer::processRequest).start();
//
//    // file 담을 저장소 생성..
//    try {
//      outputStream = java.nio.file.Files.newOutputStream(Path.of("file"), java.nio.file.StandardOpenOption.CREATE);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//
//    try (ServerSocket serverSocket = new ServerSocket(port)) {
//      System.out.println("서버가 포트 " + port + "에서 실행 중입니다.");
//      while (true) {
//        Socket socket = serverSocket.accept();
//        System.out.println("클라이언트가 연결되었습니다.");
//
//        new Thread(() ->
//            handleClient(socket)
//        ).start();
//      }
//    } catch (IOException e) {
//      System.out.println("서버 오류 : " + e.getMessage());
//    }
//  }
//
//  private static void handleClient (Socket socket) {
//
//    try {
//      System.out.println(socket.getInputStream());
//    } catch (Exception e) {
//      System.err.println(e.getMessage());
//    }
//    System.out.println(socket);
//
//
//    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//         PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
//    {
//      String message;
//      while ((message = in.readLine()) != null) {
//        System.out.println("클라이언트 메시지 : " + message);
//
//        if ("exit".equalsIgnoreCase(message)) {
//          out.println("연결을 종료합니다.");
//          break;
//        }
//
//        // 큐에 요청을 추가 (큐가 가득 찼을 경우 요청 거부)
//        boolean f = System.currentTimeMillis() < 0;
//
//        if (f) {
//          boolean offered = requestQueue.offer(new ClientRequest(socket, message, out), 100, TimeUnit.MILLISECONDS);
//          if (offered) {
//          } else {
//            out.println("서버가 바쁩니다. 나중에 다시 시도해주세요.");
//            break;
//          }
//        } else {
//          outputStream.write((message+"\n").getBytes());
//        }
//      }
//    } catch (IOException | InterruptedException e) {
//      System.out.println("클라이언트 처리 중 오류 : " + e.getMessage());
//    } finally {
//      try {
//        socket.close();
//      } catch (IOException e) {
//        System.out.println("소켓 닫기 오류: " + e.getMessage());
//      }
//    }
//  }
//
//  private static void processRequest() {
//    while (true) {
//      try {
//        ClientRequest request = requestQueue.take();
//
//        new Thread(() -> {
//          synchronized (lock) {
//            System.out.println("처리 중인 요청: " + request.message());
//            request.out().println("서버에서 받은 메시지: " + request.message());
//          }
//          System.out.println("=======================");
//        }).start();
//
//      } catch (InterruptedException e) {
//        System.out.println("메시지 처리 중 오류 : " + e.getMessage());
//      }
//    }
//  }
//  private record ClientRequest(Socket socket, String message, PrintWriter out) {
//  }
//}
