package com.laidw.gobang.core.combo.two;

import com.laidw.gobang.core.constant.Direction;

/**
 * 两子距离为2的活二
 */
public class FarTow extends LiveTwo {

    public FarTow(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int score() {
        return 3;
    }

    @Override
    public int length() {
        return 4;
    }
}
