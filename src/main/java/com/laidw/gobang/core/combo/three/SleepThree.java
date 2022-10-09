package com.laidw.gobang.core.combo.three;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Direction;

/**
 * 眠三
 */
public class SleepThree extends Combo {

    public SleepThree(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 3;
    }

    @Override
    public int score() {
        return 3; // 一般不会刻意去构造眠三（不如形成活三，然后等对方来堵），因此眠三的分数不能太高
    }

    @Override
    public int destroyScore() {
        return 2; // 同理，破坏对方的眠三的意义也不大
    }
}
