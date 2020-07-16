package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.ArrayList;
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

        void update(JSONArray jsonArray) {
            rotation = jsonArray.getFloat(0);
            width = jsonArray.getInt(1);
            height = jsonArray.getInt(2);
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

    Player(UUID id, float x, float y, float speed, float direction, float rotation, int width, int height, String name, int viewportWidth, int viewportHeight, Player.Turret turret, List<Bullet> bullets) {
        super(id, x, y, speed, direction, rotation, width, height);
        this.name = name;
        this.turret = turret;
        this.bullets = bullets;
        setViewport(viewportWidth, viewportHeight);
    }

    void setViewport(int viewportWidth, int viewportHeight) {
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
    boolean update(JSONArray jsonArray) {
        if (!super.update(jsonArray.getJSONArray(1))) {
            return false;
        }
        turret.update(jsonArray.getJSONArray(3));
        //todo bullets
        return true;
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
        return "[player," + super.serialize() + "," + name + "," + turret.serialize() + "," + bullets.toString() + "]";
    }
}
