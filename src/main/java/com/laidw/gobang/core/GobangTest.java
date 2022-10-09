package com.laidw.gobang.core;

import com.laidw.gobang.core.player.IntelligentPlayer;
import com.laidw.gobang.core.util.XyUtil;
import com.laidw.gobang.core.player.RandomPlayer;

/**
 * 测试
 */
public class GobangTest {
    public static void main(String[] args) {
        GobangGame game = new GobangGame(15, GobangChess.class, IntelligentPlayer.class, RandomPlayer.class);
        for (int i = 0; i < 3000; i++) {
            long start = System.currentTimeMillis();
            while (game.getWinner() == null) {
                game.next(XyUtil.NULL_POSITION);
            }
            long end = System.currentTimeMillis();
            System.out.println(game.getWinner() + "；平均每轮耗时：" + (end - start) / game.getTurn());
            game.reset();
        }
    }
}
