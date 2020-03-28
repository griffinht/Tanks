package net.stzups.tanks.game;

class World {
    private static final int SECTOR_SIZE = 16;
    private static final int WORLD_SECTORS = 100;

    Sector[][] sectors = new Sector[WORLD_SECTORS][WORLD_SECTORS];

    World() {
        //populate sectors
        for (int x = 0; x < sectors.length; x++) {
            for (int y = 0; y < sectors[x].length; y++) {
                sectors[x][y] = new Sector(x, y, SECTOR_SIZE);
            }
        }
    }

    void tick() {
        for (Sector[] sectorsX : sectors) {
            for (Sector sector : sectorsX) {
                for (Object object : sector.objects) {
                    object.x += object.speed * Math.cos(object.direction);
                    object.y += object.speed * Math.sin(object.direction);
                    if ((int)object.x > sector.x * SECTOR_SIZE + SECTOR_SIZE
                            || object.x < sector.x * SECTOR_SIZE
                            || (int)object.y > sector.y * SECTOR_SIZE + SECTOR_SIZE
                            || object.y < sector.y * SECTOR_SIZE) {
                        Sector s = sectors[(int)object.x/SECTOR_SIZE][(int)object.y/SECTOR_SIZE];
                        System.out.println("Moving object at (" + object.x + ", " + object.y + ") from sector " + sector.x + ", " + sector.y + " to sector " + s.x + ", " + s.y);
                        sector.objects.remove(object);
                        sectors[(int)object.x/SECTOR_SIZE][(int)object.y/SECTOR_SIZE].objects.add(object);//todo out of bounds
                    }
                }
            }
        }
    }

    void addObject(Object object) {
        sectors[(int)object.x/SECTOR_SIZE][(int)object.y/SECTOR_SIZE].objects.add(object);//todo out of bounds
    }
}
