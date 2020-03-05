package net.stzups.tanks;

import java.net.Socket;
import java.util.UUID;

class Client extends WorkerRunnable {
    private UUID uuid;

    Client(Socket socket, UUID uuid) {
        super(socket);
        this.uuid = uuid;
        new Thread(this).start();
    }

    UUID getUUID() {
        return uuid;
    }
}
