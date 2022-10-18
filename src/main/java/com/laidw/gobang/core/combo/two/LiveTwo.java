package com.laidw.gobang.core.combo.two;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Direction;

/**
 * 活二；根据两个子的距离，活二可以分成3种
 */
public abstract class LiveTwo extends Combo {

    protected LiveTwo(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 4;
    }
}
