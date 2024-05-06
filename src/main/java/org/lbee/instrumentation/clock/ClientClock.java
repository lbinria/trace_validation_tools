package org.lbee.instrumentation.clock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientClock implements InstrumentationClock {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Get a new instance of the client clock.
     * @param ip   address of the server
     * @param port port of the server
     * @return a new instance of the client clock (null if an error occurred)
     */
    public static ClientClock getInstance(String ip, int port) {
        ClientClock clientClock = new ClientClock();
        try {
            clientClock.startConnection(ip, port);
        } catch (IOException e) {
            System.out.println("Error while starting connection: " + e.getMessage());
        }
        return clientClock;  
    }

    @Override
    public long getNextTime(long clock) {
        // request the next time from the server w.r.t. the current clock
        out.println(clock+"");
        long newValue = -1;
        try {
            newValue = Long.parseLong(in.readLine());
        } catch (NumberFormatException | IOException e) {
            System.out.println("Error while getting next time: " + e.getMessage());
        }
        return newValue;
    }

    private void startConnection(String ip, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
