package com.sztefanov.ajaxbridge;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AjaxBridge implements Runnable {

  private ServerSocket serverSocket;
  private Gson gson;

  public AjaxBridge() {
  }

  private static Map<String, String> getQueryMap(String query) {

    String[] params = query.split("&");
    Map<String, String> map = new HashMap<>();

    for (String param : params) {

      String name = param.split("=")[0];
      String value = param.split("=")[1];

      map.put(name, value);
    }

    return map;
  }

  private Map<String, String> requestGet(String requestHeaders) {

    Map<String, String> get = null;
    String[] split = requestHeaders.split(" ");

    if (split[0].equals("GET")) {

      if (split[1].charAt(0) == '/') {
        split[1] = split[1].substring(1, split[1].length());
      }

      if (split[1].charAt(0) == '?') {
        split[1] = split[1].substring(1, split[1].length());
      }

      get = getQueryMap(split[1]);
    }

    return get;
  }

  private void listen() {

    try {

      gson = new Gson();
      serverSocket = new ServerSocket(50000);

      System.out.println("AjaxBridge: Listening");

      while (true) {

        Socket socket = serverSocket.accept();
        InputStreamReader isr = new InputStreamReader(socket.getInputStream());
        BufferedReader in = new BufferedReader(isr);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        StringBuilder data = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null) {

          if (line.length() == 0) {
            break;
          }

          data.append(line);
        }

        Map<String, String> get = requestGet(data.toString());
        String callback = get.get("callback");

        int[] test = {1, 2, 3};
        
        String json = gson.toJson(test);
        String response = callback + "(" + json + ")";

        out.println("HTTP/1.1 200 OK");
        out.println("Server: AjaxBrige : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: application/javascript");
        out.println("Content-length: " + response.length());
        
        out.println(); // blank between headers and content

        out.println(response);

        out.flush();
        out.close();
        in.close();
        socket.close();
      }
    } catch (IOException ex) {
      Logger.getLogger(AjaxBridge.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void run() {
    listen();
  }

}
