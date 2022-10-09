package com.laidw.gobang.core.player;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.combo.four.RushFour;
import com.laidw.gobang.core.combo.three.LiveThree;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.util.ComboUtil;
import com.laidw.gobang.core.util.VcfUtil;
import com.laidw.gobang.core.util.VctUtil;
import com.laidw.gobang.core.util.XyUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 比较智能地决定下棋的位置
 */
public class IntelligentPlayer extends GobangPlayer {

    public IntelligentPlayer(Color color, GobangChess chess) {
        super(color, chess);
    }

    @Override
    protected int deducePosition(int opponentPosition) {
        int dimension = chess.getDimension();

        // 当前双方的落子总数
        List<Integer> historyPositions = chess.getHistoryPositions();
        int size = historyPositions.size();

        // 如果对局刚开始
        if (size <= 1) {
            int halfDimension = dimension >> 1;
            int centerPosition = XyUtil.position(halfDimension, halfDimension);

            // 如果我方是黑方，或者对手黑方没有落在中间位置，则落在中间位置
            if (size == 0 || historyPositions.get(0) != centerPosition) {
                return centerPosition;
            }

            // 否则，落在中间位置的右下角（左上角等也行）
            return XyUtil.position(halfDimension + 1, halfDimension + 1);
        }

        Combo myFirst = chess.getFirstComboOf(me);
        Combo opponentFirst = chess.getFirstComboOf(opponent);

        // 如果我方有冲四（包括活四），则直接构造五子取胜
        if (myFirst instanceof RushFour) {
            return myFirst.getNextPositions().get(0);
        }

        // 如果对方有冲四（包括活四），则只能去堵
        if (opponentFirst instanceof RushFour) {
            return findBestFrom(chess, me, opponentFirst.getNextPositions());
        }

        // 如果我方有活三，则构造活四
        if (myFirst instanceof LiveThree) {
            return myFirst.getNextPositions().get(0);
        }

        // 寻找我方的44点，如果能找到，则返回该点
        Integer position = searchFor44(chess, me);
        if (position != null) {
            return position;
        }

        // 尝试进行VCF
        LinkedList<Integer> path = new LinkedList<>();
        if (VcfUtil.tryVcf(chess, me, opponent, path)) {
            position = path.removeFirst();
            return position;
        }

        // 如果对方有活三，则堵活三
        if (opponentFirst instanceof LiveThree) {
            return findBestFrom(chess, me, opponentFirst.getNextPositions());
        }

        // 如果对方有44点，则堵住该点
        position = searchFor44(chess, opponent);
        if (position != null) {
            return position;
        }

        // 判断对方是否能VCF，如果能，则尝试破坏其VCF
        path.clear();
        if (VcfUtil.tryVcf(chess, opponent, me, path)) {
            position = tryDestroyOpponentVcf(chess, me, path);
            if (position != null) {
                return position;
            }
            return findBestFrom(chess, me, null);
        }

        // 如果能形成双活三，则构造双活三
        position = searchFor33(chess, me);
        if (position != null) {
            return position;
        }

        // 尝试VCT
        if ((position = VctUtil.tryVct(chess, me, opponent)) != null) {
            return position;
        }

        // 再尝试模拟方式的VCT
        if ((position = VcfUtil.tryVct(chess, me, opponent)) != null) {
            return position;
        }

        // 堵住对手的双活三点
        position = searchFor33(chess, opponent);
        if (position != null) {
            return position;
        }

        // 防止对手VCT
        if ((position = VctUtil.tryVct(chess, opponent, me)) != null) {
            return position;
        }

        // 防止对手VCT
        if ((position = VcfUtil.tryVct(chess, opponent, me)) != null) {
            return position;
        }

        return findBestFrom(chess, me, null);
    }

    /**
     * 从指定的点中找到最好的点来落子；如果positions为null，则从所有空格中找到最好的点
     */
    private static int findBestFrom(GobangChess chess, Color me, List<Integer> positions) {
        if (positions == null) {
            int dimension = chess.getDimension();
            positions = new ArrayList<>(dimension * dimension);
            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    if (chess.isBlank(x, y)) {
                        positions.add(XyUtil.position(x, y));
                    }
                }
            }
        }

        if (positions.size() == 1) {
            return positions.get(0);
        }

        int maxScore = Integer.MIN_VALUE;
        Integer maxPosition = positions.get(0);
        for (Integer position : positions) {
            int score = chess.tryAndGetNetScore(XyUtil.x(position), XyUtil.y(position), me);
            if (score > maxScore) {
                maxScore = score;
                maxPosition = position;
            }
        }
        return maxPosition;
    }

    /**
     * 遍历所有的冲四点，看看哪个点能同时形成两个冲四
     */
    private static Integer searchFor44(GobangChess chess, Color color) {
        for (Map.Entry<Integer, List<Combo>> entry : chess.getRushFourMapOf(color).entrySet()) {
            if (ComboUtil.isMultiRushFours(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 遍历所有的活三点，看看哪个点能同时形成两个活三
     */
    private static Integer searchFor33(GobangChess chess, Color color) {
        for (Map.Entry<Integer, List<Combo>> entry : chess.getLiveThreeMapOf(color).entrySet()) {
            if (entry.getValue().size() >= 2) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 已知对手目前形成了VCF路径opponentPath，这里我们需要占据该路径中的点，避免对手VCF成功
     * 这里可能会有多种方案，本方法会返回其中的最佳方案；注意path中有敌我双方的落子位置
     */
    private static Integer tryDestroyOpponentVcf(GobangChess chess, Color me, LinkedList<Integer> opponentPath) {
        List<Integer> candidates = new ArrayList<>(opponentPath.size());
        LinkedList<Integer> temPath = new LinkedList<>();
        while (!opponentPath.isEmpty()) {

            // 抢先把路径中的点占据；如果占据之后对手无法VCF，则将该点保存下来
            Integer toOccupy = opponentPath.removeFirst();
            chess.set(toOccupy, me);
            if (!VcfUtil.tryVcf(chess, me.reversed(), me, temPath)) {
                candidates.add(toOccupy);
            }

            // 回溯
            chess.back();
        }

        return candidates.isEmpty() ? null : findBestFrom(chess, me, candidates);
    }
}