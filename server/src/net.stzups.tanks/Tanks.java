package net.stzups.tanks;

import net.stzups.tanks.server.Server;

import java.util.logging.Logger;

public class Tanks {
    private static final int PORT = 80;
    static Server server = new Server(PORT);
    private static Logger logger;

    public static void main(String[] args) {
        long start = System.nanoTime();
        logger = LoggerHandler.setLogger();
        logger.info("Starting server on port " + PORT);

        FileManager.load();
        new Thread(server).start();
        new Thread(ConsoleManager::manage).start();

        logger.info("Started server on port " + PORT + " in " + (System.nanoTime()-start)/1000000+"ms");
    }
}