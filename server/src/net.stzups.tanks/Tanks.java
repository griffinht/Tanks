package net.stzups.tanks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tanks {
    private static final int PORT = 2000;

    public static void main(String[] args) {
        long start = System.nanoTime();
        System.out.println("Starting server on port " + PORT);

        Server server = new Server(PORT);
        new Thread(server).start();

        System.out.println("Started server on port " + PORT + " in " + (float)(System.nanoTime()/start)/1000000+"ms");
        while(true) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String input = bufferedReader.readLine();
                switch (input.toLowerCase()) {
                    case "stop":
                        System.out.println("Stopping server...");
                        server.stop();
                        System.out.println("Server stopped");
                        return;
                    default:
                        System.out.println("Unknown command \""+input+"\"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}