package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
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

    void update(int tick, float dt) {
        executorService.submit(() -> {
            JSONObject grid = new JSONObject();
            //System.out.print("looping through " + game.connectionPlayerMap.size() + ": ");
            try {
                for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {
                    Player player = entry.getValue();
                    JSONObject payload = new JSONObject();
                    JSONObject play = new JSONObject();

                    JSONArray server = new JSONArray();
                    server.put(tick);
                    server.put(game.getLastTickTime());
                    play.put("server", server);

                    UUID uuid = UUID.randomUUID();
                    play.put("uuid", uuid);
                    play.put("ping", player.getPing());
                    if (player.updateViewport()) {
                        JSONArray viewport = new JSONArray();
                        viewport.put(player.getViewportWidth());
                        viewport.put(player.getViewportHeight());
                        payload.put("viewport", viewport);
                    }
                    JSONObject playerGrid = new JSONObject();
                    for (int x = (int) (player.x - player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x <= (int) (player.x + player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x++) {
                        for (int y = (int) (player.y - player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y <= (int) (player.y + player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y++) {
                            String key = x + "," + y;
                            if (grid.has(key)) {
                                JSONObject g = grid.getJSONObject(key);
                                if (g.length() > 0) {
                                    playerGrid.put(key, grid.get(key));
                                }
                            } else {
                                JSONObject g = new JSONObject();
                                JSONArray entities = new JSONArray();
                                for (Entity entity : game.world.grid.get(x, y)) {
                                    entities.put(new JSONArray(entity.serialize()));
                                }
                                if (entities.length() > 0) {
                                    g.put("entities", entities);
                                }
                                grid.put(key, g);
                                if (g.length() > 0) {
                                    playerGrid.put(key, g);
                                }
                            }
                        }
                    }
                    play.put("grid", playerGrid);
                    payload.put("play", play);
                    //System.out.print(entry.getKey().getUUID() + ", ");

                    entry.getKey().sendText(payload.toString());
                    player.getPingQueue().add(new AbstractMap.SimpleEntry<>(uuid, System.currentTimeMillis()));//todo java 9 Map.entry(k,v)
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
                    Player player = new Player(UUID.randomUUID(), 0, 0, 0, 0, 0, 5, 2, name, viewportWidth, viewportHeight, new Player.Turret(0, 0.5f, 3f), new ArrayList<>());
                    game.connectionPlayerMap.put(connection, player);
                    game.world.addEntity(player);
                    logger.info("New player " + player.getName());

                    JSONArray newP = new JSONArray();
                    newP.put(player.id);
                    player.updateViewport();
                    JSONArray viewport = new JSONArray();
                    viewport.put(player.getViewportWidth());
                    viewport.put(player.getViewportHeight());
                    JSONObject payloadOut = new JSONObject();
                    payloadOut.put("newPlayer", newP);
                    payloadOut.put("viewport", viewport);
                    connection.sendText(payloadOut.toString());
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
                if (!player.getPingQueue().isEmpty()) {//todo this might be exploitable
                    if (payload.has("play")) {
                        UUID uuid = UUID.fromString(payload.getString("play"));
                        Map.Entry<UUID, Long> poll;
                        boolean match = false;
                        while (!player.getPingQueue().isEmpty()) {
                            poll = player.getPingQueue().poll();
                            if (poll.getKey().equals(uuid)) {
                                match = true;
                                long ping = System.currentTimeMillis() - poll.getValue();
                                if (ping < 0) {
                                    logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", replied to pong packet in negative time");
                                } else {
                                    player.setPing((int) ping);
                                    if ((int) ping > Connection.MAXIMUM_PING) {
                                        logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", ping too high");//todo will this ever happen if the thing below is already checking?
                                        connection.close(true);
                                        return;
                                    }
                                }
                                break;
                            }
                        }
                        if(!match) {
                            logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", specified the wrong pong packet id");
                            connection.close(true);//todo change to kick with reason method also reason for kick
                            return;
                        }
                    } else if (System.currentTimeMillis() - player.getPingQueue().peek().getValue() > Connection.MAXIMUM_PING) {
                        logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", took too long to respond to ping");
                        connection.close(true);
                        return;
                    }
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
            game.world.removeEntity(game.connectionPlayerMap.get(connection));
            game.connectionPlayerMap.remove(connection);
        } else {
            System.out.println("couldnt remove game connection for "+connection.getSocket().getInetAddress().getHostAddress());
        }
    }

    public void addConnection(Connection connection) {
        connection.sendText("{\"information\":[" + Game.NETWORK_TICK_RATE + "]}");
    }
}
