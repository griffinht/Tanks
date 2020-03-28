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
    }

    void tick() {
        executorService.submit(() -> {
            System.out.println("doing network stuff");
            for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {
                entry.getKey().sendText("hello, how do you do?");
            }
        });
    }

    void stop() {
        executorService.shutdown();
    }

    public void newPlayer(Connection connection, String payload) {
        System.out.println("network got packet from " + payload);
        Player player = new Player(connection, "your mom's", 0, 0);
        game.connectionPlayerMap.put(connection, player);
        game.world.addObject(player);
        System.out.println("added new player "+player.getName());
    }

    public void onTextPacket(Connection connection, String payload) {
        if (!game.connectionPlayerMap.containsKey(connection)) {
            Player player = new Player(connection, payload, 0, 0);
            logger.info("New player " + player.getName());
        } else {
            Player player = game.connectionPlayerMap.get(connection);
            logger.info(player.getName()+": "+payload);
        }
    }
}
