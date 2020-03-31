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

    JSONObject serialize() {
        JSONObject sector = new JSONObject();
        JSONObject jsonBlocks = new JSONObject();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                Block block = blocks[x][y];
                if (block != null) {
                    jsonBlocks.append(x + "," + y, block.serialize());
                }
            }
        }
        sector.append("blocks", jsonBlocks);

        JSONObject jsonEntities = new JSONObject();
        for (Entity entity : entities) {
            jsonEntities.append(entity.x + "," + entity.y, entity.serialize());
        }
        sector.append("entities", jsonEntities);

        return sector;
    }
}
