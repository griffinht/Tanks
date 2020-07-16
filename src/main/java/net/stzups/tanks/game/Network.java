package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class Network implements PacketListener {

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Game game;

    Network(Game game) {
        this.game = game;
        Tanks.server.addPacketListener(this);
    }

    void tick(int tick) {
        executorService.submit(() -> {
            JSONObject[][] sectors = new JSONObject[World.WORLD_SECTORS][World.WORLD_SECTORS];
            float tps = (Math.round(game.getTps() * 100) / 100F);

            //System.out.print("looping through " + game.connectionPlayerMap.size() + ": ");
            try {
                for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {
                    Player player = entry.getValue();
                    JSONObject payload = new JSONObject();
                    payload.put("tick", tick);
                    for (int x = Math.max(0, (int) ((player.x - player.viewportWidth) / 2.0 / World.SECTOR_SIZE)); x < Math.ceil((player.x + player.viewportWidth) / 2.0 / World.SECTOR_SIZE); x++) {
                        for (int y = Math.max(0, (int) ((player.y - player.viewportHeight) / 2.0 / World.SECTOR_SIZE)); y < Math.ceil((player.y + player.viewportHeight) / 2.0 / World.SECTOR_SIZE); y++) {
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
                    //System.out.print(entry.getKey().getUUID() + ", ");
                    entry.getKey().sendText("{\"play\":" + payload.toString() + ",\"ping\":" + entry.getKey().getPing() + ",\"tps\":" + tps + "}");
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            //System.out.print("\r");
        });
    }

    void stop() {
        executorService.shutdown();
    }

    public void onTextPacket(Connection connection, String rawPayload) {
        if (!game.connectionPlayerMap.containsKey(connection)) {
            try {
                JSONObject payload = new JSONObject(rawPayload);
                if (payload.has("newPlayer")) {
                    JSONArray newClient = payload.getJSONArray("newPlayer");
                    String name = newClient.getString(0);
                    int viewportWidth = newClient.getInt(1);
                    int viewportHeight = newClient.getInt(2);
                    Player player = new Player(UUID.randomUUID(), 0, 0, 0, 0, 0, 40, 20, name, viewportWidth, viewportHeight, new Player.Turret(0, 4, 30), new ArrayList<>());
                    game.connectionPlayerMap.put(connection, player);
                    game.world.addObject(player);
                    logger.info("New player " + player.getName());

                    connection.sendText("{\"newPlayer\":[\"" + player.id + "\"]}");
                }
                if (payload.has("time")) {
                    connection.setPing((int) (System.currentTimeMillis() - payload.getLong("time")));
                } else {
                    logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", should have included time, instead got " + payload.keySet());
                    connection.close(true);
                }
                //remember to add a return back here
            } catch (JSONException e) {
                logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", parsing packet caused " + e.getMessage());
                connection.close(true);
            }
        } else {
            Player player = game.connectionPlayerMap.get(connection);
            //logger.info(player.getName() + ": " + rawPayload);
            try {
                JSONObject payload = new JSONObject(rawPayload);
                if (payload.has("time")) {
                    connection.setPing((int) (System.currentTimeMillis() - payload.getLong("time")));
                } else {
                    logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", should have included time, instead got " + payload.keySet());
                    connection.close(true);
                    return;
                }
                if (payload.has("player")) {
                    JSONArray jsonPlayer = payload.getJSONArray("player");
                    if (!player.update(jsonPlayer)) {
                        logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", specified incorrect player ID");
                        connection.close(true);
                        return;
                    }
                }
                if (payload.has("viewport")) {
                    JSONArray viewport = payload.getJSONArray("viewport");
                    player.setViewport(viewport.getInt(0), viewport.getInt(1));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", parsing packet caused " + e.getMessage());
                connection.close(true);
            }
        }
    }

    public void removeConnection(Connection connection) {

        if (game.connectionPlayerMap.containsKey(connection)) {
            System.out.println("remove game connection for "+connection.getSocket().getInetAddress().getHostAddress());
            game.world.removeObject(game.connectionPlayerMap.get(connection));
            game.connectionPlayerMap.remove(connection);
        } else {
            System.out.println("couldnt remove game connection for "+connection.getSocket().getInetAddress().getHostAddress());
        }
    }

    public void addConnection(Connection connection) {
        connection.sendText("{\"information\":[" + Game.NETWORK_TICK_RATE + "]}");
    }
}
