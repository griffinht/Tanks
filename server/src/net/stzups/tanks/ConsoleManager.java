package net.stzups.tanks;

import net.stzups.tanks.server.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.logging.Logger;

class ConsoleManager {

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    static void manage() {
        while (true) {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String[] input = bufferedReader.readLine().split("\\s");
                switch (input[0].toLowerCase()) {
                    case "stop":
                        logger.info("Stopping server...");
                        Tanks.server.stop();
                        logger.info("Server stopped");
                        return;
                    case "list":
                        Collection<Connection> connections = Tanks.server.getClients();
                        logger.info("Listing " + connections.size() + " clients");
                        for (Connection connection : connections) {
                            logger.info(connection.getUUID() + " : " + connection.getSocket().getInetAddress().getHostAddress());
                        }
                        break;
                    case "kick":
                        if (input.length > 1) {
                            boolean found = false;
                            for (Connection connection : Tanks.server.getClients()) {
                                if (connection.getSocket().getInetAddress().getHostAddress().equals(input[1]) || connection.getUUID().toString().equals(input[1])) {
                                    connection.close(true);
                                    logger.info("Kicked " + connection.getUUID());
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                logger.info("Could not find client matching " + input[1]);
                        } else {
                            logger.info("Needs one argument");
                        }
                        break;
                    default:
                        logger.info("Unknown command \"" + input[0] + "\"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
