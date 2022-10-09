package com.laidw.gobang.core.util;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.combo.two.LiveTwo;
import com.laidw.gobang.core.combo.four.RushFour;
import com.laidw.gobang.core.combo.three.LiveThree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 和冲四点/活三点/活二点相关的一些操作
 */
public abstract class ComboUtil {
    private static final int COMBO_LIST_SIZE = 6;

    private ComboUtil() {}

    /**
     * 从position所属的4行的扫描结果中获取出由该点形成的冲四
     */
    public static List<Combo> getRushFoursOf(int position, Collection<Combo> scanResult) {
        List<Combo> rushFours = new ArrayList<>(COMBO_LIST_SIZE);
        for (Combo combo : scanResult) {
            if (combo instanceof RushFour && combo.getDirection().isInRange(combo.getPosition(), position, combo.length())) {
                rushFours.add(combo);
            }
        }
        return rushFours;
    }

    /**
     * 从position所属的4行的扫描结果中获取出由该点形成的活三
     */
    public static List<Combo> getLiveThreesOf(int position, Collection<Combo> scanResult) {
        List<Combo> liveThrees = new ArrayList<>(COMBO_LIST_SIZE);
        for (Combo combo : scanResult) {
            if (combo instanceof LiveThree && combo.getDirection().isInRange(combo.getPosition(), position, combo.length())) {
                liveThrees.add(combo);
            }
        }
        return liveThrees;
    }

    /**
     * 从position所属的4行的扫描结果中获取出由该点形成的活二
     */
    public static List<Combo> getLiveTwosOf(int position, Collection<Combo> scanResult) {
        List<Combo> liveTwos = new ArrayList<>(COMBO_LIST_SIZE);
        for (Combo combo : scanResult) {
            if (combo instanceof LiveTwo && combo.getDirection().isInRange(combo.getPosition(), position, combo.length())) {
                liveTwos.add(combo);
            }
        }
        return liveTwos;
    }

    /**
     * 已知rushFours是某个冲四点能形成的冲四，判断它是否真的是双冲四
     */
    public static boolean isMultiRushFours(List<Combo> rushFours) {
        int size = rushFours.size();
        if (size < 2) {
            return false;
        }

        // 有些冲四的防点可能是相同的，这种情况只会出现在同一个方向上
        // 比如XOO_O_OOX，在空格中放入我方棋子后，看似形成了两个冲四，但对方只需将另一个空格堵住，这两个冲四就被同时破解了
        int position = rushFours.get(0).getNextPositions().get(0);
        for (int i = 1; i < size; i++) {
            if (rushFours.get(i).getNextPositions().get(0) != position) {
                return true;
            }
        }

        return false;
    }
}