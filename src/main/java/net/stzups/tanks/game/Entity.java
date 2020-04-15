package net.stzups.tanks.game;

import java.util.UUID;

class Entity {

    UUID id;
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

    String serialize() {
        return "[" + id.toString() + "," + x + "," + y + "," + speed + "," + direction + "," + rotation + "," + width + "," + height + "]";
    }

    protected String serializeStripped() {
        return id.toString() + "," + x + "," + y + "," + speed + "," + direction + "," + rotation + "," + width + "," + height;
    }
}
