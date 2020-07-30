package net.stzups.tanks.game;

import net.stzups.util.Grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

class World {
    static final int GRID_SIZE = 16; //todo dynamic grid size???

    Grid<Entity> grid = new Grid<>();
    Map<Character, Entity> entities = new HashMap<>();

    World() {

    }

    void update(int tick, float dt) {
        for (Integer columnKey : new TreeSet<>(grid.get().keySet())) {
            Map<Integer, List<Entity>> column = grid.get(columnKey);
            for (Integer rowKey : new TreeSet<>(column.keySet())) {
                List<Entity> row = column.get(rowKey);
                for (Entity entity : new ArrayList<>(row)) {
                    entity.move(dt);
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
        entities.put(entity.id, entity);
    }

    void removeEntity(Entity entity) {
        grid.insert((int) entity.x / GRID_SIZE, (int) entity.y / GRID_SIZE, entity);
        entities.remove(entity.id);
    }

    char generateRandomId() {
        //todo make this good
        char id = (char) (Math.random() * 65535);
        while (entities.containsKey(id)) {
            id = (char) (Math.random() * 65535);
        }
        return id;
    }
}
