package net.stzups.tanks.game;

import net.stzups.tanks.server.Connection;

class Player extends Object {
    private String name;

    Player(Connection connection, String name, int x, int y) {
        super(x, y, 0, 0);
        this.name = name;
    }

    String getName() {
        return name;
    }
}
