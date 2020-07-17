package net.stzups.tanks.game;

import net.stzups.util.Grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class World {
    static final int GRID_SIZE = 64; //todo dynamic grid size???

    Grid<Entity> grid = new Grid<>();

    World() {

    }

    void tick(int tick) {
        for (Integer columnKey : new TreeSet<>(grid.get().keySet())) {
            Map<Integer, List<Entity>> column = grid.get(columnKey);
            for (Integer rowKey : new TreeSet<>(column.keySet())) {
                List<Entity> row = column.get(rowKey);
                Iterator<Entity> iterator = row.iterator();
                for (Entity entity : new ArrayList<>(row)) {
                    if (entity.speed != 0) {
                        entity.x += entity.speed * Math.cos(entity.direction);
                    }
                    if (entity.speed != 0) {
                        entity.y += entity.speed * Math.sin(entity.direction);
                    }
                    if (columnKey != (int) entity.x / GRID_SIZE || rowKey != (int) entity.y / GRID_SIZE) {
                        grid.remove(columnKey, rowKey, entity);
                        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
                    }
                }
            }
        }
        System.out.println();
    }

    void addEntity(Entity entity) {
        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
    }

    void removeEntity(Entity entity) {
        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
    }
}
