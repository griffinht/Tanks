package net.stzups.tanks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;

public class Tanks {
    private static final int PORT = 2000;

    public static void main(String[] args) {
        long start = System.nanoTime();
        Logger.log("Starting server on port " + PORT);

        URL url = Tanks.class.getResource("/resources/client/index.html");
        try (InputStream inputStream = url.openStream()){
            File file = new File("client/");
            file.mkdir();
            file = new File("client/index.html");
            if (!file.exists()) {
                Files.copy(inputStream, Paths.get("client/index.html"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Server server = new Server(PORT);
        new Thread(server).start();

        Logger.log("Started server on port " + PORT + " in " + (float)(System.nanoTime()/start)/1000000+"ms");
        while(true) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String[] input = bufferedReader.readLine().split("\\s");
                switch (input[0].toLowerCase()) {
                    case "stop":
                        Logger.log("Stopping server...");
                        server.stop();
                        Logger.log("Server stopped");
                        return;
                    case "list":
                        Collection<Client> clients = server.getClients();
                        Logger.log("Listing " + clients.size() + " clients");
                        for (Client client : clients) {
                            Logger.log(client.getUUID() + " : " + client.getSocket().getInetAddress());
                        }
                        break;
                    case "kick":
                        if (input.length > 1) {
                            UUID uuid;
                            boolean kick = false;

                            try {
                                uuid = UUID.fromString(input[1]);
                                if (server.getClientsMap().containsKey(uuid)) {
                                    server.getClient(uuid).close();
                                    Logger.log("Kicked " + uuid);
                                    kick = true;
                                }
                                Logger.log("Could not find client matching " + input[1]);
                            } catch (IllegalArgumentException ignored) {}

                            for (Client client : server.getClients()) {
                                if (client.getSocket().getInetAddress().getHostAddress().equals(input[1])) {
                                    kick = true;
                                    client.close();
                                    Logger.log("Kicked "+ client.getUUID());
                                    break;
                                }
                            }

                            if (!kick) {
                                Logger.log("Could not find client matching " + input[1]);
                            }
                        } else {
                            Logger.log("Needs one argument");
                        }
                        break;
                    default:
                        Logger.log("Unknown command \"" + input[0] + "\"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}