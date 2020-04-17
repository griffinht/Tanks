package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.UUID;

class Entity {

    final UUID id;
    double x;
    double y;
    double speed;
    double direction;
    double rotation;
    int width;
    int height;

    Entity(UUID id, double x, double y, double speed, double direction, double rotation, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        this.rotation = rotation;
        this.width = width;
        this.height = height;
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
        width = jsonArray.getInt(6);
        height = jsonArray.getInt(7);
        return true;
    }

    String serialize() {
        return "[" + id.toString() + "," + x + "," + y + "," + speed + "," + direction + "," + rotation + "," + width + "," + height + "]";
    }
}
