package com.laidw.gobang.core.combo.two;

import com.laidw.gobang.core.constant.Direction;

/**
 * 两子距离为0的活二
 */
public class ConTow extends LiveTwo {

    public ConTow(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int score() {
        return 5;
    }

    @Override
    public int length() {
        return 2;
    }
}
