package net.stzups.tanks.game;

import org.json.JSONArray;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class Player extends Entity {
    static class Turret {
        double rotation;
        float width;
        float height;
        Turret (double rotation, float width, float height) {
            this.rotation = rotation;
            this.width = width;
            this.height = height;
        }

        void update(JSONArray jsonArray) {
            rotation = jsonArray.getFloat(0);
            width = jsonArray.getFloat(1);
            height = jsonArray.getFloat(2);
        }

        String serialize() {
            return "[" + rotation + "," + width + "," + height + "]";
        }
    }

    private static final float MAX_VIEWPORT_WIDTH = 16;
    private static final float MAX_VIEWPORT_HEIGHT = 9;
    private static final int VIEWPORT_SCALE = 4;

    private String name;
    private Player.Turret turret;
    private Map<UUID, Bullet> bullets;

    private int viewportWidth;
    private int viewportHeight;
    private boolean updateViewport = false;
    private Queue<Map.Entry<UUID, Long>> pingQueue = new ArrayDeque<>();
    private int ping;

    Player(UUID id, float x, float y, float speed, float direction, float rotation, float width, float height, String name, int viewportWidth, int viewportHeight, Player.Turret turret, Map<UUID, Bullet> bullets) {
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
        updateViewport = true;
    }

    boolean updateViewport() {
        if (updateViewport) {
            updateViewport = false;
            return true;
        } else {
            return false;
        }
    }

    void addBullet(Bullet bullet) {
        bullets.put(bullet.id, bullet);
    }

    public String getName() {
        return name;
    }

    int getViewportWidth() {
        return viewportWidth;
    }

    int getViewportHeight() {
        return viewportHeight;
    }

    int getPing() {
        return ping;
    }

    Queue<Map.Entry<UUID, Long>> getPingQueue() {
        return pingQueue;
    }

    void setPing(int ping) {
        this.ping = ping;
    }

    @Override
    void move(float dt) {

    }

    @Override
    boolean update(JSONArray jsonArray) {
        if (!super.update(jsonArray.getJSONArray(1))) {
            return false;
        }
        turret.update(jsonArray.getJSONArray(3));
        return true;
    }

    @Override
    String serialize() {
        StringBuilder bullets = new StringBuilder();
        for (Map.Entry<UUID, Bullet> entry : this.bullets.entrySet()) {
            bullets.append(entry.getValue().id);
            bullets.append(",");
        }
        if (bullets.length() > 0) {
            bullets.substring(0, bullets.length() - 1);
        }
        return "[player," + super.serialize() + "," + name + "," + turret.serialize() + ",[" + bullets.toString() + "]]";
    }
}
