package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.UUID;

class Bullet extends Entity {
    private static final int MAX_HITS = 3;
    UUID owner;
    int hits;
    Bullet(UUID id, float x, float y, float speed, float direction, float rotation, float width, float height, UUID owner, int hits) {
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
    String serialize() {
        return "[" + super.serialize() + "," + owner + "," + hits + "]";
    }

    static Bullet deserialize(JSONArray jsonArray) {
        if (jsonArray.getJSONArray(0).getString(0).equals("newBullet")) {
            jsonArray.getJSONArray(0).put(0, UUID.randomUUID());
        }
        Entity entity = Entity.deserialize(jsonArray.getJSONArray(0));
        System.out.println(jsonArray.get(1) + ", " + jsonArray.get(2));
        return new Bullet(entity.id, entity.x, entity.y, entity.speed, entity.direction, entity.rotation, entity.width, entity.height, UUID.fromString(jsonArray.getString(1)), jsonArray.getInt(2));
    }
}
