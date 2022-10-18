package com.laidw.gobang.core.score.impl;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;

/**
 * 根据某个位置对对手的重要程度来评分；如果该位置是对手的冲四点/活三点/活二点，则该点的得分会比较高
 */
public class DestructiveComboPointScoreStrategy extends AbstractComboPointScoreStrategy {

    public DestructiveComboPointScoreStrategy() {
        super(1, 7, 3);
    }

    public DestructiveComboPointScoreStrategy(int rushFourPointScore, int liveThreePointScore, int liveTwoPointScore) {
        super(rushFourPointScore, liveThreePointScore, liveTwoPointScore);
    }

    @Override
    public int getScoreOfPosition(GobangChess chess, Color me, Color opponent, Integer position) {

        // 冲四点的得分较低，因为我们已经知道对手无法VCF成功，我们下在对手的冲四点的收益很小
        return chess.getRushFourCountOf(opponent, position) * rushFourPointScore
                + chess.getLiveThreeCountOf(opponent, position) * liveThreePointScore
                + chess.getLiveTwoCountOf(opponent, position) * liveTwoPointScore;
    }
}
