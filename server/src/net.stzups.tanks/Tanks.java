package net.stzups.tanks;

public class Tanks {
    private static final int PORT = 80;
    static Server server = new Server(PORT);

    public static void main(String[] args) {
        long start = System.nanoTime();
        Logger.log("Starting server on port " + PORT);

        FileManager.load();
        new Thread(server).start();
        new Thread(ConsoleManager::manage).start();

        Logger.log("Started server on port " + PORT + " in " + (System.nanoTime()-start)/1000000+"ms");
    }
}