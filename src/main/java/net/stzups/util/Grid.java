package net.stzups.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid<T> {

    private Map<Integer, Map<Integer, List<T>>> grid = new HashMap<>();

    public void insert(int columnIndex, int rowIndex, T data) {
        if(!grid.containsKey(columnIndex)) {
            //System.out.println("added");
            grid.put(columnIndex, new HashMap<>());
        }
        /*
        System.out.println(grid);
        System.out.println(grid.containsKey(columnIndex));
        System.out.println(grid.get(columnIndex));
        System.out.println("");
        */
        Map<Integer, List<T>> column = grid.get(columnIndex);

        if(!column.containsKey((rowIndex))) {
            column.put(rowIndex, new ArrayList<>());
        }

        column.get(rowIndex).add(data);
    }

    public void remove(int columnIndex, int rowIndex, T data) {
        if(!grid.containsKey((columnIndex)))
            return;
        Map<Integer, List<T>> column = grid.get(columnIndex);

        if(!column.containsKey(rowIndex))
            return;

        List<T> row = column.get(rowIndex);
        row.remove(data);

        if(row.size() == 0) {
            column.remove(rowIndex);
            if(column.size() == 0) {
                grid.remove(columnIndex);
            }
        }
    }

    public List<T> get(int columnIndex, int rowIndex) {
        if(!grid.containsKey((columnIndex)))
            return new ArrayList<>();
        Map<Integer, List<T>> column = grid.get(columnIndex);

        if(!column.containsKey(rowIndex))
            return new ArrayList<>();

        return column.get(rowIndex);
    }

    public Map<Integer, List<T>> get(int columnIndex) {
        if(!grid.containsKey((columnIndex)))
            return new HashMap<>();
        return grid.get(columnIndex);
    }

    public Map<Integer, Map<Integer, List<T>>> get() {
        return grid;
    }
}
