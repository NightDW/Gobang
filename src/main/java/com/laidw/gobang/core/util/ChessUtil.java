package com.laidw.gobang.core.util;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Color;

import java.util.Collection;
import java.util.NavigableSet;

/**
 * 负责将棋盘转成字符串
 */
public abstract class ChessUtil {
    private static final char[] NUMS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] CHESS_PIECES = new char[3];
    static {
        CHESS_PIECES[0] = '_';
        CHESS_PIECES[Color.BLACK.value] = 'X';
        CHESS_PIECES[Color.WHITE.value] = 'O';
    }

    private ChessUtil() {}

    public static String toString(GobangChess chess, int[][] chessMatrix) {
        StringBuilder sb = new StringBuilder();
        int dimension = chess.getDimension();

        appendChessHead(sb, dimension);
        for (int i = 0; i < dimension; i++) {
            appendChessRow(sb, chessMatrix[i], i);
        }

        sb.append("\nblack combos: \n");
        appendCombos(sb, chess.getCombosOf(Color.BLACK));
        sb.append("\nblack rush fours: \n");
        appendPositionAsString(sb, chess.getRushFourMapOf(Color.BLACK).keySet(), "\n");

        sb.append("\nwhite combos: \n");
        appendCombos(sb, chess.getCombosOf(Color.WHITE));
        sb.append("\nwhite rush fours: \n");
        appendPositionAsString(sb, chess.getRushFourMapOf(Color.WHITE).keySet(), "\n");

        return sb.toString();
    }

    private static void appendCombos(StringBuilder sb, NavigableSet<Combo> combos) {
        for (Combo combo : combos) {
            sb.append(combo.getClass().getSimpleName()).append(' ');
            appendPositionAsString(sb, combo.getPosition(), " ");
            sb.append(combo.getDirection().name()).append(" ");
            appendPositionAsString(sb, combo.getNextPositions(), "\n");
        }
    }

    private static void appendChessHead(StringBuilder sb, int dimension) {
        sb.append("    ");
        for (int i = 0; i < dimension; i++) {
            sb.append(NUMS[i]).append(' ');
        }
        sb.append('\n');
    }

    private static void appendChessRow(StringBuilder sb, int[] chessRow, int rowNum) {
        sb.append(NUMS[rowNum]).append(" [ ");
        for (int color : chessRow) {
            char chessPiece = CHESS_PIECES[color];
            sb.append(chessPiece).append(' ');
        }
        sb.append("]\n");
    }

    private static void appendPositionAsString(StringBuilder sb, int position, String suffix) {
        sb.append('(').append(XyUtil.x(position)).append(", ").append(XyUtil.y(position)).append(')').append(suffix);
    }

    private static void appendPositionAsString(StringBuilder sb, Collection<Integer> positions, String suffix) {
        if (positions.isEmpty()) {
            sb.append("[]").append(suffix);
            return;
        }
        sb.append('[');
        for (int position : positions) {
            appendPositionAsString(sb, position, ", ");
        }
        sb.setCharAt(sb.length() - 2, ']');
        sb.append(suffix);
    }
}
