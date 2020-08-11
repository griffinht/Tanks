package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {

    public static final int ID = 2;

    static class Turret {
        float rotation;
        float width;
        float height;

        private static byte rotationUpdate = 1;
        private static byte widthUpdate = 1 << 1;
        private static byte heightUpdate = 1 << 2;

        private static byte FULL_UPDATE_FLAGS = (byte) (rotationUpdate | widthUpdate | heightUpdate);
        private byte updateFlags = FULL_UPDATE_FLAGS;

        Turret (float rotation, float width, float height) {
            this.rotation = rotation;
            this.width = width;
            this.height = height;
        }

        void update(JSONArray jsonArray) {
            float rotation = jsonArray.getFloat(0);
            if (this.rotation != rotation) {
                this.rotation = rotation;
                updateFlags |= rotationUpdate;
            }
            float width = jsonArray.getFloat(1);
            if (this.width != width) {
                this.width = width;
                updateFlags |= widthUpdate;
            }
            float height = jsonArray.getFloat(2);
            if (this.height != height) {
                this.height = height;
                updateFlags |= heightUpdate;
            }
        }

        byte[] serialize(boolean fullUpdate) {
            byte updateFlags;
            if (fullUpdate) {
                updateFlags = FULL_UPDATE_FLAGS;
            } else {
                updateFlags = this.updateFlags;
            }
            boolean rotationU = (updateFlags & rotationUpdate) == rotationUpdate;
            boolean widthU = (updateFlags & widthUpdate) == widthUpdate;
            boolean heightU = (updateFlags & heightUpdate) == heightUpdate;
            ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 4 * ((rotationU ? 1 : 0) + (widthU ? 1 : 0) + (heightU ? 1 : 0)));
            byteBuffer.put(updateFlags);
            if (rotationU) byteBuffer.putFloat(rotation);
            if (widthU) byteBuffer.putFloat(width);
            if (heightU) byteBuffer.putFloat(height);

            if (!fullUpdate) {
                this.updateFlags = (byte) 0;
            }
            return byteBuffer.array();
        }
    }

    public static final float MAX_VIEWPORT_WIDTH = 16;
    public static final float MAX_VIEWPORT_HEIGHT = 9;
    public static final int VIEWPORT_SCALE = 4;

    private String name;
    private Player.Turret turret;

    private static byte nameUpdate = 1;
    private static byte turretUpdate = 1 << 1;
    private static byte bulletsUpdate = 1 << 2;

    private static byte FULL_UPDATE_FLAGS = (byte) (nameUpdate | turretUpdate | bulletsUpdate);
    private byte updateFlags = (byte) 0;

    List<Entity> knownEntities = new ArrayList<>();

    private int viewportWidth;
    private int viewportHeight;

    Player(int id, float x, float y, float speed, float direction, float rotation, float width, float height, String name, int viewportWidth, int viewportHeight, Player.Turret turret) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.name = name;
        this.turret = turret;
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

    @Override
    byte getUpdateFlags() {
        return updateFlags;
    }

    @Override
    void move(int tick, float dt) {

    }

    @Override
    boolean update(JSONArray jsonArray) {
        if (!super.update(jsonArray.getJSONArray(1))) {
            return false;
        }
        turret.update(jsonArray.getJSONArray(3));
        if (turret.updateFlags != 0) {
            updateFlags |= turretUpdate;
        }
        return true;
    }

    @Override
    byte[] serialize(boolean fullUpdate) {
        byte[] entity = super.serialize(fullUpdate);
        byte updateFlags;
        if (fullUpdate) {
            updateFlags = FULL_UPDATE_FLAGS;
        } else {
            updateFlags = this.updateFlags;
        }
        byte[] name;
        if ((updateFlags & nameUpdate) == nameUpdate) {
            byte[] n = this.name.getBytes(StandardCharsets.UTF_8);//todo inefficient?
            ByteBuffer nameByteBuffer = ByteBuffer.allocate(2 + n.length);
            nameByteBuffer.putShort((short) n.length);
            nameByteBuffer.put(n);
            name = nameByteBuffer.array();
        } else {
            name = new byte[0];
        }
        byte[] turret;
        if ((updateFlags & turretUpdate) == turretUpdate) {
            turret = this.turret.serialize(fullUpdate);
        } else {
            turret = new byte[0];
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(entity.length + 1 + name.length + turret.length);
        byteBuffer.put(entity);
        byteBuffer.put(updateFlags);
        byteBuffer.put(name);
        byteBuffer.put(turret);

        if (!fullUpdate) {
            this.updateFlags = (byte) 0;
        }
        return byteBuffer.array();
    }
}
