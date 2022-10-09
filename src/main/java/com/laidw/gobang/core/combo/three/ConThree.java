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
    public int score() {
        return 15; // 活三一般是由活二转换来的，因此活三的分数要包含活二的分数
    }

    @Override
    public int length() {
        return 3;
    }
}
