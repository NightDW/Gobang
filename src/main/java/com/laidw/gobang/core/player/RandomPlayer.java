package com.laidw.gobang.core.player;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.util.XyUtil;

import java.util.Random;

/**
 * 随机决定下棋的位置
 */
public class RandomPlayer extends GobangPlayer {
    private final Random random = new Random();

    public RandomPlayer(Color color, GobangChess chess) {
        super(color, chess);
    }

    @Override
    protected int deducePosition(int opponentPosition) {
        int dimension = chess.getDimension();
        while (true) {
            int x = random.nextInt(dimension);
            int y = random.nextInt(dimension);
            if (chess.isBlank(x, y)) {
                return XyUtil.position(x, y);
            }
        }
    }
}
