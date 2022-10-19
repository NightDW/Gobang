package com.laidw.gobang.core.util;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.combo.four.LiveFour;
import com.laidw.gobang.core.combo.four.RushFour;
import com.laidw.gobang.core.combo.three.LiveThree;
import com.laidw.gobang.core.constant.Color;

import java.util.*;

/**
 * 连续冲四（VCF）和连续冲三（VCT）的实现逻辑
 */
public abstract class VcfUtil {
    private static final int ADDITION_LIVE_THREE_COUNT = 0;
    private static final int MAX_VCF_TIMES = 33333;
    private static int VCF_TIMES = 0;

    private VcfUtil() {}

    public static boolean tryVcf(GobangChess chess, Color me, Color opponent, LinkedList<Integer> path) {
        VCF_TIMES = 0;
        boolean result = tryVcfInternal(chess, me, opponent, path);
        VCF_TIMES = 0;
        return result;
    }

    /**
     * 尝试进行VCF，枚举出所有冲四点并不断冲四，看看能不能形成我方获胜的局面
     *
     * 返回值代表是否VCF成功；如果成功，则path中第一个元素就是当前应该落子的位置
     */
    private static boolean tryVcfInternal(GobangChess chess, Color me, Color opponent, LinkedList<Integer> path) {
        if (VCF_TIMES++ >= MAX_VCF_TIMES) {
            return false;
        }

        // 我方有冲四，说明VCF成功
        Combo myFirst = chess.getFirstComboOf(me);
        if (myFirst instanceof RushFour) {
            return true;
        }

        // 否则，如果对方有活四或双冲四，则VCF失败
        if (hasLiveFourOrMultiRushFour(chess, opponent)) {
            return false;
        }
        
        // 否则，如果对手有一个冲四（即出现了反四），则堵住该冲四
        Combo opponentFirst = chess.getFirstComboOf(opponent);
        if (opponentFirst instanceof RushFour) {
            return tryVcfOnOpponentOneRushFour(chess, me, opponent, path, opponentFirst);
        }

        // 否则，如果我方有活三，则VCF成功
        if (myFirst instanceof LiveThree) {
            return true;
        }

        // 遍历我方所有的冲四点，如果有一个点能形成双冲四，则VCF成功
        Set<Map.Entry<Integer, List<Combo>>> rushFourEntries = chess.getRushFourMapOf(me).entrySet();
        for (Map.Entry<Integer, List<Combo>> rushFourEntry : rushFourEntries) {
            if (ComboUtil.isMultiRushFours(rushFourEntry.getValue())) {
                return true;
            }
        }

        // 否则，说明局势不明朗，此时开始尝试VCF：枚举所有的冲四点，依次判断这些冲四点可不可行
        for (Map.Entry<Integer, List<Combo>> rushFourEntry : rushFourEntries) {
            Integer rushFourPosition = rushFourEntry.getKey();

            // 我方下在rushFourPosition，则对方必须下在opponentPosition（冲四的防点）上
            Integer opponentPosition = rushFourEntry.getValue().get(0).getNextPositions().get(0);

            // 记录双方的落子位置，并更新棋盘
            path.addLast(rushFourPosition);
            path.addLast(opponentPosition);
            chess.set(rushFourPosition, me);
            chess.set(opponentPosition, opponent);

            // 递归调用；如果被调用方返回true，说明该路线可行，因此直接返回true
            // 注意返回前需要先撤回之前的操作，而path不需要删除元素
            if (tryVcfInternal(chess, me, opponent, path)) {
                chess.back();
                chess.back();
                return true;
            }

            // 回溯
            chess.back();
            chess.back();
            path.removeLast();
            path.removeLast();
        }

        // 所有冲四点都枚举过了，且没有一个能VCF成功，因此返回false
        return false;
    }

