package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;

class Entity {

    final int id;
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

    private byte updateFlags = (byte) ((xUpdate | yUpdate | speedUpdate | directionUpdate | rotationUpdate | widthUpdate | heightUpdate) ^ 0x80);

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

    void move(float dt) {
        this.x += Math.cos(this.direction) * this.speed * dt / 1000;
        this.y += Math.sin(this.direction) * this.speed * dt / 1000;
    }

    boolean update(JSONArray jsonArray) {
        if (jsonArray.getInt(0) != id) {
            return false;
        }

        x = jsonArray.getFloat(1);
        y = jsonArray.getFloat(2);
        speed = jsonArray.getFloat(3);
        direction = jsonArray.getFloat(4);
        rotation = jsonArray.getFloat(5);
        width = jsonArray.getFloat(6);
        height = jsonArray.getFloat(7);
        return true;
    }

    byte[] serialize() {
        boolean xU = (updateFlags & xUpdate) == xUpdate;
        boolean yU = (updateFlags & yUpdate) == yUpdate;
        boolean speedU = (updateFlags & speedUpdate) == speedUpdate;
        boolean directionU = (updateFlags & directionUpdate) == directionUpdate;
        boolean rotationU = (updateFlags & rotationUpdate) == rotationUpdate;
        boolean widthU = (updateFlags & widthUpdate) == widthUpdate;
        boolean heightU = (updateFlags & heightUpdate) == heightUpdate;
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 1 + 2 + ((xU ? 4 : 0) + (yU ? 4 : 0) + (speedU ? 4 : 0) + (directionU ? 4 : 0) + (rotationU ? 4 : 0) + (widthU ? 4 : 0) + (heightU ? 4 : 0)));
        byteBuffer.putShort((short) 1); //entity id is 1
        byteBuffer.put(updateFlags);
        byteBuffer.putShort((short) id);
        if (xU) byteBuffer.putFloat(x);
        if (yU) byteBuffer.putFloat(y);
        if (speedU) byteBuffer.putFloat(speed);
        if (directionU) byteBuffer.putFloat(direction);
        if (rotationU) byteBuffer.putFloat(rotation);
        if (widthU) byteBuffer.putFloat(width);
        if (heightU) byteBuffer.putFloat(height);

        return byteBuffer.array();
    }

    static Entity deserialize(JSONArray jsonArray) {
        return new Entity((char) jsonArray.getInt(0), jsonArray.getFloat(1), jsonArray.getFloat(2), jsonArray.getFloat(3), jsonArray.getFloat(4), jsonArray.getFloat(5), jsonArray.getFloat(6), jsonArray.getFloat(7));
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
