package net.stzups.tanks.game;

import java.util.HashMap;
import java.util.Map;

class World {
    static final int SECTOR_SIZE = 16;
    static final int WORLD_SECTORS = 100;

    final Sector[][] sectors = new Sector[WORLD_SECTORS][WORLD_SECTORS];

    Sector[][] changedSectors = new Sector[WORLD_SECTORS][WORLD_SECTORS];

    World() {
        //populate sectors
        for (int x = 0; x < sectors.length; x++) {
            for (int y = 0; y < sectors[x].length; y++) {
                sectors[x][y] = new Sector(x, y, SECTOR_SIZE);
            }
        }
    }

    void tick(int tick) {
        Map<Entity, Sector> moveEntities = new HashMap<>();
        for (Sector[] sectorsX : sectors) {
            for (Sector sector : sectorsX) {
                for (Entity entity : sector.entities) {
                    boolean move = false;
                    if (entity.speed != 0) {
                        entity.x += entity.speed * Math.cos(entity.direction);
                        move = true;
                    }
                    if (entity.speed != 0) {
                        entity.y += entity.speed * Math.sin(entity.direction);
                        move = true;
                    }
                    if (move &&
                            ((int) entity.x > sector.x * SECTOR_SIZE + SECTOR_SIZE
                            || entity.x < sector.x * SECTOR_SIZE
                            || (int) entity.y > sector.y * SECTOR_SIZE + SECTOR_SIZE
                            || entity.y < sector.y * SECTOR_SIZE)) {
                        moveEntities.put(entity, sector);
                    }
                }
            }
        }
        for(Map.Entry<Entity, Sector> entry : moveEntities.entrySet()) {
            entry.getValue().entities.remove(entry.getKey());
            if((int) entry.getKey().x/SECTOR_SIZE > World.WORLD_SECTORS - 1) {
                entry.getKey().x = (World.WORLD_SECTORS - 1)* SECTOR_SIZE;
            }
            if((int) entry.getKey().y/SECTOR_SIZE > World.WORLD_SECTORS - 1) {
                entry.getKey().y = (World.WORLD_SECTORS - 1) * SECTOR_SIZE;
            }
            sectors[(int) entry.getKey().x/SECTOR_SIZE][(int) entry.getKey().y/SECTOR_SIZE].entities.add(entry.getKey());//todo out of bounds
        }
    }

    void addObject(Entity entity) {
        sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE].entities.add(entity);//todo out of bounds
    }

    void removeObject(Entity entity) {
        sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE].entities.remove(entity);
    }
}
