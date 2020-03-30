package net.stzups.tanks.game;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class Sector {
    int x;
    int y;

    Block[][] blocks;
    List<Entity> entities = new ArrayList<>();

    Block[][] changedBlocks;
    List<Entity> changedEntities = new ArrayList<>();

    Sector(int x, int y, int sectorSize) {
        blocks = new Block[sectorSize][sectorSize];
        changedBlocks = new Block[sectorSize][sectorSize];
        this.x = x;
        this.y = y;
    }
}
