package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;

class Entity {

    public static final int ID = 1;

    private int tick;

    int id;
    float x;
    float y;
    float speed;
    float direction;
    float rotation;
    float width;
    float height;

    private static byte xUpdate = 1;
    private static byte yUpdate = 1 << 1;
    private static byte speedUpdate = 1 << 2;
    private static byte directionUpdate = 1 << 3;
    private static byte rotationUpdate = 1 << 4;
    private static byte widthUpdate = 1 << 5;
    private static byte heightUpdate = 1 << 6;

    private byte FULL_UPDATE_FLAGS = (byte) (xUpdate | yUpdate | speedUpdate | directionUpdate | rotationUpdate | widthUpdate | heightUpdate);
    private byte updateFlags = FULL_UPDATE_FLAGS;

    Entity(int id, float x, float y, float speed, float direction, float rotation, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        this.rotation = rotation;
        this.width = width;
        this.height = height;
    }

    byte getUpdateFlags() {
        return updateFlags;
    }

    void move(int tick, float dt) {
        if (tick > this.tick) {
            this.tick = tick;
            x += Math.cos(this.direction) * this.speed * dt / 1000;
            y += Math.sin(this.direction) * this.speed * dt / 1000;
        }
    }

    boolean update(JSONArray jsonArray) {
        if (jsonArray.getInt(0) != id) {
            return false;
        }
        byte uUpdateFlags = (byte) jsonArray.getInt(1);
        int offset = 2;
        if ((uUpdateFlags & Entity.xUpdate) == Entity.xUpdate) {
            float x = jsonArray.getFloat(offset);
            offset += 1;
            if (this.x != x) {
                this.x = x;
                updateFlags |= xUpdate;
                updateFlags |= yUpdate;
            }
        }
        if ((uUpdateFlags & Entity.yUpdate) == Entity.yUpdate) {
            float y = jsonArray.getFloat(offset);
            offset += 1;
            if (this.y != y) {
                this.y = y;
                updateFlags |= xUpdate;
                updateFlags |= yUpdate;
            }
        }
        if ((uUpdateFlags & Entity.speedUpdate) == Entity.speedUpdate) {
            float speed = jsonArray.getFloat(offset);
            offset += 1;
            if (this.speed != speed) {
                this.speed = speed;
                updateFlags |= speedUpdate;
            }
        }
        if ((uUpdateFlags & Entity.directionUpdate) == Entity.directionUpdate) {
            float direction = jsonArray.getFloat(offset);
            offset += 1;
            if (this.direction != direction) {
                this.direction = direction;
                updateFlags |= directionUpdate;
            }
        }
        if ((uUpdateFlags & Entity.rotationUpdate) == Entity.rotationUpdate) {
            float rotation = jsonArray.getFloat(offset);
            offset += 1;
            if (this.rotation != rotation) {
                this.rotation = rotation;
                updateFlags |= rotationUpdate;
            }
        }
        if ((uUpdateFlags & Entity.widthUpdate) == Entity.widthUpdate) {
            float width = jsonArray.getFloat(offset);
            offset += 1;
            if (this.width != width) {
                this.width = width;
                updateFlags |= widthUpdate;
            }
        }
        if ((uUpdateFlags & Entity.heightUpdate) == Entity.heightUpdate) {
            float height = jsonArray.getFloat(offset);
            if (this.height != height) {
                this.height = height;
                updateFlags |= heightUpdate;
            }
        }
        return true;
    }

    byte[] serialize(boolean fullUpdate) {
        byte updateFlags;
        if (fullUpdate) {
            updateFlags = FULL_UPDATE_FLAGS;
        } else {
            updateFlags = this.updateFlags;
        }
        boolean xU = (updateFlags & xUpdate) == xUpdate;
        boolean yU = (updateFlags & yUpdate) == yUpdate;
        boolean speedU = (updateFlags & speedUpdate) == speedUpdate;
        boolean directionU = (updateFlags & directionUpdate) == directionUpdate;
        boolean rotationU = (updateFlags & rotationUpdate) == rotationUpdate;
        boolean widthU = (updateFlags & widthUpdate) == widthUpdate;
        boolean heightU = (updateFlags & heightUpdate) == heightUpdate;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 2 + ((xU ? 4 : 0) + (yU ? 4 : 0) + (speedU ? 4 : 0) + (directionU ? 4 : 0) + (rotationU ? 4 : 0) + (widthU ? 4 : 0) + (heightU ? 4 : 0)));
        byteBuffer.put(updateFlags);
        byteBuffer.putShort((short) id);
        if (xU) byteBuffer.putFloat(x);
        if (yU) byteBuffer.putFloat(y);
        if (speedU) byteBuffer.putFloat(speed);
        if (directionU) byteBuffer.putFloat(direction);
        if (rotationU) byteBuffer.putFloat(rotation);
        if (widthU) byteBuffer.putFloat(width);
        if (heightU) byteBuffer.putFloat(height);

        if (!fullUpdate) {
            this.updateFlags = (byte) 0;
        }
        return byteBuffer.array();
    }

    static Entity deserialize(JSONArray jsonArray) {
        return new Entity(jsonArray.getInt(0), jsonArray.getFloat(2), jsonArray.getFloat(3), jsonArray.getFloat(4), jsonArray.getFloat(5), jsonArray.getFloat(6), jsonArray.getFloat(7), jsonArray.getFloat(8));
    }
/*
    private static String readBytesToString(byte[] bytes) {
        StringBuilder string = new StringBuilder();

        for (byte b : bytes) {
            string.append((b >> 7) & 1);
            string.append((b >> 6) & 1);
            string.append((b >> 5) & 1);
            string.append((b >> 4) & 1);
            string.append((b >> 3) & 1);
            string.append((b >> 2) & 1);
            string.append((b >> 1) & 1);
            string.append(b & 1);
            string.append(" ");
            string.append(" (");
            string.append(b);
            string.append(") ");
        }

        if (string.length() > 0) {
            return string.substring(0, string.length() - 1);
        } else {
            return string.toString();
        }
    }

    private static String readBytesToString(byte b) {
        return readBytesToString(new byte[]{b});
    }*/
}
