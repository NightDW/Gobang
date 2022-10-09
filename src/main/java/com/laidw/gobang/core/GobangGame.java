package com.laidw.gobang.core;

import com.laidw.gobang.core.util.XyUtil;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.player.GobangPlayer;

/**
 * 五子棋游戏；包括棋盘和玩家
 */
public class GobangGame {
    private final int dimension;
    private final Class<? extends GobangChess> chessType;
    private final Class<? extends GobangPlayer> blackPlayerType;
    private final Class<? extends GobangPlayer> whitePlayerType;

    public GobangGame(int dimension, Class<? extends GobangChess> chessType, Class<? extends GobangPlayer> blackPlayerType, Class<? extends GobangPlayer> whitePlayerType) {
        this.dimension = dimension;
        this.chessType = chessType;
        this.blackPlayerType = blackPlayerType;
        this.whitePlayerType = whitePlayerType;
        reset();
    }

    private GobangChess chess;
    private GobangPlayer[] players;
    private int last;
    private int turn;

    public void reset() {
        this.chess = newChess(chessType, dimension);
        this.players = new GobangPlayer[] {newPlayer(blackPlayerType, Color.BLACK, chess), newPlayer(whitePlayerType, Color.WHITE, chess)};
        this.last = XyUtil.NULL_POSITION;
        this.turn = 0;
    }

    public void next(int userPosition) {
        last = players[turn % 2].play(last, userPosition);
        turn++;
    }

    public void back() {
        last = chess.back();
        turn--;
    }

    public Color getWinner() {
        return chess.getWinner();
    }

    public int getTurn() {
        return turn;
    }

    public GobangChess getChess() {
        return chess;
    }

    private static GobangPlayer newPlayer(Class<? extends GobangPlayer> playerType, Color color, GobangChess chess)  {
        try {
            return playerType.getConstructor(Color.class, GobangChess.class).newInstance(color, chess);
        } catch (Exception e) {
            throw new RuntimeException("实例化玩家对象失败！");
        }
    }

    private static GobangChess newChess(Class<? extends GobangChess> chessType, int dimension) {
        try {
            return chessType.getConstructor(int.class).newInstance(dimension);
        } catch (Exception e) {
            throw new RuntimeException("实例化棋盘对象失败！");
        }
    }
}
