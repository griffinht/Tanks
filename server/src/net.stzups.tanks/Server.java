package net.stzups.tanks;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Server implements Runnable {
    protected final int PORT;

    protected ServerSocket server;
    private Map<UUID, Client> clients = new HashMap<>();
    protected boolean stopped = false;
    protected Thread thread;

    Server(int port) {
        this.PORT = port;
    }

    public void run() {
        synchronized (this) {
            this.thread = Thread.currentThread();
        }
        // Open new socket
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open on port " + PORT);
        }
        // Main connections loop
        while (!stopped) {
            try {
                new Client(server.accept(), UUID.randomUUID());
                Logger.log("new client");
                //Client client = new Client(server.accept(), UUID.randomUUID());
                //clients.put(client.getUUID(), client);
                //System.out.println("New client with id "+client.getUUID());
            } catch (IOException e) {
                if (stopped) {
                    Logger.log("Server stopped on exception");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        Logger.log("Server stopped");
    }

    synchronized void stop() {
        stopped = true;
        try {
            this.server.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
}
