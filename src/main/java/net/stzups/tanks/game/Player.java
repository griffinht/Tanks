package net.stzups.tanks.game;

public class Player extends Entity {

    private static final float MAX_VIEWPORT_WIDTH = 16;
    private static final float MAX_VIEWPORT_HEIGHT = 9;
    private static final int VIEWPORT_SCALE = 4;

    private String name;
    int viewportWidth;
    int viewportHeight;

    Player(String name, double x, double y, int viewportWidth, int viewportHeight) {
        super(x, y, 0, 0);
        this.name = name;
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
        return super.serialize() + ",player{" + name + "}";
    }
}
