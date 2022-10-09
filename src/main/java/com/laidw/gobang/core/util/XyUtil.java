package com.laidw.gobang.core.util;

/**
 * 处理坐标相关的逻辑
 */
public abstract class XyUtil {
    public static final int NULL_POSITION = -101;

    private XyUtil() {}

    public static int position(int x, int y) {
        return x * 100 + y;
    }

    public static int x(int position) {
        return position / 100;
    }

    public static int y(int position) {
        return position % 100;
    }
}
