package net.stzups.tanks.game;

class Block {
    int x;
    int y;

    Block(int x, int y) {
        this.x = x;
        this.y = y;
    }

    String serialize() {
        return "[" + x + "," + y + "]";
    }
}
