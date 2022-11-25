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
}
