package com.laidw.gobang.core.combo;

import com.laidw.gobang.core.constant.Direction;
import com.laidw.gobang.core.util.XyUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋子组合，如冲四、活三等
 */
@Getter
public abstract class Combo implements Comparable<Combo> {
    private static final int NEXT_POSITIONS_SIZE = 4;

    /**
     * 该组合的起点
     */
    private final int position;

    /**
     * 该组合的方向
     */
    private final Direction direction;

    /**
     * 对于活三来说，这里存的是该活三的防点；对于活二来说，这里存的是能使活二变成活三的点
     */
    private final List<Integer> nextPositions;

    protected Combo(int[] point, Direction direction, int[] next) {
        this.position = XyUtil.position(point[0], point[1]);
        this.direction = direction;
        this.nextPositions = new ArrayList<>(NEXT_POSITIONS_SIZE);
        addNextPosition(next);
    }

    @Override
    public final int compareTo(Combo o) {
        if (this == o) {
            return 0;
        }

        int orderResult, directionResult;
        if ((orderResult = order() - o.order()) != 0) {
            return orderResult;
        }

        // 这里原本的实现是这样的，但这么写是有问题的
        // 我一开始想的是，利用TreeSet来对Combo进行排序，order越小越排在前面，如果order一致，则按照插入顺序排序
        // 问题出现在按照插入顺序排序上：TreeSet底层是搜索二叉树，它要求元素之间必须有明确的大小关系
        // 如果按插入顺序排序，就忽略了元素的实际大小关系，统一返回1；这可能导致在本应该返回-1的情况下返回了1
        // return position == o.position && direction == o.direction ? 0 : 1;

        return (directionResult = direction.ordinal() - o.direction.ordinal()) != 0 ? directionResult : Integer.compare(position, o.position);
    }

    public final Combo addNextPosition(int[] point) {
        this.nextPositions.add(XyUtil.position(point[0], point[1]));
        return this;
    }

    public final Combo addNextPositionIf(boolean condition, int[] point) {
        if (condition) {
            this.nextPositions.add(XyUtil.position(point[0], point[1]));
        }
        return this;
    }

    /**
     * 排序；黑白方的Combo将存在各自的TreeSet中；值越小越排在前面
     */
    public abstract int order();

    /**
     * 我方形成该组合能获得的分数；在打分的时候用到
     */
    public abstract int score();

    /**
     * 我方破坏对方的该组合能够得的分数；在打分的时候用到
     */
    public abstract int destroyScore();

    /**
     * 该组合的长度；一般只有冲四、活三和活二会用到
     */
    public int length() {
        throw new UnsupportedOperationException();
    }
}
