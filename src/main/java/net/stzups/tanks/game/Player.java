package net.stzups.tanks.game;

import java.util.List;
import java.util.UUID;

public class Player extends Entity {
    static class Turret {
        double rotation;
        int width;
        int height;
        Turret (double rotation, int width, int height) {
            this.rotation = rotation;
            this.width = width;
            this.height = height;
        }

        String serialize() {
            return "[" + rotation + "," + width + "," + height + "]";
        }
    }

    private static final float MAX_VIEWPORT_WIDTH = 16;
    private static final float MAX_VIEWPORT_HEIGHT = 9;
    private static final int VIEWPORT_SCALE = 4;

    private String name;
    int viewportWidth;
    int viewportHeight;
    Player.Turret turret;
    List<Bullet> bullets;

    Player(UUID id, double x, double y, double speed, double direction, double rotation, int width, int height, String name, int viewportWidth, int viewportHeight, Player.Turret turret, List<Bullet> bullets) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.name = name;
        this.turret = turret;
        this.bullets = bullets;
        setViewport(viewportWidth, viewportHeight);
    }

    private void setViewport(int viewportWidth, int viewportHeight) {
        if ((float) viewportWidth / (float) viewportHeight >= MAX_VIEWPORT_WIDTH / MAX_VIEWPORT_HEIGHT) {
            this.viewportWidth = (int) (MAX_VIEWPORT_WIDTH * VIEWPORT_SCALE);
            this.viewportHeight = (int) ((float) this.viewportWidth * ((float) viewportHeight / (float) viewportWidth));
        } else {
            this.viewportHeight = (int) (MAX_VIEWPORT_HEIGHT * VIEWPORT_SCALE);
            this.viewportWidth = (int) ((float) this.viewportHeight * ((float) viewportWidth / (float) viewportHeight));
        }
    }

    public String getName() {
        return name;
    }

    @Override
    String serialize() {
        StringBuilder bullets = new StringBuilder();
        for (Bullet bullet : this.bullets) {
            bullets.append(",");
            bullets.append(bullet.serialize());
        }
        if (bullets.length() == 0) {
            bullets.append("[]");
        }
        return "[player," + super.serializeStripped() + "," + name + "," + turret.serialize() + "," + bullets.toString() + "]";
    }
}
