package com.laidw.gobang.web.chess;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.util.XyUtil;

import java.util.List;

/**
 * 提供一些方法供Thymeleaf模板调用
 */
public class ViewableChess extends GobangChess {
    public ViewableChess(int dimension) {
        super(dimension);
    }

    public String getBlankType(int x, int y) {
        int position = XyUtil.position(x, y);
        int value = 0;
        for (Color color : Color.values()) {
            value += getRushFourMapOf(color).keySet().contains(position) ? color.value : 0;
        }
        return value == 0 ? "-" : value + "";
    }

    public boolean isLast(int x, int y) {
        List<Integer> historyPositions = getHistoryPositions();
        return !historyPositions.isEmpty() && historyPositions.get(historyPositions.size() - 1) == XyUtil.position(x, y);
    }
}
