package net.stzups.tanks.game;

public class Player extends Entity {
    private String name;
    private double viewportX;
    private double viewportY;
    private int viewportWidth;
    private int viewportHeight;

    Player(String name, double x, double y) {
        super(x, y, 0, 0);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
