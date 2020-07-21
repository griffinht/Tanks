package net.stzups.tanks.game;

import net.stzups.util.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class World {
    static final int GRID_SIZE = 16; //todo dynamic grid size???

    Grid<Entity> grid = new Grid<>();

    World() {

    }

    void update(int tick, float dt) {
        for (Integer columnKey : new TreeSet<>(grid.get().keySet())) {
            Map<Integer, List<Entity>> column = grid.get(columnKey);
            for (Integer rowKey : new TreeSet<>(column.keySet())) {
                List<Entity> row = column.get(rowKey);
                for (Entity entity : new ArrayList<>(row)) {
                    entity.x += entity.speed * Math.cos(entity.direction) * dt / 1000;//speed is units per second
                    entity.y += entity.speed * Math.sin(entity.direction) * dt / 1000;
                    if (columnKey != (int) entity.x / GRID_SIZE || rowKey != (int) entity.y / GRID_SIZE) {
                        grid.remove(columnKey, rowKey, entity);
                        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
                    }
                }
            }
        }
    }

    void addEntity(Entity entity) {
        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
    }

    void removeEntity(Entity entity) {
        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
    }
}
