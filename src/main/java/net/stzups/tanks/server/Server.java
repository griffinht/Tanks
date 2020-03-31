package net.stzups.tanks.server;

import net.stzups.tanks.FileManager;
import net.stzups.tanks.Tanks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Server implements Runnable {

    private final int PORT;
    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private FileManager fileManager;

    private ServerSocket serverSocket;
    private Map<InetAddress, Connection> connections = new HashMap<>();
    private boolean stopped = false;

    private List<PacketListener> packetListeners = new ArrayList<>();

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
            logger.warning("Could not open on port " + PORT);
            System.exit(0);
        }
        // Main connections loop
        while (!stopped) {
            try {
                new Connection(this, serverSocket.accept(), fileManager);
            } catch (IOException e) {
                if (stopped) {
                    logger.info("Closing " + connections.size() + " connections...");
                    for (Connection connection : new ArrayList<>(connections.values())) {
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

    public Collection<Connection> getConnections() {
        return connections.values();
    }

    boolean containsConnection(Connection connection) {
        return connections.containsKey(connection.getSocket().getInetAddress());
    }

    void addConnection(Connection connection) {
        connections.put(connection.getSocket().getInetAddress(), connection);
        for (PacketListener packetListener : packetListeners) {
            packetListener.addConnection(connection);
        }
    }

    void removeConnection(Connection connection) {
        connections.remove(connection.getSocket().getInetAddress());
        for (PacketListener packetListener : packetListeners) {
            packetListener.removeConnection(connection);
        }
    }

    void onTextPacket(Connection connection, String payload) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.onTextPacket(connection, payload);
        }
    }

    public void addPacketListener(PacketListener packetListener) {
        packetListeners.add(packetListener);
    }

    void sendText(List<Connection> recipients, String payload) {
        if (recipients == null) {
            for (Connection connection : connections.values()) {
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
            for (Connection connection : connections.values()) {
                connection.sendText(payload);
            }
        } else {
            for (Connection connection : connections.values()) {
                if (!recipients.contains(connection)) {
                    connection.sendText(payload);
                }
            }
        }
    }
}
