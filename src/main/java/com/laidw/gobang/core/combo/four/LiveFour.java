package com.laidw.gobang.core.combo.four;

import com.laidw.gobang.core.constant.Direction;

/**
 * 活四
 */
public class LiveFour extends ConFour {

    public LiveFour(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 0;
    }
}
