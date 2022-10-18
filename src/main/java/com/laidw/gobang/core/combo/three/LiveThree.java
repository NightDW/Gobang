package com.laidw.gobang.core.combo.three;

import com.laidw.gobang.core.constant.Direction;

/**
 * 活三，包括连活三和跳活三
 */
public abstract class LiveThree extends SleepThree {

    protected LiveThree(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 2;
    }
}
