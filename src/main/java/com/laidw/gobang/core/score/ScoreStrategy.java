package com.laidw.gobang.core.score;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;

public interface ScoreStrategy {
    int getScoreOfPosition(GobangChess chess, Color me, Color opponent, Integer position);
}
