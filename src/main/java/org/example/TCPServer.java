package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPServer {
  private static final int port = 3333;
  private static final int MAX_QUEUE_SIZE = 100000;
  private static final BlockingQueue<ClientRequest> requestQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
  private static final Object lock = new Object();

  //모니터링을 위한 변수
  private static final AtomicInteger activeRequests = new AtomicInteger(0); // 처리 중인 요청 수
  private static final AtomicInteger rejectedRequests = new AtomicInteger(0); // 거부된 요청 수
  private static final AtomicInteger totalRequests = new AtomicInteger(0); // 총 요청 수
  private static final AtomicInteger successRequests = new AtomicInteger(0); // 처리 완료 요청 수

  private static OutputStream outputStream;

  public static void main(String[] args) {
    new Thread(TCPServer::processRequest).start();
//    new Thread(TCPServer::monitorServer).start(); // 서버 상태 모니터링 스레드

    try {
      outputStream = java.nio.file.Files.newOutputStream(Path.of("file"), java.nio.file.StandardOpenOption.CREATE);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("서버가 포트 " + port + "에서 실행 중입니다.");
      while (true) {
        Socket socket = serverSocket.accept();
        System.out.println("클라이언트가 연결되었습니다.");

        new Thread(() ->

            handleClient(socket)

        ).start();
      }
    } catch (IOException e) {
      System.out.println("서버 오류 : " + e.getMessage());
    }
  }

  private static void handleClient (Socket socket) {

    try {
      System.out.println(socket.getInputStream());
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

//    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//         PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
//    {
//      String message;
//      while ((message = in.readLine()) != null) {
////        System.out.println("클라이언트 메시지 : " + message);
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
//            totalRequests.incrementAndGet();
//          } else {
//            out.println("서버가 바쁩니다. 나중에 다시 시도해주세요.");
//            rejectedRequests.incrementAndGet(); // 거부된 요청 수 증가
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
  }

  private static void processRequest() {
    while (true) {
      try {
        ClientRequest request = requestQueue.take();
        activeRequests.incrementAndGet(); // 처리 중인 요청 수 증가

        new Thread(() -> {
          synchronized (lock) {
            System.out.println("처리 중인 요청: " + request.message());
            request.out().println("서버에서 받은 메시지: " + request.message());
          }
          successRequests.incrementAndGet(); // 처리된 요청 수 증가
          System.out.println("=======================");
        }).start();

      } catch (InterruptedException e) {
        System.out.println("메시지 처리 중 오류 : " + e.getMessage());
      } finally {
        activeRequests.decrementAndGet(); // 처리 완료 후 activeRequests 감소
      }
    }
  }

  private static void monitorServer() {
    while (true) {
      try {
        Thread.sleep(10000);
        System.out.println("\n[서버 상태 모니터링]");
        System.out.println("총 요청 수: " + successRequests.get());  // 처리된 요청만 카운트
        System.out.println("처리 중인 요청 수: " + activeRequests.get());
        System.out.println("처리 완료 요청 수: " + successRequests.get());
        System.out.println("대기 중인 요청 수: " + requestQueue.size());
        System.out.println("거부된 요청 수: " + rejectedRequests.get());
        System.out.println("=======================\n");
      } catch (InterruptedException e) {
        System.out.println("모니터링 중 오류 : " + e.getMessage());
      }
    }
  }

  private record ClientRequest(Socket socket, String message, PrintWriter out) {
  }
}
