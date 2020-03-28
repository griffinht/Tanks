package net.stzups.tanks.game;

class Player extends Object {
    private String name;

    Player(String name, double x, double y) {
        super(x, y, 0, 0);
        this.name = name;
    }

    String getName() {
        return name;
    }
}
