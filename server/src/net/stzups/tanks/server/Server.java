package net.stzups.tanks.server;

import net.stzups.tanks.FileManager;
import net.stzups.tanks.Tanks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Server implements Runnable {

    private final int PORT;
    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private FileManager fileManager;

    private ServerSocket serverSocket;
    private Map<InetAddress, Connection> clients = new HashMap<>();
    private boolean stopped = false;

    public Server(int port) {
        this.PORT = port;
        fileManager = new FileManager();
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
                new Connection(this, serverSocket.accept(), fileManager);
            } catch (IOException e) {
                if (stopped) {
                    logger.info("Closing " + clients.size() + " connections...");
                    for (Connection connection : new ArrayList<>(clients.values())) {
                        connection.close(true);
                    }
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        logger.info("Server stopped");
    }

    public synchronized void stop() {
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

    public Collection<Connection> getClients() {
        return clients.values();
    }

    Map<InetAddress, Connection> getClientsMap() {
        return clients;
    }

    void onTextPacket(Connection connection, String payload) {
        logger.info(connection.getSocket().getInetAddress().getHostAddress() + ": " + payload);
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
