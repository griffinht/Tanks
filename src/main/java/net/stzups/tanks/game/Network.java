package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class Network implements PacketListener {

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Game game;

    Network(Game game) {
        this.game = game;
        Tanks.server.addPacketListener(this);
    }

    void tick() {
        executorService.submit(() -> {
            JSONObject[][] sectors = new JSONObject[World.WORLD_SECTORS][World.WORLD_SECTORS];

            for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {
                Player player = entry.getValue();
                JSONObject payload = new JSONObject();
                for (int x = Math.max(0, (int) (player.x - player.viewportWidth / 2.0 / World.SECTOR_SIZE)); x < Math.ceil(player.x + player.viewportWidth / 2.0 / World.SECTOR_SIZE); x++) {
                    for (int y = Math.max(0, (int) (player.y - player.viewportHeight / 2.0 / World.SECTOR_SIZE)); y < Math.ceil(player.y + player.viewportHeight / 2.0 / World.SECTOR_SIZE); y++) {
                        Sector sector = game.world.sectors[x][y];
                        if (sector != null) {
                            if (sectors[x][y] == null) {
                                JSONObject jsonSector = sector.serialize();
                                sectors[x][y] = jsonSector;
                                payload.put(x + "," + y, jsonSector);
                            } else {
                                payload.put(x + "," + y, sectors[x][y]);
                            }
                        }
                    }
                }
                entry.getKey().sendText(payload.toString());
            }
        });
    }

    void stop() {
        executorService.shutdown();
    }

    public void onTextPacket(Connection connection, String rawPayload) {
        if (!game.connectionPlayerMap.containsKey(connection)) {
            try {
                JSONObject payload = new JSONObject(rawPayload);
                JSONObject newClient = payload.getJSONObject("newClient");
                String name = newClient.getString("name");
                int viewportWidth = newClient.getInt("viewportWidth");
                int viewportHeight = newClient.getInt("viewportHeight");
                Player player = new Player(name, 0, 0, viewportWidth, viewportHeight);
                game.connectionPlayerMap.put(connection, player);
                game.world.addObject(player);
                logger.info("New player " + player.getName());
            } catch (JSONException e) {
                logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", parsing newClient packet caused " + e.getMessage());
                connection.close(true);
            }

        } else {
            Player player = game.connectionPlayerMap.get(connection);
            logger.info(player.getName()+": "+rawPayload);
        }
    }

    public void removeConnection(Connection connection) {
        if (game.connectionPlayerMap.containsKey(connection)) {
            game.world.removeObject(game.connectionPlayerMap.get(connection));
            game.connectionPlayerMap.remove(connection);
        }
    }

    public void addConnection(Connection connection) {

    }
}
