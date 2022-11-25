package com.laidw.gobang.core.combo.three;

import com.laidw.gobang.core.constant.Direction;

/**
 * 跳活三
 */
public class JumpThree extends LiveThree {

    public JumpThree(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int length() {
        return 4;
    }
}
