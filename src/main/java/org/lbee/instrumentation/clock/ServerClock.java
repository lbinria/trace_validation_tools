package org.lbee.instrumentation.clock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A server clock that can be shared through multiple processes. 
 */
public class ServerClock {
    private ServerSocket serverSocket;
    private long clockValue;

    public synchronized long getNextTime(long clock) {
        clockValue = Math.max(clockValue, clock) + 1;
        return clockValue;
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            new RequestHandler(serverSocket.accept()).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private class RequestHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public RequestHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            System.out.println("New client connected");
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                long newValue = -1;
                while ((inputLine = in.readLine()) != null) {
                    try {
                        long clock = Long.parseLong(inputLine);
                        newValue = getNextTime(clock);
                    } catch (NumberFormatException e) {
                        System.out.println("Received an invalid clock value");
                    }
                    out.println(newValue + "");
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Can't listen on port");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        int port = Integer.parseInt(args[0]);

        ServerClock server = new ServerClock();
        server.start(port);
    }
} 
