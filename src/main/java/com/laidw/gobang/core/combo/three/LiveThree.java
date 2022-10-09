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

    @Override
    public int destroyScore() {
        return 10000; // 在打分环节中，一般是不会出现破坏对方活三的情况的，因为发现对方的活三后我们一般会立刻去堵，不会留到打分环节来评判
    }
}
