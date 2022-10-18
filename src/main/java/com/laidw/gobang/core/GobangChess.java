package com.laidw.gobang.core;

import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.combo.ComboMap;
import com.laidw.gobang.core.combo.four.ConFour;
import com.laidw.gobang.core.combo.four.JumpFour;
import com.laidw.gobang.core.combo.four.LiveFour;
import com.laidw.gobang.core.combo.three.ConThree;
import com.laidw.gobang.core.combo.three.JumpThree;
import com.laidw.gobang.core.combo.three.SleepThree;
import com.laidw.gobang.core.combo.two.ConTow;
import com.laidw.gobang.core.combo.two.FarTow;
import com.laidw.gobang.core.combo.two.JumpTow;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.constant.Direction;
import com.laidw.gobang.core.util.ChessUtil;
import com.laidw.gobang.core.util.ComboUtil;
import com.laidw.gobang.core.util.XyUtil;

import java.util.*;

/**
 * 五子棋棋盘
 */
public class GobangChess {
    private static final int FIVE = 5;

    private final int dimension;
    private final int[][] matrix;
    private final LineScanner[] rowScanners;
    private final LineScanner[] colScanners;
    private final LineScanner[] negScanners;
    private final LineScanner[] posScanners;
    private final TreeSet<Combo> blackCombos = new TreeSet<>();
    private final TreeSet<Combo> whiteCombos = new TreeSet<>();
    private final ComboMap blackRushFourMap = new ComboMap();
    private final ComboMap whiteRushFourMap = new ComboMap();
    private final ComboMap blackLiveThreeMap = new ComboMap();
    private final ComboMap whiteLiveThreeMap = new ComboMap();
    private final ComboMap blackLiveTwoMap = new ComboMap();
    private final ComboMap whiteLiveTwoMap = new ComboMap();
    private final LinkedList<Integer> historyPositions = new LinkedList<>();
    private Color winner = null;

    public GobangChess(int dimension) {
        if (dimension < FIVE) {
            throw new IllegalArgumentException("棋盘大小不能小于" + FIVE + "！");
        }

        int slashCount = (dimension << 1) - 1;
        this.dimension = dimension;
        this.matrix = new int[dimension][dimension];
        this.rowScanners = new LineScanner[dimension];
        this.colScanners = new LineScanner[dimension];
        this.negScanners = new LineScanner[slashCount];
        this.posScanners = new LineScanner[slashCount];
        initScanners(dimension, slashCount);
    }

    /**
     * 在指定位置落子，并更新棋盘相关的信息；坐标必须合法，且必须指定颜色
     */
    public final void set(int position, Color color) {
        set(XyUtil.x(position), XyUtil.y(position), color);
    }

    /**
     * 在指定位置落子，并更新棋盘相关的信息；坐标必须合法，且必须指定颜色
     */
    public final void set(int x, int y, Color color) {
        if (winner != null) {
            throw new RuntimeException("游戏已结束，胜利者为：" + winner.name());
        }
        if (!isValid(x, y) || !isBlank(x, y) || color == null) {
            throw new IllegalArgumentException("未指定棋子颜色，或者不能下在此处！");
        }
        setAndRefresh(x, y, color);
        historyPositions.add(XyUtil.position(x, y));
    }

    /**
     * 回退到上一步
     */
    public final int back() {
        if (historyPositions.isEmpty()) {
            return XyUtil.NULL_POSITION;
        }
        int position = historyPositions.removeLast();
        setAndRefresh(XyUtil.x(position), XyUtil.y(position), null);
        winner = null;
        return position;
    }

    /**
     * 尝试在指定位置落子，并将该位置所属4行的该颜色棋子的Combo扫描结果返回
     */
    public final Collection<Combo> tryAndScan(int position, Color color) {
        int x = XyUtil.x(position), y = XyUtil.y(position);
        setValue(x, y, color);
        List<Combo> list = new ArrayList<>();
        for (LineScanner scanner : getScannersOf(x, y)) {
            scanner.scan(color, list);
        }
        setValue(x, y, null);
        return list;
    }

