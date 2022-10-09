package com.laidw.gobang.core.combo;

import java.util.*;

/**
 * 主要用于维护冲四点/活三点/活二点，以及该点对应的冲四/活三/活二
 */
public class ComboMap {

    /**
     * key为冲四点/活三点/活二点，value为该点形成的冲四/活三/活二
     * 虽然value是Combo集合，但集合中的元素必须都是冲四/活三/活二
     */
    private final Map<Integer, List<Combo>> map = new HashMap<>();

    public void putIfNotEmpty(Integer position, List<Combo> combos) {
        if (!combos.isEmpty()) {
            map.put(position, combos);
        }
    }

    public void remove(Integer position) {
        map.remove(position);
    }

    public List<Combo> getOrEmpty(Integer position) {
        List<Combo> list;
        return (list = map.get(position)) == null ? Collections.emptyList() : list;
    }

    public Set<Integer> keySet() {
        return map.keySet();
    }

    public Set<Map.Entry<Integer, List<Combo>>> entrySet() {
        return map.entrySet();
    }

    public ComboMap unmodified() {
        return new UnmodifiedComboMap();
    }


    private class UnmodifiedComboMap extends ComboMap {
        private final Map<Integer, List<Combo>> localMap = new HashMap<>(map);

        @Override
        public void putIfNotEmpty(Integer position, List<Combo> combos) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(Integer position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Combo> getOrEmpty(Integer position) {
            List<Combo> list;
            return (list = localMap.get(position)) == null ? Collections.emptyList() : list;
        }

        @Override
        public Set<Integer> keySet() {
            return localMap.keySet();
        }

        @Override
        public Set<Map.Entry<Integer, List<Combo>>> entrySet() {
            return localMap.entrySet();
        }

        @Override
        public ComboMap unmodified() {
            return this;
        }
    }
}