    /**
     * 如果我方在VCF时被对方反四了，则调用该方法判断我方能否VCF成功
     *
     * 注意，此时对手肯定只有opponentRushFour这唯一一个冲四
     */
    private static boolean tryVcfOnOpponentOneRushFour(GobangChess chess, Color me, Color opponent, LinkedList<Integer> path, Combo opponentRushFour) {

        // 先占据对手冲四的防点
        Integer myPosition = opponentRushFour.getNextPositions().get(0);
        path.addLast(myPosition);
        chess.set(myPosition, me);

        // 如果堵住对方的冲四后，我方有活四或双冲四，则VCF成功
        if (hasLiveFourOrMultiRushFour(chess, me)) {
            chess.back();
            return true;
        }

        // 如果我方只有一个冲四，则对方也必须挡我方的冲四
        Combo myFirst = chess.getFirstComboOf(me);
        if (myFirst instanceof RushFour) {
            Integer opponentPosition = myFirst.getNextPositions().get(0);
            path.addLast(opponentPosition);
            chess.set(opponentPosition, opponent);

            // 如果接下来能VCF成功，则本次VCF也是成功的；否则失败
            if (tryVcfInternal(chess, me, opponent, path)) {
                chess.back();
                chess.back();
                return true;
            }
            chess.back();
            chess.back();
            path.removeLast();
            path.removeLast();
            return false;
        }

        // 否则，说明我方的冲四被对方反四了，且我方无法反回来，因此VCF失败
        chess.back();
        path.removeLast();
        return false;
    }

    /**
     * 已知双方都tryVcf失败，并且此时轮到我方
     * 那么此时如果我方形成活三的话，对方也是必须回应的，此时对方的行动也是可预测的了
     *
     * 因此，如果我们能构建出一个活三，并且不管对手防住活三的哪个位置，我们都可以VCF，那么我们还是能够获胜
     * 需要注意的是，在形成活三时，对方会有多个防守点，也可以直接冲四打乱我方节奏
     *
     * 返回null代表VCT失败，不为null说明VCT成功，且该值就是此时应该落子的位置
     */
    public static Integer tryVct(GobangChess chess, Color me, Color opponent) {
        return tryVct(chess, me, opponent, ADDITION_LIVE_THREE_COUNT);
    }

    /**
     * 尝试进行VCT，至少需要构造一个活三；additionLiveThreeCount代表还可以再构造几个活三
     * 本方法的实现逻辑可能有点问题；另外additionLiveThreeCount不能太大，否则效率太低了
     */
    private static Integer tryVct(GobangChess chess, Color me, Color opponent, int additionLiveThreeCount) {

        // 如果有冲四，则VCT成功，此时直接返回一个非null值即可
        Combo myFirst = chess.getFirstComboOf(me);
        if (myFirst instanceof RushFour) {
            return XyUtil.NULL_POSITION;
        }

        // 如果对方有活四或双冲四，则失败
        if (hasLiveFourOrMultiRushFour(chess, opponent)) {
            return null;
        }

        // 如果对方有冲四，则堵上对方的冲四
        Combo opponentFirst = chess.getFirstComboOf(opponent);
        if (opponentFirst instanceof RushFour) {
            return tryVctOnOpponentOneRushFour(chess, me, opponent, additionLiveThreeCount, opponentFirst);
        }

        // 如果我方有活三，则返回一个非null值代表成功
        if (myFirst instanceof LiveThree) {
            return XyUtil.NULL_POSITION;
        }

        // 获取我方所有的活三点
        Set<Map.Entry<Integer, List<Combo>>> liveThreeEntries = chess.getLiveThreeMapOf(me).entrySet();

        // 获取到我方的冲四点
        Set<Integer> rushFourPositions = chess.getRushFourMapOf(me).keySet();

        // 临时集合，调用tryVcf方法时会用到
        LinkedList<Integer> path = new LinkedList<>();

        // 依次尝试在活三点中落子，并判断是否能无视对手下一步的落子VCF成功；如果是，则返回该活三点
        NEXT_POSITION: for (Map.Entry<Integer, List<Combo>> liveThreeEntry : liveThreeEntries) {
            Integer liveThreePosition = liveThreeEntry.getKey();

            // 如果该活三点同时也是冲四点，则忽略该点；因为之前tryVcf失败了，此时下在冲四点只会重复搜索
            if (rushFourPositions.contains(liveThreePosition)) {
                continue;
            }

            // 在活三点落子
            chess.set(liveThreePosition, me);

            // 对手下一步可以选择活三的其中一个防点，也可以选择自己的冲四点
            Set<Integer> opponentPositions = new HashSet<>(liveThreeEntry.getValue().get(0).getNextPositions());
            opponentPositions.addAll(chess.getRushFourMapOf(opponent).keySet());

            // 枚举对手所有可能的操作，如果所有操作都能使我方VCF成功，则返回这个活三点
            // 只要有任意一个操作不能使得我方获胜，就应该直接判断下一个活三点
            for (Integer opponentPosition : opponentPositions) {
                path.clear();
                chess.set(opponentPosition, opponent);
                boolean vcfSuccess = tryVcfInternal(chess, me, opponent, path);

                // 注意，如果VCF失败了，也不要立刻认为该点不可行；此时可以再构建一个活三，然后再VCF看看能不能赢
                // 如果再构建一个活三后成功地VCF了，则当前的活三点是可行的
                // 另外，由于VCT需要考虑的情况比VCF多，因此效率没那么高，因此需要通过additionLiveThreeCount参数限制VCT递归调用次数
                if (!vcfSuccess && additionLiveThreeCount > 0 && tryVct(chess, me, opponent, additionLiveThreeCount - 1) != null) {
                    vcfSuccess = true;
                }

                // 如果当前活三点不可行，则立刻判断下一个活三点
                if (!vcfSuccess) {
                    chess.back();
                    chess.back();
                    continue NEXT_POSITION;
                }

                // 回溯
                chess.back();
            }

            // 执行到这里，说明当前活三点可行，因此返回该点
            chess.back();
            return liveThreePosition;
        }

        return null;
    }

