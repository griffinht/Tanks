package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.UUID;

class Bullet extends Entity {
    private static final int MAX_HITS = 3;
    Player owner;
    int hits;
    Bullet(UUID id, float x, float y, float speed, float direction, float rotation, float width, float height, Player owner, int hits) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.owner = owner;
        this.hits = hits;
    }

    @Override
    boolean update(JSONArray jsonArray) {
        super.update(jsonArray.getJSONArray(0));
        hits = jsonArray.getInt(1);
        return true;
    }

    @Override
    String serialize() {
        return "[" + super.serialize() + "," + hits + "]";
    }
}