    /**
     * 获取在指定位置落子的收益（落子前后我方Combo总分之差）
     */
    public final int tryAndGetNetScore(int x, int y, Color color) {
        final List<Combo> comboBuffer = new ArrayList<>();
        int before = 0, after = 0;

        setValue(x, y, color);
        for (LineScanner scanner : getScannersOf(x, y)) {
            before += getTotalScoreOf(color == Color.BLACK ? scanner.localBlackCombos : scanner.localWhiteCombos);
            after += getTotalScoreOf(scanner.scan(color, comboBuffer));
            comboBuffer.clear();
        }
        setValue(x, y, null);

        return after - before;
    }

    /**
     * 在指定位置落子，并更新棋盘信息
     */
    private void setAndRefresh(int x, int y, Color color) {
        setValue(x, y, color);
        LineScanner[] scanners = getScannersOf(x, y);
        for (LineScanner scanner : scanners) {
            scanner.refreshChess();
        }
        if (color == null) {
            return;
        }
        for (LineScanner scanner : scanners) {
            if (scanner.isWinner(color)) {
                winner = color;
                return;
            }
        }
    }

    public final int[][] getMatrix() {
        int[][] matrix = new int[dimension][];
        for (int i = 0; i < dimension; i++) {
            matrix[i] = Arrays.copyOf(this.matrix[i], dimension);
        }
        return matrix;
    }

    public final Color getWinner() {
        return winner;
    }

    public final int getDimension() {
        return dimension;
    }

    public final List<Integer> getHistoryPositions() {
        return Collections.unmodifiableList(historyPositions);
    }

    public final Combo getFirstComboOf(Color color) {
        TreeSet<Combo> combos = color == Color.BLACK ? blackCombos : whiteCombos;
        return combos.isEmpty() ? null : combos.first();
    }

    public final NavigableSet<Combo> getCombosOf(Color color) {
        return Collections.unmodifiableNavigableSet(new TreeSet<>(color == Color.BLACK ? blackCombos : whiteCombos));
    }

    public final ComboMap getRushFourMapOf(Color color) {
        return (color == Color.BLACK ? blackRushFourMap : whiteRushFourMap).unmodified();
    }

    public final ComboMap getLiveThreeMapOf(Color color) {
        return (color == Color.BLACK ? blackLiveThreeMap : whiteLiveThreeMap).unmodified();
    }

    public final ComboMap getLiveTwoMapOf(Color color) {
        return (color == Color.BLACK ? blackLiveTwoMap : whiteLiveTwoMap).unmodified();
    }
    public final int getRushFourCountOf(Color color, Integer position) {
        return (color == Color.BLACK ? blackRushFourMap : whiteRushFourMap).getOrEmpty(position).size();
    }

    public final int getLiveThreeCountOf(Color color, Integer position) {
        return (color == Color.BLACK ? blackLiveThreeMap : whiteLiveThreeMap).getOrEmpty(position).size();
    }

    public final int getLiveTwoCountOf(Color color, Integer position) {
        return (color == Color.BLACK ? blackLiveTwoMap : whiteLiveTwoMap).getOrEmpty(position).size();
    }


