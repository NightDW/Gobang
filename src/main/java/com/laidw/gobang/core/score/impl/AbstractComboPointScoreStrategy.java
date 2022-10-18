package com.laidw.gobang.core.score.impl;

import com.laidw.gobang.core.score.ScoreStrategy;

public abstract class AbstractComboPointScoreStrategy implements ScoreStrategy {
    protected final int rushFourPointScore;
    protected final int liveThreePointScore;
    protected final int liveTwoPointScore;

    protected AbstractComboPointScoreStrategy(int rushFourPointScore, int liveThreePointScore, int liveTwoPointScore) {
        this.rushFourPointScore = rushFourPointScore;
        this.liveThreePointScore = liveThreePointScore;
        this.liveTwoPointScore = liveTwoPointScore;
    }
}
