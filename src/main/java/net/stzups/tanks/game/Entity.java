package net.stzups.tanks.game;

class Entity {
    public double x;
    public double y;

    public double speed;
    public double direction;

    Entity(double x, double y, double speed, double direction) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

    String serialize() {
        return speed + "," + direction;
    }
}
