package com.laidw.gobang.core.combo.four;

import com.laidw.gobang.core.constant.Direction;

/**
 * 跳冲四
 */
public class JumpFour extends RushFour {

    public JumpFour(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int length() {
        return 5;
    }
}
