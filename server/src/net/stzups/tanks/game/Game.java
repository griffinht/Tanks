package net.stzups.tanks.game;

import java.util.ArrayList;
import java.util.List;

public class Game implements Runnable {

    private static final int NETWORK_TICK_RATE = 20;

    public Network network = new Network(this);
    private long lastNetworkTick = 0;

    private boolean running;
    private int ticks = 0;
    private long lastTick = 0;

    List<Player> players = new ArrayList<>();
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
                network.tick();
                lastNetworkTick = time;
            }

            System.out.print(ticks + " ticks, " + (int) (1000 / (elapsedTime / 1000000.0)) + "tps, " + elapsedTime / 1000000.0 + "ms per tick\r");
            //movement
            world.tick();


            ticks++;
        }
    }

    public void stop() {
        running = false;
        network.stop();
    }
}
