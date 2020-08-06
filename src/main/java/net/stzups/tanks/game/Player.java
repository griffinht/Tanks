package net.stzups.tanks.game;

import java.io.ByteArrayOutputStream;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class Player extends Entity {
    static class Turret {
        float rotation;
        float width;
        float height;

        private static byte rotationUpdate = 1;
        private static byte widthUpdate = 1 << 1;
        private static byte heightUpdate = 1 << 2;

        private byte updateFlags = (byte) (rotationUpdate | widthUpdate | heightUpdate);

        Turret (float rotation, float width, float height) {
            this.rotation = rotation;
            this.width = width;
            this.height = height;
        }

        void update(JSONArray jsonArray) {
            rotation = jsonArray.getFloat(0);
            width = jsonArray.getFloat(1);
            height = jsonArray.getFloat(2);
        }

        byte[] serialize() {
            boolean rotationU = (updateFlags & rotationUpdate) == rotationUpdate;
            boolean widthU = (updateFlags & widthUpdate) == widthUpdate;
            boolean heightU = (updateFlags & heightUpdate) == heightUpdate;
            ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 1 + 4 * ((rotationU ? 1 : 0) + (widthU ? 1 : 0) + (heightU ? 1 : 0)));
            byteBuffer.putShort((short) 2);
            byteBuffer.put(updateFlags);
            if (rotationU) byteBuffer.putFloat(rotation);
            if (widthU) byteBuffer.putFloat(width);
            if (heightU) byteBuffer.putFloat(height);
            return byteBuffer.array();
        }
    }

    private static final float MAX_VIEWPORT_WIDTH = 16;
    private static final float MAX_VIEWPORT_HEIGHT = 9;
    private static final int VIEWPORT_SCALE = 4;

    private String name;
    private Player.Turret turret;
    private Map<Character, Bullet> bullets;

    private static byte nameUpdate = 1;
    private static byte turretUpdate = 1 << 1;
    private static byte bulletsUpdate = 1 << 2;

    private byte updateFlags = (byte) (nameUpdate | turretUpdate | bulletsUpdate);

    private int viewportWidth;
    private int viewportHeight;
    private boolean updateViewport = false;
    private Queue<Map.Entry<Integer, Long>> pingQueue = new ArrayDeque<>();
    private short ping;

    Player(char id, float x, float y, float speed, float direction, float rotation, float width, float height, String name, int viewportWidth, int viewportHeight, Player.Turret turret, Map<Character, Bullet> bullets) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.name = name;
        this.turret = turret;
        this.bullets = bullets;
        setViewport(viewportWidth, viewportHeight);
    }

    void setViewport(int viewportWidth, int viewportHeight) {
        if ((float) viewportWidth / (float) viewportHeight >= MAX_VIEWPORT_WIDTH / MAX_VIEWPORT_HEIGHT) {
            this.viewportWidth = (int) (MAX_VIEWPORT_WIDTH * VIEWPORT_SCALE);
            this.viewportHeight = (int) ((float) this.viewportWidth * ((float) viewportHeight / (float) viewportWidth));
        } else {
            this.viewportHeight = (int) (MAX_VIEWPORT_HEIGHT * VIEWPORT_SCALE);
            this.viewportWidth = (int) ((float) this.viewportHeight * ((float) viewportWidth / (float) viewportHeight));
        }
        updateViewport = true;
    }

    boolean updateViewport() {
        if (updateViewport) {
            updateViewport = false;
            return true;
        } else {
            return false;
        }
    }

    void addBullet(Bullet bullet) {
        bullets.put(bullet.id, bullet);
    }

    public String getName() {
        return name;
    }

    int getViewportWidth() {
        return viewportWidth;
    }

    int getViewportHeight() {
        return viewportHeight;
    }

    int getPing() {
        return ping;
    }

    Queue<Map.Entry<Integer, Long>> getPingQueue() {
        return pingQueue;
    }

    void setPing(short ping) {
        this.ping = ping;
    }

    @Override
    void move(float dt) {

    }

    @Override
    boolean update(JSONArray jsonArray) {
        if (!super.update(jsonArray.getJSONArray(1))) {
            return false;
        }
        turret.update(jsonArray.getJSONArray(3));
        return true;
    }

    @Override
    byte[] serialize() {
        byte[] entity = super.serialize();
        boolean nameU = (updateFlags & nameUpdate) == nameUpdate;
        boolean turretU = (updateFlags & turretUpdate) == turretUpdate;
        boolean bulletsU = (updateFlags & bulletsUpdate) == bulletsUpdate;
        byte[] name;
        if (nameU) {
            byte[] n = this.name.getBytes(StandardCharsets.UTF_8);//todo inefficient?
            ByteBuffer nameByteBuffer = ByteBuffer.allocate(2 + n.length);
            nameByteBuffer.putShort((short) n.length);
            nameByteBuffer.put(n);
            name = nameByteBuffer.array();
        } else {
            name = new byte[0];
        }
        byte[] turret;
        if (turretU) {
            turret = this.turret.serialize();
        } else {
            turret = new byte[0];
        }
        byte[] bullets;
        if (bulletsU) {
            try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
                for (Character id : this.bullets.keySet()) {
                    byteOutputStream.write(id);
                }
                bullets = byteOutputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                bullets = new byte[0];
            }
        } else {
            bullets = new byte[0];
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + entity.length + 1 + name.length + turret.length + 2 + bullets.length);
        byteBuffer.putShort((short) 2);
        byteBuffer.put(entity);
        byteBuffer.put(updateFlags);
        if (nameU) byteBuffer.put(name);
        if (turretU) byteBuffer.put(turret);
        if (bulletsU) {byteBuffer.putShort((short) this.bullets.size()); byteBuffer.put(bullets);}
        return byteBuffer.array();
    }
}
