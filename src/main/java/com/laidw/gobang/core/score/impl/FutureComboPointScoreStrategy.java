package com.laidw.gobang.core.score.impl;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.combo.ComboMap;
import com.laidw.gobang.core.constant.Color;

import java.util.List;
import java.util.Map;

/**
 * 根据落子后我方有几个冲四点/活三点/活二点来打分
 */
public class FutureComboPointScoreStrategy extends AbstractComboPointScoreStrategy {

    public FutureComboPointScoreStrategy() {
        super(1, 7, 3);
    }

    public FutureComboPointScoreStrategy(int rushFourPointScore, int liveThreePointScore, int liveTwoPointScore) {
        super(rushFourPointScore, liveThreePointScore, liveTwoPointScore);
    }

    @Override
    public int getScoreOfPosition(GobangChess chess, Color me, Color opponent, Integer position) {
        int score = 0;
        chess.set(position, me);
        score += getScore(chess.getRushFourMapOf(me), rushFourPointScore);
        score += getScore(chess.getLiveThreeMapOf(me), liveThreePointScore);
        score += getScore(chess.getLiveTwoMapOf(me), liveTwoPointScore);
        chess.back();
        return score;
    }

    private static int getScore(ComboMap comboMap, int baseScore) {
        int count = 0;
        for (Map.Entry<Integer, List<Combo>> entry : comboMap.entrySet()) {
            count += entry.getValue().size();
        }
        return count * baseScore;
    }
}
