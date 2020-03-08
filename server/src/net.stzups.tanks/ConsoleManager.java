package net.stzups.tanks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

class ConsoleManager {
    static void manage() {
        while (true) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String[] input = bufferedReader.readLine().split("\\s");
                switch (input[0].toLowerCase()) {
                    case "stop":
                        Logger.log("Stopping server...");
                        Tanks.server.stop();
                        Logger.log("Server stopped");
                        return;
                    case "list":
                        Collection<Client> clients = Tanks.server.getClients();
                        Logger.log("Listing " + clients.size() + " clients");
                        for (Client client : clients) {
                            Logger.log(client.getUUID() + " : " + client.getSocket().getInetAddress().getHostAddress());
                        }
                        break;
                    case "kick":
                        if (input.length > 1) {
                            for (Client client : Tanks.server.getClients()) {
                                if (client.getSocket().getInetAddress().getHostAddress().equals(input[1]) || client.getUUID().toString().equals(input[1])) {
                                    client.close();
                                    Logger.log("Kicked " + client.getUUID());
                                    break;
                                }
                            }

                            Logger.log("Could not find client matching " + input[1]);
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
