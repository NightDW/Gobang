package com.laidw.gobang.core.combo.three;

import com.laidw.gobang.core.constant.Direction;

/**
 * 连活三
 */
public class ConThree extends LiveThree {

    public ConThree(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int length() {
        return 3;
    }
}
