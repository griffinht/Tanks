package net.stzups.tanks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Server implements Runnable {
    private final int PORT;

    private ServerSocket serverSocket;
    private Map<UUID, Client> clients = new HashMap<>();
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
                Socket socket = serverSocket.accept();
                UUID uuid = UUID.randomUUID();
                clients.put(uuid, new Client(this, socket, uuid));
            } catch (IOException e) {
                if (stopped) {
                    Logger.log("Server stopped on exception");
                    for (Client client : clients.values()) {
                        client.close();
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

    Client getClient(UUID uuid) { //throw client not registered?
        return clients.get(uuid);
    }

    Collection<Client> getClients() {
        return clients.values();
    }

    Map<UUID, Client> getClientsMap() {
        return clients;
    }

    void onTextPacket(Client client, String payload) {
        Logger.log(client.getSocket().getInetAddress() + ": " + payload);
        sendTextExcept(Collections.singletonList(client), payload);
    }

    void sendText(List<Client> recipients, String payload) {
        if (recipients == null) {
            for (Client client : clients.values()) {
                client.sendText(payload);
            }
        } else {
            for (Client client : recipients) {
                client.sendText(payload);
            }
        }
    }

    void sendTextExcept(List<Client> recipients, String payload) {
        if (recipients == null) {
            for (Client client : clients.values()) {
                client.sendText(payload);
            }
        } else {
            for (Client client : clients.values()) {
                if (!recipients.contains(client)) {
                    client.sendText(payload);
                }
            }
        }
    }
}
