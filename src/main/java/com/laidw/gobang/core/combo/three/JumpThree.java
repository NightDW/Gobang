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
    public int score() {
        return 13; // 活三一般是由活二转换来的，因此活三的分数要包含活二的分数
    }

    @Override
    public int length() {
        return 4;
    }
}
