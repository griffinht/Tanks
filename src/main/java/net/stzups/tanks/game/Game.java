package net.stzups.tanks.game;

import net.stzups.tanks.server.Connection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Game implements Runnable {

    static final int NETWORK_TICK_RATE = 20;
    static final int GAME_TICK_RATE = 60;

    private Network network = new Network(this);
    private long lastNetworkTick = 0;

    private boolean running;
    private int tick = 0;
    private double tickRate = 0;
    private long lastTick = 0;

    Map<Connection, Player> connectionPlayerMap = new HashMap<>();
    World world = new World();

    public Game() {

    }

    public void run() {
        running = true;

        while (running) {
            long time = System.nanoTime();
            long elapsedTime = time - lastTick;
            lastTick = time;
            if ((time - lastNetworkTick) / 1000000 > 1000 / NETWORK_TICK_RATE) {
                network.tick(tick);
                lastNetworkTick = time;
            }

            tickRate = elapsedTime / 1000000.0;

            //System.out.print(tick + " ticks, " + (int) (1000 / tickRate) + "tps, " + elapsedTime / 1000000.0 + "ms per tick\r");// todo fix tickrate reporting
            //movement
            world.tick(tick);


            tick++;
            try {
                Thread.sleep(Math.max(1000 / GAME_TICK_RATE - (int) ((System.nanoTime() - time) / 1000000), 0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    double getTickRate() {
        return tickRate;
    }
}