    public final boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < dimension && y < dimension;
    }

    public final boolean isBlank(int x, int y) {
        return matrix[x][y] == 0;
    }

    public final boolean isEqual(int x, int y, Color color) {
        return matrix[x][y] == color.value;
    }

    private boolean isValid(int[] point) {
        int x = point[0], y = point[1];
        return isValid(x, y);
    }

    private boolean isBlank(int[] point) {
        int x = point[0], y = point[1];
        return isBlank(x, y);
    }

    private boolean isEqual(int[] point, Color color) {
        int x = point[0], y = point[1];
        return isEqual(x, y, color);
    }

    private boolean isValidAndBlank(int[] point) {
        int x = point[0], y = point[1];
        return isValid(x, y) && isBlank(x, y);
    }

    private boolean isValidAndEqual(int[] point, Color color) {
        int x = point[0], y = point[1];
        return isValid(x, y) && isEqual(x, y, color);
    }

    private void setValue(int x, int y, Color color) {
        matrix[x][y] = color == null ? 0 : color.value;
    }

    @Override
    public final String toString() {
        return ChessUtil.toString(this, matrix);
    }

    private static int getTotalScoreOf(Collection<Combo> combos) {
        int total = 0;
        for (Combo combo : combos) {
            total += combo.score();
        }
        return total;
    }


    //
    // LineScanner相关
    //

    private class LineScanner {
        private final int beginX, beginY;
        private final Direction direction;
        private final List<Combo> localBlackCombos = new ArrayList<>();
        private final List<Combo> localWhiteCombos = new ArrayList<>();

        public LineScanner(int beginX, int beginY, Direction direction) {
            this.beginX = beginX;
            this.beginY = beginY;
            this.direction = direction;
        }

        public void refreshChess() {
            clearCombos();
            blackCombos.addAll(scan(Color.BLACK, localBlackCombos));
            whiteCombos.addAll(scan(Color.WHITE, localWhiteCombos));
            refreshComboPoints(blackRushFourMap, blackLiveThreeMap, blackLiveTwoMap, Color.BLACK);
            refreshComboPoints(whiteRushFourMap, whiteLiveThreeMap, whiteLiveTwoMap, Color.WHITE);
        }

        public Collection<Combo> scan(Color color, Collection<Combo> combos) {
            scan(color, beginX, beginY, combos);
            return combos;
        }

        public boolean isWinner(Color color) {
            return hasContinuous5(color, new int[] {beginX, beginY});
        }

        private boolean hasContinuous5(Color color, int[] begin) {
            while (isValid(begin) && !isEqual(begin, color)) {
                direction.step(begin);
            }

            if (!isValid(begin)) {
                return false;
            }

            int count = 1;
            int[] end = direction.newStep(begin);
            while (isValidAndEqual(end, color)) {
                direction.step(end);
                count++;
            }

            return count >= 5 || hasContinuous5(color, end);
        }

        private void clearCombos() {
            localBlackCombos.forEach(blackCombos::remove);
            localWhiteCombos.forEach(whiteCombos::remove);
            localBlackCombos.clear();
            localWhiteCombos.clear();
        }

        private void refreshComboPoints(ComboMap rushFourMap, ComboMap liveThreeMap, ComboMap liveTwoMap, Color color) {
            int x = beginX, y = beginY;
            do {
                Integer position = XyUtil.position(x, y);
                rushFourMap.remove(position);
                liveThreeMap.remove(position);
                liveTwoMap.remove(position);
                if (isBlank(x, y)) {
                    Collection<Combo> combos = tryAndScan(position, color);
                    rushFourMap.putIfNotEmpty(position, ComboUtil.getRushFoursOf(position, combos));
                    liveThreeMap.putIfNotEmpty(position, ComboUtil.getLiveThreesOf(position, combos));
                    liveTwoMap.putIfNotEmpty(position, ComboUtil.getLiveTwosOf(position, combos));
                }
                x += direction.dx;
                y += direction.dy;
            } while (isValid(x, y));
        }

        private int scan(Color color, int beginX, int beginY, Collection<Combo> combos) {
            int[] begin = {beginX, beginY};

            // 将begin移到第一个color棋子上
            while (isValid(begin) && !isEqual(begin, color)) {
                direction.step(begin);
            }

            // 没有color棋子，则直接返回0
            if (!isValid(begin)) {
                return 0;
            }

            // 统计连续出现的color棋子数量；end最终指向最后一个color棋子的后一格
            int count = 1;
            int[] end = direction.newStep(begin);
            while (isValidAndEqual(end, color)) {
                direction.step(end);
                count++;
            }

            // hat是begin的前面第二格，head是begin的前一格，tail是end的后一格
            int[] hat = direction.newStep(begin, -2);
            int[] head = direction.newStep(begin, -1);
            int[] tail = direction.newStep(end);

            // 从tail开始继续往后查找，顺便看看接下来的那一排有几个连续的color棋子
            int nextCount = scan(color, tail[0], tail[1], combos);

            // 如果end不合法或者是异色棋子，说明无法向后发展
            if (!isValid(end) || isEqual(end, color.reversed())) {
                return scanOnRightBlock(hat, head, begin, end, tail, color, count, combos);
            }

            // 否则，说明end是一个空格

            // 如果空格后面一个格子不合法或是异色棋子
            if (!isValid(tail) || isEqual(tail, color.reversed())) {
                return scanOnRightOneBlank(hat, head, begin, end, tail, color, count, combos);
            }

            // 否则，说明空格后面一个格子是空格或同色棋子

            // 如果空格后面一个格子是空格
            if (isBlank(tail)) {
                return scanOnRightTwoBlank(hat, head, begin, end, tail, color, count, combos);
            }

            // 否则，说明当前排与下一排只间隔了一个空格

            // 冲四
            if (count + nextCount >= 4) {
                combos.add(new JumpFour(begin, direction, end));
                return count;
            }

            // 如果两数之和为2或3，则是?OO_O?/?O_OO?/?O_O?的情况
            int[] left = head;
            int[] right = direction.newStep(tail, nextCount);
            boolean leftIsBlank = isValidAndBlank(left);
            boolean rightIsBlank = isValidAndBlank(right);
            boolean beforeLeftIsBlank = isValidAndBlank(direction.newStep(left, -1));
            boolean afterRightIsBlank = isValidAndBlank(direction.newStep(right));

            // ?OO_O?/?O_OO?
            if (count + nextCount >= 3) {

                // 如果左右两个?都是空格（_OO_O_/_O_OO_），则是跳活三
                if (leftIsBlank && rightIsBlank) {
                    combos.add(new JumpThree(begin, direction, end)
                            .addNextPosition(left)
                            .addNextPosition(right));

                // 只有一个?是空格（_OO_OX/XO_OO_），则是眠三
                } else if (leftIsBlank || rightIsBlank) {
                    combos.add(new SleepThree(begin, direction, end)
                            .addNextPosition(leftIsBlank ? left : right));
                }

            // ?O_O?
            } else {

                // 左右两个?必须是空格（_O_O_），并且左/右还得再有一个空格，才是活二
                if (leftIsBlank && rightIsBlank && (beforeLeftIsBlank || afterRightIsBlank)) {
                    combos.add(new JumpTow(begin, direction, end)
                            .addNextPositionIf(beforeLeftIsBlank, left)
                            .addNextPositionIf(afterRightIsBlank, right));
                }
            }

            return count;
        }

        /**
         * 已知end不合法或者是异色棋子，即无法向后发展
         */
        private int scanOnRightBlock(int[] hat, int[] head, int[] begin, int[] end, int[] tail, Color color, int count, Collection<Combo> combos) {

            // 如果无法向前发展，则直接返回
            if (!isValidAndBlank(head)) {
                return count;
            }

            // 否则，说明begin前面是一个空格
            // 1. 如果此时有4个连子，则找到一个冲四（_OOOOX）
            // 2. 如果此时有3个连子，且再前面也是空格，则找到一个眠三（__OOOX）
            if (count >= 4) {
                combos.add(new ConFour(begin, direction, head));
            } else if (count >= 3 && isValidAndBlank(hat)) {
                combos.add(new SleepThree(begin, direction, head)
                        .addNextPosition(hat));
            }
            return count;
        }

        /**
         * 已知end是空格，且tail不合法或是异色棋子
         */
        private int scanOnRightOneBlank(int[] hat, int[] head, int[] begin, int[] end, int[] tail, Color color, int count, Collection<Combo> combos) {

            // 如果无法向前发展，则顶多只有一个冲四（XOOOO_X）
            if (!isValidAndBlank(head)) {
                if (count >= 4) {
                    combos.add(new ConFour(begin, direction, end));
                }
                return count;
            }

            // 否则，说明左右都有一个空格（且左边可能还有更多空格）
            // 1. 如果有4个连子，则找到了活四（_OOOO_X）
            // 2. 如果有3个连子，则可能是活三（__OOO_X）或眠三（X_OOO_X）
            // 3. 如果有2个连子，且左边至少有3个空格，则找到了活二（___OO_X）
            if (count >= 4) {
                combos.add(new LiveFour(begin, direction, end)
                        .addNextPosition(head));
            } else if (count >= 3) {
                if (isValidAndBlank(hat)) {
                    combos.add(new ConThree(begin, direction, head)
                            .addNextPosition(end).addNextPosition(hat));
                } else {
                    combos.add(new SleepThree(begin, direction, head)
                            .addNextPosition(end));
                }
            } else if (count >= 2 && isValidAndBlank(hat) && isValidAndBlank(direction.newStep(hat, -1))) {
                combos.add(new ConTow(begin, direction, head)
                        .addNextPosition(hat));
            }
            return count;
        }

        /**
         * 已知end和tail都是空格，而tail后面的位置还不确定
         */
        private int scanOnRightTwoBlank(int[] hat, int[] head, int[] begin, int[] end, int[] tail, Color color, int count, Collection<Combo> combos) {

            // 在无法向前发展的情况下：
            // 1. 如果有4个连子，则找到了冲四（X0000__）
            // 2. 如果有3个连子，则找到了眠三（X000__）
            if (!isValidAndBlank(head)) {
                if (count >= 4) {
                    combos.add(new ConFour(begin, direction, end));
                } else if (count >= 3) {
                    combos.add(new SleepThree(begin, direction, end)
                            .addNextPosition(tail));
                }
                return count;
            }

            // 否则，说明左边至少有一个空格，右边至少有两个空格

            // 如果有4个连子，则是活四（_OOOO__）
            if (count >= 4) {
                combos.add(new LiveFour(begin, direction, head)
                        .addNextPosition(end));
                return count;
            }

            // 如果有3个连子，则是连活三（_OOO__）
            if (count >= 3) {
                combos.add(new ConThree(begin, direction, end)
                        .addNextPosition(head)
                        .addNextPositionIf(!isValidAndBlank(hat), tail));
                return count;
            }

            // 如果只有1个连子（_O__）：
            // 1. 如果两个空格后面是两个color棋子（_O__OO），则是眠三
            // 2. 如果两个空格后面是一个color棋子和一个空格（_O__O_），则是活二
            // 3. 否则直接忽略
            if (count <= 1) {
                int[] last = direction.newStep(end, 3);
                if (!isValidAndEqual(direction.newStep(end, 2), color) || !isValid(last) || isEqual(last, color.reversed())) {
                    return count;
                }
                if (isBlank(last)) {
                    combos.add(new FarTow(begin, direction, end)
                            .addNextPosition(tail));
                } else {
                    combos.add(new SleepThree(begin, direction, end)
                            .addNextPosition(tail));
                }
                return count;
            }

            // 否则，说明有2个连子（_OO__），这时候需要查看更前和更后格子的情况（??_OO__?）
            boolean hatIsBlank = isValidAndBlank(hat);
            boolean afterTailIsBlank = isValidAndBlank(direction.newStep(tail));

            // 如果第二或第三个?是空格（?__OO__?/??_OO___），则是活二
            if (hatIsBlank || afterTailIsBlank) {
                combos.add(new ConTow(begin, direction, end)
                        .addNextPositionIf(afterTailIsBlank, tail)
                        .addNextPositionIf(hatIsBlank, head)
                        .addNextPositionIf(hatIsBlank && isValidAndBlank(direction.newStep(hat, -1)), hat));
            }
            return count;
        }
    }

    private LineScanner[] getScannersOf(int x, int y) {
        int negIdx = negIdx(x, y);
        int posIdx = posIdx(x, y);
        return new LineScanner[]{rowScanners[x], colScanners[y], negScanners[negIdx], posScanners[posIdx]};
    }

    private int negIdx(int x, int y) {
        return dimension - x + y - 1;
    }

    private int posIdx(int x, int y) {
        return x + y;
    }

    private void initScanners(int dimension, int slashCount) {
        for (int i = 0; i < dimension; i++) {
            this.rowScanners[i] = new LineScanner(i, 0, Direction.TRANSVERSE);
            this.colScanners[i] = new LineScanner(0, i, Direction.VERTICAL);
        }

        int x = 14, y = 1;
        for (int i = 0; i < slashCount; i++) {
            if (x >= 0) {
                negScanners[i] = new LineScanner(x--, 0, Direction.NEG_SLASH);
            } else {
                negScanners[i] = new LineScanner(0, y++, Direction.NEG_SLASH);
            }
        }

        x = 1; y = 0;
        for (int i = 0; i < slashCount; i++) {
            if (y < dimension) {
                posScanners[i] = new LineScanner(0, y++, Direction.POS_SLASH);
            } else {
                posScanners[i] = new LineScanner(x++, 14, Direction.POS_SLASH);
            }
        }

        // 前/后4个斜线是不需要扫描的，因此将其替换成emptyScanner；当然不替换也没关系

        LineScanner emptyScanner = new LineScanner(-1, -1, null) {
            @Override public void refreshChess() { }
            @Override public boolean isWinner(Color color) { return false; }
            @Override public Collection<Combo> scan(Color color, Collection<Combo> combos) { return Collections.emptyList(); }
        };

        for (int i = 0; i < FIVE; i++) {
            posScanners[i] = emptyScanner;
            negScanners[i] = emptyScanner;
            posScanners[slashCount - i - 1] = emptyScanner;
            negScanners[slashCount - i - 1] = emptyScanner;
        }
    }
}