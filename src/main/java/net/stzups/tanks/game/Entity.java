package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.UUID;

class Entity {

    final UUID id;
    float x;
    float y;
    float speed;
    float direction;
    float rotation;
    float width;
    float height;

    Entity(UUID id, float x, float y, float speed, float direction, float rotation, float width, float height) {
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
        if (!UUID.fromString(jsonArray.getString(0)).equals(id)) {
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

    String serialize() {
        return "[" + id.toString() + "," + x + "," + y + "," + speed + "," + direction + "," + rotation + "," + width + "," + height + "]";
    }

    static Entity deserialize(JSONArray jsonArray) {
        Object rawId = jsonArray.get(0);
        UUID id;
        if (rawId instanceof UUID) {
            id = (UUID) rawId;
        } else {
            id = UUID.randomUUID();
        }
        return new Entity(id, jsonArray.getFloat(1), jsonArray.getFloat(2), jsonArray.getFloat(3), jsonArray.getFloat(4), jsonArray.getFloat(5), jsonArray.getFloat(6), jsonArray.getFloat(7));
    }
}
