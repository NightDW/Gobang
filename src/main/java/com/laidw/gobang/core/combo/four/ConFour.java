package com.laidw.gobang.core.combo.four;

import com.laidw.gobang.core.constant.Direction;

/**
 * 连冲四
 */
public class ConFour extends RushFour {

    public ConFour(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int length() {
        return 4;
    }
}
