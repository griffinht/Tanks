package net.stzups.tanks.game;

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
                        Sector s = sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE];
                        System.out.println("Moving object at (" + entity.x + ", " + entity.y + ") from sector " + sector.x + ", " + sector.y + " to sector " + s.x + ", " + s.y);
                        sector.entities.remove(entity);
                        sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE].entities.add(entity);//todo out of bounds
                    }
                }
            }
        }
    }

    void addObject(Entity entity) {
        sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE].entities.add(entity);//todo out of bounds
    }

    void removeObject(Entity entity) {
        sectors[(int) entity.x/SECTOR_SIZE][(int) entity.y/SECTOR_SIZE].entities.remove(entity);
    }
}
