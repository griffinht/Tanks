package net.stzups.tanks.game;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.util.UUID;

class Bullet extends Entity {
    private static final int MAX_HITS = 3;

    final private char owner;
    private int hits;

    private static byte hitUpdate = 1;

    private byte updateFlags = hitUpdate;

    Bullet(char id, float x, float y, float speed, float direction, float rotation, float width, float height, char owner, int hits) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.owner = owner;
        this.hits = hits;
    }

    @Override
    boolean update(JSONArray jsonArray) {
        super.update(jsonArray.getJSONArray(0));
        hits = jsonArray.getInt(2);
        return true;
    }

    @Override
    byte[] serialize() {
        byte[] entity = super.serialize();
        boolean hitU = (updateFlags & hitUpdate) == hitUpdate;
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + entity.length + (hitU ? 1 : 0));
        return byteBuffer.array();
    }

    static Bullet deserialize(JSONArray jsonArray) {
        if (jsonArray.getJSONArray(0).getString(0).equals("newBullet")) {
            jsonArray.getJSONArray(0).put(0, UUID.randomUUID());
        }
        Entity entity = Entity.deserialize(jsonArray.getJSONArray(0));
        return new Bullet(entity.id, entity.x, entity.y, entity.speed, entity.direction, entity.rotation, entity.width, entity.height, (char) jsonArray.getInt(1), jsonArray.getInt(2));
    }
}
