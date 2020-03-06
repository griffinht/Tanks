package net.stzups.tanks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tanks {
    private static final int PORT = 2000;

    public static void main(String[] args) {
        long start = System.nanoTime();
        Logger.log("Starting server on port " + PORT);

        Server server = new Server(PORT);
        new Thread(server).start();

        Logger.log("Started server on port " + PORT + " in " + (float)(System.nanoTime()/start)/1000000+"ms");
        while(true) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String input = bufferedReader.readLine();
                switch (input.toLowerCase()) {
                    case "stop":
                        Logger.log("Stopping server...");
                        server.stop();
                        Logger.log("Server stopped");
                        return;
                    default:
                        Logger.log("Unknown command \""+input+"\"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}