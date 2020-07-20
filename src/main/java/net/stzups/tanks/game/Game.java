package net.stzups.tanks.game;

import net.stzups.tanks.server.Connection;

import java.util.HashMap;
import java.util.Map;

public class Game implements Runnable {

    private static final int GAME_TICK_RATE = 20;
    private static final int GAME_TICK_INTERVAL = 1000000000 / GAME_TICK_RATE;
    static final int NETWORK_TICK_RATE = 20;
    private static final int NETWORK_TICK_INTERVAL = 1000000000 / NETWORK_TICK_RATE;

    private final Network network = new Network(this);

    final Map<Connection, Player> connectionPlayerMap = new HashMap<>();
    final World world = new World();

    private long lastNetworkTick = 0;
    private boolean running;
    private int tick = 0;
    private float tps = 0;
    private long lastTick = 0;


    public Game() {

    }

    public void run() {
        running = true;

        while (running) {
            long now = System.nanoTime();
            float dt = (now - lastTick) / 1000000000f;//dt is ms since last tick
            if (now - lastTick > GAME_TICK_INTERVAL) {
                tps = 1f / dt;

                lastTick = now;

                //System.out.print(tick + " ticks, " + (Math.round(tps * 100) / 100.0) + "tps, " + dt + "s per tick\r");// todo fix tickrate reporting
                //movement
                world.update(tick, dt);
                dt = 0;

                tick++;
            }

            if (now - lastNetworkTick > NETWORK_TICK_INTERVAL) {
                network.update(tick, dt);
                lastNetworkTick = now;
            }

            Thread.yield(); //todo idk if this needs to be here, also try to help alleviate cpu hog?
        }
    }

    public void stop() {
        running = false;
        network.stop();
    }

    public Map<Connection, Player> getConnectionPlayerMap() {
        return connectionPlayerMap;
    }

    double getTps() {
        return tps;
    }
}
