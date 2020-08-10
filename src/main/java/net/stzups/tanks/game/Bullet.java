package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;

class Bullet extends Entity {

    public static final int ID = 3;

    private static final int MAX_HITS = 3;

    final private int owner;
    private short hits;

    private static byte ownerUpdate = 1;
    private static byte hitUpdate = 1 << 1;

    private static byte FULL_UPDATE_FLAGS = (byte) (ownerUpdate | hitUpdate);
    byte updateFlags = FULL_UPDATE_FLAGS;

    Bullet(int id, float x, float y, float speed, float direction, float rotation, float width, float height, int owner, short hits) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.owner = owner;
        this.hits = hits;
    }

    @Override
    byte getUpdateFlags() {
        return updateFlags;
    }

    @Override
    boolean update(JSONArray jsonArray) {
        super.update(jsonArray.getJSONArray(0));
        short hits = (short) jsonArray.getInt(2);
        if (this.hits != hits) {
            this.hits = hits;
            updateFlags |= hitUpdate;
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
        boolean ownerU = (updateFlags & ownerUpdate) == ownerUpdate;
        boolean hitU = (updateFlags & hitUpdate) == hitUpdate;
        ByteBuffer byteBuffer = ByteBuffer.allocate(entity.length + 1 + (ownerU ? 2 : 0) + (hitU ? 1 : 0));
        byteBuffer.put(entity);
        byteBuffer.put(updateFlags);
        if (ownerU) byteBuffer.putShort((short) owner);
        if (hitU) byteBuffer.put((byte) hits);

        if (!fullUpdate) {
            this.updateFlags = (byte) 0;
        }
        return byteBuffer.array();
    }

    static Bullet deserialize(JSONArray jsonArray) {
        Entity entity = Entity.deserialize(jsonArray.getJSONArray(0));
        return new Bullet(entity.id, entity.x, entity.y, entity.speed, entity.direction, entity.rotation, entity.width, entity.height, (char) jsonArray.getInt(1), (short) jsonArray.getInt(2));
    }
}
