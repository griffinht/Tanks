package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
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
            Map<Map.Entry<Integer, Integer>, byte[]> grid = new HashMap<>();
            //System.out.print("looping through " + game.connectionPlayerMap.size() + ": ");
            try {
                for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {//todo test with ByteArrayOutputStream
                    Player player = entry.getValue();

                    ByteBuffer viewportByteBuffer;
                    if (player.updateViewport()) {
                        viewportByteBuffer = ByteBuffer.allocate(2 + 4 + 4);
                        viewportByteBuffer.putShort((short) 1);
                        viewportByteBuffer.putInt(player.getViewportWidth());
                        viewportByteBuffer.putInt(player.getViewportHeight());
                    } else {
                        viewportByteBuffer = ByteBuffer.allocate(0);
                    }

                    ByteBuffer serverByteBuffer = ByteBuffer.allocate(2 + 4 + 4);//todo performance //todo static properties
                    serverByteBuffer.putShort((short) 0);
                    serverByteBuffer.putInt(tick);
                    serverByteBuffer.putFloat(game.getLastTickTime());//todo isnt this just dt?

                    ByteBuffer idByteBuffer = ByteBuffer.allocate(2 + 4);
                    idByteBuffer.putShort((short) 1);
                    int id = (int) (Math.random() * 2147483647);//todo this is probably bad
                    idByteBuffer.putInt(id);

                    ByteBuffer pingByteBuffer = ByteBuffer.allocate(2 + 2);
                    pingByteBuffer.putShort((short) 2);
                    pingByteBuffer.putShort((short) player.getPing());

                    byte[] playerGrid;
                    try (ByteArrayOutputStream playerOutputStream = new ByteArrayOutputStream()) {
                        for (int x = (int) (player.x - player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x <= (int) (player.x + player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x++) {//todo go from ints to shorts?
                            for (int y = (int) (player.y - player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y <= (int) (player.y + player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y++) {
                                Map.Entry<Integer, Integer> key = new AbstractMap.SimpleEntry<>(x, y);
                                if (grid.containsKey(key)) {//todo performance
                                    byte[] g = grid.get(key);
                                    if (g.length > 0) {
                                        playerOutputStream.write(g);
                                    }
                                } else {
                                    try (ByteArrayOutputStream gridOutputStream = new ByteArrayOutputStream()) {
                                        for (Entity entity : game.world.grid.get(x, y)) {
                                            gridOutputStream.write(entity.serialize());
                                        }
                                        byte[] entities = gridOutputStream.toByteArray();

                                        ByteBuffer gridByteBuffer = ByteBuffer.allocate(2 + 2 + entities.length);
                                        gridByteBuffer.putShort((short) x);
                                        gridByteBuffer.putShort((short) y);
                                        if (entities.length > 0) {
                                            gridByteBuffer.put(entities);
                                        }
                                        byte[] g = gridByteBuffer.array();
                                        grid.put(key, g);
                                        if (g.length > 0) {
                                            playerOutputStream.write(g);
                                        }
                                    }
                                }
                            }
                        }
                        playerGrid = playerOutputStream.toByteArray();
                    }

                    ByteBuffer playerGridByteBuffer = ByteBuffer.allocate(2 + playerGrid.length);//todo this seems really unnecessary
                    playerGridByteBuffer.putShort((short) 3);
                    playerGridByteBuffer.put(playerGrid);
                    ByteBuffer playByteBuffer = ByteBuffer.allocate(2 + serverByteBuffer.position() + idByteBuffer.position() + pingByteBuffer.position() + playerGridByteBuffer.position());
                    playByteBuffer.putShort((short) 0);
                    playByteBuffer.put(serverByteBuffer.array());
                    playByteBuffer.put(idByteBuffer.array());
                    playByteBuffer.put(pingByteBuffer.array());
                    playByteBuffer.put(playerGridByteBuffer.array());

                    ByteBuffer payloadByteBuffer = ByteBuffer.allocate(viewportByteBuffer.position() + playByteBuffer.array().length);//todo make this more dynamic add byte buffers to list???
                    payloadByteBuffer.put(viewportByteBuffer.array());
                    payloadByteBuffer.put(playByteBuffer.array());
                    entry.getKey().sendBinary(payloadByteBuffer.array());
                    player.getPingQueue().add(new AbstractMap.SimpleEntry<>(id, System.currentTimeMillis()));//todo java 9 Map.entry(k,v)
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
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
                if (payload.has("newPlayer")) {
                    JSONArray newClient = payload.getJSONArray("newPlayer");
                    String name = newClient.getString(0);
                    int viewportWidth = newClient.getInt(1);
                    int viewportHeight = newClient.getInt(2);
                    Player player = new Player(game.world.generateRandomId(), 0, 0, 0, 0, 0, 5, 2, name, viewportWidth, viewportHeight, new Player.Turret(0, 0.5f, 3f), new HashMap<>());
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
                        Map.Entry<Integer, Long> poll;
                        boolean match = false;
                        while (!player.getPingQueue().isEmpty()) {
                            poll = player.getPingQueue().poll();
                            if (poll.getKey().equals(uuid)) {
                                match = true;
                                long ping = System.currentTimeMillis() - poll.getValue();
                                if (ping < 0) {
                                    logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", replied to pong packet in negative time");
                                } else {
                                    if (ping > Connection.MAXIMUM_PING) {
                                        logger.warning("Kicking " + connection.getSocket().getInetAddress().getHostAddress() + " after sending " + rawPayload + ", ping too high");//todo will this ever happen if the thing below is already checking?
                                        connection.close(true);
                                        return;
                                    } else {
                                        player.setPing((short) ping);
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
                if (payload.has("bullets")) {
                    JSONArray bullets = payload.getJSONArray("bullets");
                    for (Object b : bullets) {
                        Bullet bullet = Bullet.deserialize((JSONArray) b);
                        game.world.addEntity(bullet);
                        player.addBullet(bullet);
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

    public void onBinaryPacket(Connection connection, byte[] payload) {
        //todo
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
