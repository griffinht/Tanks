package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.util.UUID;

class Bullet extends Entity {
    private static final int MAX_HITS = 3;

    final private int owner;
    private short hits;

    private static byte ownerUpdate = 1;
    private static byte hitUpdate = 1 << 1;

    private byte updateFlags = (byte) (ownerUpdate | hitUpdate);

    Bullet(int id, float x, float y, float speed, float direction, float rotation, float width, float height, int owner, short hits) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.owner = owner;
        this.hits = hits;
    }

    @Override
    boolean update(JSONArray jsonArray) {
        super.update(jsonArray.getJSONArray(0));
        hits = (short) jsonArray.getInt(2);
        return true;
    }

    @Override
    byte[] serialize() {
        byte[] entity = super.serialize();
        boolean ownerU = (updateFlags & ownerUpdate) == ownerUpdate;
        boolean hitU = (updateFlags & hitUpdate) == hitUpdate;
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + entity.length + 1 + (ownerU ? 2 : 0) + (hitU ? 1 : 0));
        byteBuffer.putShort((short) 3);
        byteBuffer.put(entity);
        byteBuffer.put(updateFlags);
        if (ownerU) byteBuffer.putShort((short) owner);
        if (hitU) byteBuffer.put((byte) hits);
        return byteBuffer.array();
    }

    static Bullet deserialize(JSONArray jsonArray) {
        if (jsonArray.getJSONArray(0).getString(0).equals("newBullet")) {
            jsonArray.getJSONArray(0).put(0, UUID.randomUUID());
        }
        Entity entity = Entity.deserialize(jsonArray.getJSONArray(0));
        return new Bullet(entity.id, entity.x, entity.y, entity.speed, entity.direction, entity.rotation, entity.width, entity.height, (char) jsonArray.getInt(1), (short) jsonArray.getInt(2));
    }
}