    /**
     * 如果我方在VCT时被对方反四了，则调用该方法判断我方能否VCT成功；注意，此时我方是没有冲四的
     *
     * 注意，此时对手肯定只有opponentRushFour这唯一一个冲四
     */
    private static Integer tryVctOnOpponentOneRushFour(GobangChess chess, Color me, Color opponent, int additionLiveThreeCount, Combo opponentRushFour) {

        // 堵住对方的冲四
        Integer myPosition = opponentRushFour.getNextPositions().get(0);
        chess.set(myPosition, me);

        // 如果对手有活三，则VCT失败
        if (chess.getFirstComboOf(opponent) instanceof LiveThree) {
            chess.back();
            return null;
        }

        // 如果我方没有活三了，说明之前在我方构造活三后，对方挡住活三的同时产生了冲四，此时VCT失败
        Combo myFirst = chess.getFirstComboOf(me);
        if (!(myFirst instanceof LiveThree)) {
            chess.back();
            return null;
        }

        // 否则，说明对手的冲四只是在延缓我们VCT的步伐，我们挡住对手的冲四后，对手必须再来挡我方的活三

        // 临时集合，调用tryVcf方法时会用到
        LinkedList<Integer> path = new LinkedList<>();

        // 遍历我方活三的防点
        for (Integer nextPosition : myFirst.getNextPositions()) {
            path.clear();
            chess.set(nextPosition, opponent);
            boolean vcfSuccess = tryVcfInternal(chess, me, opponent, path);

            // 注意，如果VCF失败了，也不要立刻认为该点不可行；此时可以再构建一个活三，然后再VCF看看能不能赢
            // 如果再构建一个活三后成功地VCF了，则当前的活三点是可行的
            // 另外，由于VCT需要考虑的情况比VCF多，因此效率没那么高，因此需要通过additionLiveThreeCount参数限制VCT递归调用次数
            if (!vcfSuccess && additionLiveThreeCount > 0 && tryVct(chess, me, opponent, additionLiveThreeCount - 1) != null) {
                vcfSuccess = true;
            }

            if (!vcfSuccess) {
                chess.back();
                chess.back();
                return null;
            }

            chess.back();
        }

        chess.back();
        return myPosition;
    }

    /**
     * 判断color方是否有活四或双冲四；本方法和ComboUtil.isMultiRushFours()方法类似
     */
    private static boolean hasLiveFourOrMultiRushFour(GobangChess chess, Color color) {
        Combo first = chess.getFirstComboOf(color);
        if (first instanceof LiveFour) {
            return true;
        }

        if (!(first instanceof RushFour)) {
            return false;
        }

        Integer nextPosition = null;
        for (Combo combo : chess.getCombosOf(color)) {
            if (!(combo instanceof RushFour)) {
                return false;
            }
            if (nextPosition == null) {
                nextPosition = combo.getNextPositions().get(0);
            } else if (!nextPosition.equals(combo.getNextPositions().get(0))) {
                return true;
            }
        }

        return false;
    }
}