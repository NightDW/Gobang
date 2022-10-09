package com.laidw.gobang.core.player;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.util.XyUtil;

/**
 * 五子棋玩家
 */
public abstract class GobangPlayer {
    protected final Color me, opponent;
    protected final GobangChess chess;

    protected GobangPlayer(Color color, GobangChess chess) {
        this.me = color;
        this.opponent = color.reversed();
        this.chess = chess;
    }

    /**
     * 下棋；会判断应该将自身颜色的棋子下在哪个地方，更新棋盘后返回刚下的位置
     *
     * @param opponentPosition 对手刚下的位置
     * @param userPosition     用户指定的位置
     * @return                 最终选中的位置
     */
    public final int play(int opponentPosition, int userPosition) {

        // 如果用户指定了下棋位置，则下在此处；否则自动推测一个合适的位置
        int position = userPosition == XyUtil.NULL_POSITION ? deducePosition(opponentPosition) : userPosition;

        chess.set(position, me);
        return position;
    }

    protected abstract int deducePosition(int opponentPosition);
}
