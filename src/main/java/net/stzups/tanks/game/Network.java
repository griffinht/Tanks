package net.stzups.tanks.game;

import net.stzups.tanks.Tanks;
import net.stzups.tanks.server.Connection;
import net.stzups.tanks.server.PacketListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Map<Entity, byte[]> entitiesPartial = new HashMap<>();
            Map<Entity, byte[]> entitiesFull = new HashMap<>();
            //System.out.print("looping through " + game.connectionPlayerMap.size() + ": ");
            try {
                for (Map.Entry<Connection, Player> entry : game.connectionPlayerMap.entrySet()) {//todo test with ByteArrayOutputStream
                    entry.getKey().sendPing();

                    Player player = entry.getValue();

                    ByteBuffer serverByteBuffer = ByteBuffer.allocate(2 + 4 + 4);//todo performance //todo static properties
                    serverByteBuffer.putShort((short) 1);
                    serverByteBuffer.putInt(tick);
                    serverByteBuffer.putFloat(game.getLastTickTime());//todo isnt this just dt?

                    ByteBuffer pingByteBuffer = ByteBuffer.allocate(2 + 2);
                    pingByteBuffer.putShort((short) 3);
                    pingByteBuffer.putShort((short) entry.getKey().getPing());

                    List<Entity> playerKnownEntities = new ArrayList<>();
                    Map<Class<?>, List<byte[]>> playerSerializedEntities = new HashMap<>();
                    for (int x = (int) (player.x - player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x <= (int) (player.x + player.getViewportWidth() / 2.0f) / World.GRID_SIZE; x++) {//todo go from ints to shorts?
                        for (int y = (int) (player.y - player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y <= (int) (player.y + player.getViewportHeight() / 2.0f) / World.GRID_SIZE; y++) {//todo more strict range todo list by grid or just all entities??
                            for (Entity entity : game.world.grid.get(x, y)) {
                                playerKnownEntities.add(entity);
                                if (player.knownEntities.contains(entity)) {
                                    byte[] serialized;
                                    if (entitiesPartial.containsKey(entity)) {
                                        //partial
                                        serialized = entitiesPartial.get(entity);
                                        if (serialized.length == 0) {
                                            continue;
                                        }
                                    } else {
                                        serialized = entity.serialize(false);
                                        entitiesPartial.put(entity, serialized);
                                    }
                                    if (playerSerializedEntities.containsKey(entity.getClass())) {
                                        playerSerializedEntities.get(entity.getClass()).add(serialized);
                                    } else {
                                        List<byte[]> serializedList = new ArrayList<>();
                                        serializedList.add(serialized);
                                        playerSerializedEntities.put(entity.getClass(), serializedList);
                                    }
                                } else {
                                    //full
                                    byte[] serialized;
                                    if (entitiesFull.containsKey(entity)) {
                                        serialized = entitiesFull.get(entity);
                                    } else {
                                        serialized = entity.serialize(true);
                                        entitiesFull.put(entity, serialized);
                                    }
                                    if (playerSerializedEntities.containsKey(entity.getClass())) {
                                        playerSerializedEntities.get(entity.getClass()).add(serialized);
                                    } else {
                                        List<byte[]> serializedList = new ArrayList<>();
                                        serializedList.add(serialized);
                                        playerSerializedEntities.put(entity.getClass(), serializedList);
                                    }
                                }
                            }
                            /*
                            try (ByteArrayOutputStream gridOutputStream = new ByteArrayOutputStream()) {
                                Map<Class<?>, List<Entity>> entityListMap = new HashMap<>();
                                for (Entity entity : game.world.grid.get(x, y)) {
                                    if (entityListMap.containsKey(entity.getClass())) {
                                        entityListMap.get(entity.getClass()).add(entity);
                                    } else {
                                        List<Entity> list = new ArrayList<>();
                                        list.add(entity);
                                        entityListMap.put(entity.getClass(), list);
                                    }
                                }

                                for (Map.Entry<Class<?>, List<Entity>> entityListEntry : entityListMap.entrySet()) {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                                    byteBuffer.putShort((short) entityListEntry.getKey().getField("ID").getInt(null));
                                    byteBuffer.putShort((short) entityListEntry.getValue().size());//todo make sure no overflow
                                    gridOutputStream.write(byteBuffer.array());
                                    for (Entity entity : entityListEntry.getValue()) {
                                        gridOutputStream.write(entity.serialize(fullUpdate));
                                    }
                                }

                                byte[] entities = gridOutputStream.toByteArray();

                                ByteBuffer gridByteBuffer = ByteBuffer.allocate(entities.length);
                                if (entities.length > 0) {
                                    gridByteBuffer.put(entities);
                                }
                                byte[] g = gridByteBuffer.array();
                                if (fullUpdate) {
                                    gridFull.put(key, g);
                                } else {
                                    grid.put(key, g);
                                }
                                if (g.length > 0) {
                                    playerOutputStream.write(g);
                                }
                            }
                            */
                        }
                    }
                    byte[] playerEntities;
                    try (ByteArrayOutputStream playerOutputStream = new ByteArrayOutputStream()) {
                        if (playerSerializedEntities.size() == 0) {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                            byteBuffer.putShort((short) 0);
                            playerOutputStream.write(byteBuffer.array());
                        } else {
                            for (Map.Entry<Class<?>, List<byte[]>> classListEntry : playerSerializedEntities.entrySet()) {
                                ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                                byteBuffer.putShort((short) classListEntry.getKey().getField("ID").getInt(null));
                                byteBuffer.putShort((short) classListEntry.getValue().size());//todo make sure no overflow
                                playerOutputStream.write(byteBuffer.array());
                                for (byte[] bytes : classListEntry.getValue()) {
                                    playerOutputStream.write(bytes);
                                }
                            }
                            ByteBuffer byteBuffer1 = ByteBuffer.allocate(2);
                            byteBuffer1.putShort((short) 0);
                            playerOutputStream.write(byteBuffer1.array());
                        }
                        playerEntities = playerOutputStream.toByteArray();
                    }
                    List<Entity> playerRemoveEntitiesList = new ArrayList<>();
                    for (Entity entity : player.knownEntities) {
                        if (!playerKnownEntities.contains(entity)) {
                            playerRemoveEntitiesList.add(entity);
                        }
                    }
                    player.knownEntities = playerKnownEntities;
                    ByteBuffer playerRemoveEntitiesByteBuffer = ByteBuffer.allocate(2 + 2 * playerRemoveEntitiesList.size());//todo speed efficieny bytebuffer vs bytearrayoutputstream???
                    playerRemoveEntitiesByteBuffer.putShort((short) playerRemoveEntitiesList.size());
                    for (Entity entity : playerRemoveEntitiesList) {
                        playerRemoveEntitiesByteBuffer.putShort((short) entity.id);
                    }

                    ByteBuffer playerGridByteBuffer = ByteBuffer.allocate(2 + playerEntities.length + playerRemoveEntitiesByteBuffer.position());//todo this seems really unnecessary
                    playerGridByteBuffer.putShort((short) 4);//todo rename without grid b/c its entities
                    playerGridByteBuffer.put(playerEntities);
                    playerGridByteBuffer.put(playerRemoveEntitiesByteBuffer.array());

                    ByteBuffer playByteBuffer = ByteBuffer.allocate(2 + serverByteBuffer.position() + pingByteBuffer.position() + playerGridByteBuffer.position());
                    playByteBuffer.putShort((short) 0);//start play
                    playByteBuffer.put(serverByteBuffer.array());
                    playByteBuffer.put(pingByteBuffer.array());
                    playByteBuffer.put(playerGridByteBuffer.array());

                    ByteBuffer payloadByteBuffer = ByteBuffer.allocate(playByteBuffer.position());
                    payloadByteBuffer.put(playByteBuffer.array());

                    entry.getKey().sendBinary(payloadByteBuffer.array());
                }
            } catch(Exception e) {
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
                    Player player = new Player(game.world.generateRandomId(), 0, 0, 0, 0, 0, 5, 2, name, viewportWidth, viewportHeight, new Player.Turret(0, 0.5f, 3f));
                    game.connectionPlayerMap.put(connection, player);
                    game.world.addEntity(player);
                    logger.info("New player " + player.getName());

                    JSONArray newP = new JSONArray();
                    newP.put(player.id);
                    JSONObject payloadOut = new JSONObject();
                    payloadOut.put("newPlayer", newP);
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
                        bullet.id = game.world.generateRandomId();
                        bullet.owner = player.id;
                        game.world.addEntity(bullet);
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
