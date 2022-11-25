package com.laidw.gobang.core.combo.four;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Direction;

/**
 * 冲四
 */
public abstract class RushFour extends Combo {

    protected RushFour(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 1;
    }
}
