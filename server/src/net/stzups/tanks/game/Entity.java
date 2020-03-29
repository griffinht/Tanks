package net.stzups.tanks.game;

class Entity {
    double x;
    double y;

    double speed;
    double direction;

    Entity(double x, double y, double speed, double direction) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
    }

}
