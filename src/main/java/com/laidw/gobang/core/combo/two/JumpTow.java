package com.laidw.gobang.core.combo.two;

import com.laidw.gobang.core.constant.Direction;

/**
 * 两子距离为1的活二
 */
public class JumpTow extends LiveTwo {

    public JumpTow(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int score() {
        return 5;
    }

    @Override
    public int length() {
        return 3;
    }
}
