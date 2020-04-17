package net.stzups.tanks.game;

import net.stzups.tanks.server.Connection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Game implements Runnable {

    private static int GAME_TICK_RATE = 60;
    private static int GAME_TICK_INTERVAL = 1000000000 / GAME_TICK_RATE;
    static final int NETWORK_TICK_RATE = 20;
    private static int NETWORK_TICK_INTERVAL = 1000000000 / NETWORK_TICK_RATE;


    private Network network = new Network(this);
    private long lastNetworkTick = 0;

    private boolean running;
    private int tick = 0;
    private float tps = 0;
    private long lastTick = 0;

    Map<Connection, Player> connectionPlayerMap = new HashMap<>();
    World world = new World();

    public Game() {

    }

    public void run() {
        running = true;

        while (running) {
            long now = System.nanoTime();

            if (now - lastTick > GAME_TICK_INTERVAL) {
                long elapsedTime = now - lastTick;
                tps = 1000000000F / elapsedTime;
                lastTick = now;

                //System.out.print(tick + " ticks, " + (Math.round(tps * 100) / 100.0) + "tps, " + elapsedTime / 1000000.0 + "ms per tick\r");// todo fix tickrate reporting
                //movement
                world.tick(tick);

                tick++;
            }

            if (now - lastNetworkTick > NETWORK_TICK_INTERVAL) {
                network.tick(tick);
                lastNetworkTick = now;
            }

            Thread.yield(); //todo idk if this needs to be here, also try to help alleviate cpu hog?
        }
    }

    public void stop() {
        running = false;
        network.stop();
    }

    public Collection<Player> getPlayers() {
        return connectionPlayerMap.values();
    }

    public Map<Connection, Player> getConnectionPlayerMap() {
        return connectionPlayerMap;
    }

    double getTps() {
        return tps;
    }
}
