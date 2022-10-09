package com.laidw.gobang.core.combo.four;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Direction;

/**
 * 冲四
 */
public abstract class RushFour extends Combo {

    protected RushFour(int[] point, Direction direction, int[] next) {
        super(point, direction, next);
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public int score() {
        return 0; // 冲四的得分是0，因为进入打分环节时，我们已经知道VCT和VCF不可能成功，因此构造冲四只会浪费我方的资源
    }

    @Override
    public int destroyScore() {
        return 10000; // 在打分环节中，一般是不会出现破坏对方冲四的情况的，因为发现对方的冲四后我们一般会立刻去堵，不会留到打分环节来评判
    }
}