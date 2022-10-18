package com.laidw.gobang.core.score.impl;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.score.ScoreStrategy;
import com.laidw.gobang.core.util.XyUtil;

/**
 * 简单的打分策略
 */
public class SimpleScoreStrategy implements ScoreStrategy {

    @Override
    public int getScoreOfPosition(GobangChess chess, Color me, Color opponent, Integer position) {

        // TODO 这里可能会有重复统计，比如_O_OO_这个Combo会被识别为一个跳活三和一个活二；这个是LineScanner扫描逻辑的问题，暂时没有比较好的解决方法
        return chess.tryAndGetNetScore(XyUtil.x(position), XyUtil.y(position), me);
    }
}
