package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class Network implements PacketListener {

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Game game;

    Network(Game game) {
        this.game = game;
        Tanks.server.addPacketListener(this);
    }

    void tick() {
        executorService.submit(() -> {
            for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {
                entry.getKey().sendText("hello " + entry.getValue().getName() + ", how do you do?");
            }
        });
    }

    void stop() {
        executorService.shutdown();
    }

    public void onTextPacket(Connection connection, String payload) {
        if (!game.connectionPlayerMap.containsKey(connection)) {
            Player player = new Player(payload, 0, 0);
            game.connectionPlayerMap.put(connection, player);
            game.world.addObject(player);
            logger.info("New player " + player.getName());
        } else {
            Player player = game.connectionPlayerMap.get(connection);
            logger.info(player.getName()+": "+payload);
        }
    }
}
