package com.laidw.gobang.core.constant;

import com.laidw.gobang.core.util.XyUtil;

/**
 * 五子棋的4个方向
 */
public enum Direction {
    TRANSVERSE(0, 1), VERTICAL(1, 0), NEG_SLASH(1, 1), POS_SLASH(1, -1);

    public final int dx, dy;
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void step(int[] point) {
        point[0] += dx;
        point[1] += dy;
    }

    public int[] newStep(int[] point) {
        return new int[] {point[0] + dx, point[1] + dy};
    }

    public int[] newStep(int[] point, int steps) {
        return new int[] {point[0] + dx * steps, point[1] + dy * steps};
    }

    /**
     * 已知beginPosition和targetPosition的连线和当前方向平行，且targetPosition可能在beginPosition的正方向或反方向上
     * 判断targetPosition是否在beginPosition的正方向上的steps步之内
     */
    public boolean isInRange(int beginPosition, int targetPosition, int steps) {
        if (dx != 0) {
            int distance = XyUtil.x(targetPosition) - XyUtil.x(beginPosition);
            return distance >= 0 && distance / dx <= steps;
        }
        int distance = XyUtil.y(targetPosition) - XyUtil.y(beginPosition);
        return distance >= 0 && distance / dy <= steps;
    }
}