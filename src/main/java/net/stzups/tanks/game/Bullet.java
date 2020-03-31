package net.stzups.tanks.game;

import java.util.UUID;

public class Bullet extends Entity {
    private static final int MAX_HITS = 3;
    Player owner;
    int hits;
    Bullet(UUID id, double x, double y, double speed, double direction, double rotation, int width, int height, Player owner, int hits) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.owner = owner;
        this.hits = hits;
    }
}
