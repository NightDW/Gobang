package com.laidw.gobang.core.score.impl;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.score.ScoreStrategy;
import com.laidw.gobang.core.util.VctUtil;

/**
 * 根据落子后我方有几个冲四点/活三点/活二点来打分
 */
public class ComboPairScoreStrategy implements ScoreStrategy {
    public static final int CP_44 = Integer.MAX_VALUE;
    public static final int CP_43 = Integer.MAX_VALUE - 1;
    public static final int CP_33 = Integer.MAX_VALUE - 2;
    public static final int CP_42 = Integer.MAX_VALUE - 3;
    public static final int CP_32 = Integer.MAX_VALUE - 4;
    public static final int CP_22 = Integer.MAX_VALUE - 5;
    public static final int LIVE_THREE = 10;
    public static final int LIVE_TWO = 5;

    @Override
    public int getScoreOfPosition(GobangChess chess, Color me, Color opponent, Integer position) {
        int rushFourCount = chess.getRushFourCountOf(me, position);
        int liveThreeCount = chess.getLiveThreeCountOf(me, position);
        int liveTwoCount = chess.getLiveTwoCountOf(me, position);

        // 双冲四
        if (rushFourCount >= 2) {
            return CP_44;
        }

        // 否则，顶多只有一个冲四；此时如果没有活三/活二，则直接返回0，因为我们已经知道VCF不可能成功，冲四也没用
        if (liveThreeCount == 0 && liveTwoCount == 0) {
            return 0;
        }

        // 否则，说明此时至少有一个活三/活二

        // 如果此时有一个冲四，说明有可能43或42
        if (rushFourCount == 1) {
            Combo rushFour = chess.getRushFourMapOf(me).getOrEmpty(position).get(0);
            boolean pass = VctUtil.checkRevert(rushFour, liveThreeCount > 0, chess.getRushFourMapOf(opponent).keySet(), chess.getLiveThreeMapOf(opponent).keySet());
            return !pass ? 0 : liveThreeCount > 0 ? CP_43 : CP_42;
        }

        // 否则，说明此时没有冲四

        // 如果有两个活三，说明有可能形成双活三
        if (liveThreeCount >= 2) {
            boolean pass = true;
            for (Combo liveThree : chess.getLiveThreeMapOf(me).getOrEmpty(position)) {
                if (!VctUtil.checkRevert(liveThree, true, chess.getRushFourMapOf(opponent).keySet(), chess.getLiveThreeMapOf(opponent).keySet())) {
                    pass = false;
                    break;
                }
            }
            return !pass ? 0 : CP_33;
        }

        // 如果有一个活三
        if (liveThreeCount == 1) {
            Combo liveThree = chess.getLiveThreeMapOf(me).getOrEmpty(position).get(0);
            boolean pass = VctUtil.checkRevert(liveThree, false, chess.getRushFourMapOf(opponent).keySet(), chess.getLiveThreeMapOf(opponent).keySet());
            return !pass ? 0 :liveTwoCount > 0 ? CP_32 : LIVE_THREE;
        }

        // 否则，说明顶多只有活二

        // 如果没有活二，则直接返回0
        if (liveTwoCount == 0) {
            return 0;
        }

        boolean pass = true;
        for (Combo liveTwo : chess.getLiveTwoMapOf(me).getOrEmpty(position)) {
            if (!VctUtil.checkRevert(liveTwo, false, chess.getRushFourMapOf(opponent).keySet(), chess.getLiveThreeMapOf(opponent).keySet())) {
                pass = false;
                break;
            }
        }

        return !pass ? 0 : liveTwoCount >= 2 ? CP_22 : LIVE_TWO;
    }
}
