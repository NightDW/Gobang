package com.laidw.gobang.core.constant;

/**
 * 黑白双方
 */
public enum Color {
    BLACK(1), WHITE(2);

    public final int value;
    Color(int value) {
        this.value = value;
    }

    public Color reversed() {
        return this == BLACK ? WHITE : BLACK;
    }
}
