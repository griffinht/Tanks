package net.stzups.tanks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable {

    private final int PORT;

    private ServerSocket serverSocket;
    private Map<InetAddress, Connection> clients = new HashMap<>();
    private boolean stopped = false;

    Server(int port) {
        this.PORT = port;
    }

    public void run() {
        // Open new socket
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open on port " + PORT);
        }
        // Main connections loop
        while (!stopped) {
            try {
                new Connection(this, serverSocket.accept());
                System.out.println("found new client, adding");
            } catch (IOException e) {
                if (stopped) {
                    Logger.log("Server stopped on exception");
                    for (Connection connection : new ArrayList<>(clients.values())) {
                        connection.close();
                    }
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
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    Connection getClient(InetAddress inetAddress) { //throw client not registered?
        return clients.get(inetAddress);
    }

    Collection<Connection> getClients() {
        return clients.values();
    }

    Map<InetAddress, Connection> getClientsMap() {
        return clients;
    }

    void onTextPacket(Connection connection, String payload) {
        Logger.log(connection.getSocket().getInetAddress() + ": " + payload);
        sendTextExcept(Collections.singletonList(connection), payload);
    }

    void sendText(List<Connection> recipients, String payload) {
        if (recipients == null) {
            for (Connection connection : clients.values()) {
                connection.sendText(payload);
            }
        } else {
            for (Connection connection : recipients) {
                connection.sendText(payload);
            }
        }
    }

    void sendTextExcept(List<Connection> recipients, String payload) {
        if (recipients == null) {
            for (Connection connection : clients.values()) {
                connection.sendText(payload);
            }
        } else {
            for (Connection connection : clients.values()) {
                if (!recipients.contains(connection)) {
                    connection.sendText(payload);
                }
            }
        }
    }
}
