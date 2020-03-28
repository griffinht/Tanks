package net.stzups.tanks.game;

import net.stzups.tanks.server.Connection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Network {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Game game;

    Network(Game game) {
        this.game = game;
    }

    void tick() {
        executorService.submit(() -> {
            System.out.println("doing network stuff");
            for (Player player : game.players) {

            }
        });
    }

    void stop() {
        executorService.shutdown();
    }

    void onPacket(Connection connection, String payload) {
        System.out.println("network got packet from " + payload);
        Player player = new Player(connection, "your mom's", 0, 0);
        game.players.add(player);
        game.world.addObject(player);
        System.out.println("added new player "+player.getName());
    }
}
