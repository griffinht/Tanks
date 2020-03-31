package net.stzups.tanks.game;

import org.json.JSONArray;
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
        JSONArray jsonBlocks = new JSONArray();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                Block block = blocks[x][y];
                if (block != null) {
                    jsonBlocks.put(block.serialize());
                }
            }
        }
        if (jsonBlocks.length() > 0) {
            sector.put("blocks", jsonBlocks);
        }

        JSONArray jsonEntities = new JSONArray();
        for (Entity entity : entities) {
            jsonEntities.put(entity.serialize());
        }
        if (jsonEntities.length() > 0) {
            sector.put("entities", jsonEntities);
        }

        return sector;
    }
}
