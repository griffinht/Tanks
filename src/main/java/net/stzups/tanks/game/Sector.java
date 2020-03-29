package net.stzups.tanks.game;

import java.util.ArrayList;
import java.util.List;

class Sector {
    int x;
    int y;

    Block[][] blocks;
    List<Entity> entities = new ArrayList<>();

    Sector(int x, int y, int sectorSize) {
        blocks = new Block[sectorSize][sectorSize];
        this.x = x;
        this.y = y;
    }
}
