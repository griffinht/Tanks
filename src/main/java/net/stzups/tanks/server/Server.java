package net.stzups.tanks.server;

import net.stzups.tanks.FileManager;
import net.stzups.tanks.Tanks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Server implements Runnable {

    private final int PORT;
    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private final FileManager fileManager;

    private final List<Connection> connections = new ArrayList<>();
    private final List<PacketListener> packetListeners = new ArrayList<>();

    private ServerSocket serverSocket;
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
            logger.warning("Could not open on port " + PORT);
            System.exit(0);
        }
        // Main connections loop
        while (!stopped) {
            try {
                Socket socket = serverSocket.accept();
                if(!stopped)
                {
                    new Connection(this, socket, fileManager);
                }
                else
                {
                    socket.close();
                }
            } catch (IOException e) {
                if (stopped) {
                    logger.info("Closing " + connections.size() + " connections...");
                    Iterator<Connection> iterator = connections.iterator();
                    while (iterator.hasNext()) {
                        iterator.next().close(true, false);
                        iterator.remove();
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

    public List<Connection> getConnections() {
        return connections;
    }

    boolean containsInetAddress(InetAddress inetAddress) {
        for (Connection connection : connections) {
            if (connection.getSocket().getInetAddress().equals(inetAddress)) {
                return true;
            }
        }

        return false;
    }

    void addConnection(Connection connection) {
        connections.add(connection);
        for (PacketListener packetListener : packetListeners) {
            packetListener.addConnection(connection);
        }
    }

    void removeConnection(Connection connection) {
        connections.remove(connection);
        for (PacketListener packetListener : packetListeners) {
            packetListener.removeConnection(connection);
        }
    }

    void onTextPacket(Connection connection, String payload) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.onTextPacket(connection, payload);
        }
    }

    void onBinaryPacket(Connection connection, byte[] payload) {
        for (PacketListener packetListener : packetListeners) {
            packetListener.onBinaryPacket(connection, payload);
        }
    }

    public void addPacketListener(PacketListener packetListener) {
        packetListeners.add(packetListener);
    }

    //todo remove
    /*
    void sendText(List<Connection> recipients, String payload) {
        if (recipients == null) {
            for (Connection connection : connections) {
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
            for (Connection connection : connections) {
                connection.sendText(payload);
            }
        } else {
            for (Connection connection : connections) {
                if (!recipients.contains(connection)) {
                    connection.sendText(payload);
                }
            }
        }
    }
     */
}
