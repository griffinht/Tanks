package net.stzups.tanks;

import net.stzups.tanks.game.Game;
import net.stzups.tanks.server.Server;

import java.util.logging.Logger;

public class Tanks {

    private static final int PORT = 80;
    private static final Logger logger = LoggerHandler.setLogger();

    public static final Server server = new Server(PORT);
    static final Game game = new Game();


    public static void main(String[] args) {
        long start = System.nanoTime();
        logger.info("Starting server on port " + PORT);

        FileManager.load();
        new Thread(ConsoleManager::manage).start();
        new Thread(server).start();
        new Thread(game).start();

        logger.info("Started server on port " + PORT + " in " + (System.nanoTime()-start)/1000000+"ms");
    }

    static void stop() {
        logger.info("Stopping server...");
        Tanks.server.stop();
        Tanks.game.stop();
        logger.info("Server stopped");
    }
}