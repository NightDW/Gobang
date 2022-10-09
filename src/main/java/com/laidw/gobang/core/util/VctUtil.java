package com.laidw.gobang.core.util;

import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.combo.Combo;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.constant.Direction;

import java.util.*;

/**
 * 连续冲三（VCT）的实现逻辑；VcfUtil的VCT逻辑需要枚举出所有可能的情况，效率很低，这里采用反推的方式来VCT，效率高，但适用范围较小
 */
public abstract class VctUtil {
    private VctUtil() {}

    /**
     * 尝试VCT
     * 首先需要找到一个冲四点，看看这个冲四点是否同时是活三点
     * 如果不是活三点，而是活二点，则尝试通过先手在该活二的范围内再落一子
     * 这样，之后在此处落子就能形成冲四活三而取胜了
     */
    public static Integer tryVct(GobangChess chess, Color me, Color opponent) {

        // 先拿到对方此时的冲四点和活三点，主要用于判断对手是否会反四/反三
        Set<Integer> opponentRushFourPositions = chess.getRushFourMapOf(opponent).keySet();
        Set<Integer> opponentLiveThreePositions = chess.getLiveThreeMapOf(opponent).keySet();

        // 先根据已有的冲四点来尝试VCT，如果能成功，则直接返回结果
        Integer result;
        if ((result = tryVct(chess, me, opponentRushFourPositions, opponentLiveThreePositions)) != null) {
            return result;
        }

        // 否则，构造活三；我们在构造活三后，对方肯定会来挡该活三，这样也能形成冲四点
        Set<Map.Entry<Integer, List<Combo>>> liveThreeEntries = chess.getLiveThreeMapOf(me).entrySet();
        NEXT_LIVE_THREE_POSITION: for (Map.Entry<Integer, List<Combo>> liveThreeEntry : liveThreeEntries) {

            // 如果该活三点形成的不是连活三，则忽略，因为对手可以下在连活三中间，导致我方没有冲四点
            // 这里不能这么判断，因为该连活三点可能同时还形成了另一个眠三，因此即使活三被挡住，也没有关系
            // for (Combo combo : liveThreeEntry.getValue()) {
            //     if (!(combo instanceof ConThree)) {
            //         continue NEXT_LIVE_THREE_POSITION;
            //     }
            // }

            Integer liveThreePosition = liveThreeEntry.getKey();
            for (Combo combo : liveThreeEntry.getValue()) {

                // 我方下在活三点形成连活三，然后枚举出对手所有可能的落子情况
                // 如果每种落子都能VCT成功，则返回该活三点；否则判断下一个活三点
                chess.set(liveThreePosition, me);
                for (Integer nextPosition : combo.getNextPositions()) {
                    chess.set(nextPosition, opponent);
                    if (tryVct(chess, me, opponentRushFourPositions, opponentLiveThreePositions) == null) {
                        chess.back();
                        chess.back();
                        continue NEXT_LIVE_THREE_POSITION;
                    }
                    chess.back();
                }
                chess.back();
                return liveThreePosition;
            }
        }
        return null;
    }

    /**
     * 尝试进行VCT，此时至少要有一个冲四点
     */
    private static Integer tryVct(GobangChess chess, Color me, Set<Integer> opponentRushFourPositions, Set<Integer> opponentLiveThreePositions) {

        // 获取到所有的冲四点
        Set<Map.Entry<Integer, List<Combo>>> rushFourEntries = chess.getRushFourMapOf(me).entrySet();
        for (Map.Entry<Integer, List<Combo>> rushFourEntry : rushFourEntries) {
            Integer rushFourPosition = rushFourEntry.getKey();
            Combo rushFour = rushFourEntry.getValue().get(0);

            // 该点已经形成一个冲四了，因此尝试让该点变成活三点（形成的活三不能和冲四在同一个方向，并且此时允许对方反三）
            // 如果能变成活三点，则将能使该点变成活三点的落子位置返回
            Integer result;
            if ((result = tryConstructLiveThree(chess, me, rushFourPosition, rushFour.getDirection(), opponentRushFourPositions, opponentLiveThreePositions, new HashSet<>())) != null) {
                return result;
            }
        }

        // 当前冲四点均不可行，返回null
        return null;
    }

    /**
     * 尝试将position变成活三点，并且该活三不能是excludePosition方向上的
     */
    private static Integer tryConstructLiveThree(GobangChess chess, Color me, Integer position, Direction excludePosition, Set<Integer> opponentRushFourPositions, Set<Integer> opponentLiveThreePositions, Set<Integer> path) {

        // 避免无限递归调用
        if (path.contains(position) || path.size() >= 100) {
            return null;
        }

        // 如果该点已经是满足要求的活三点了，则直接返回该点
        // 当path.size() <= 1时，允许对方有反三，这是因为对方有反三也没用，我们下一步就冲四了
        List<Combo> liveThrees = chess.getLiveThreeMapOf(me).getOrEmpty(position);
        for (Combo liveThree : liveThrees) {
            if (liveThree.getDirection() != excludePosition && checkRevert(liveThree, path.size() <= 1, opponentRushFourPositions, opponentLiveThreePositions)) {
                return position;
            }
        }

        // 否则，尝试将该点变成或三点

        // 先将该点记录下来，避免后续又判断该点，造成死循环
        path.add(position);

        // 查看该点能形成哪些活二，并尝试在活二的方向上再落一子，使其变成活三
        List<Combo> liveTwos = chess.getLiveTwoMapOf(me).getOrEmpty(position);
        for (Combo liveTwo : liveTwos) {
            if (liveTwo.getDirection() == excludePosition) {
                continue;
            }
            for (Integer nextPosition : liveTwo.getNextPositions()) {
                Integer result;
                if ((result = tryConstructLiveThree(chess, me, nextPosition, liveTwo.getDirection(), opponentRushFourPositions, opponentLiveThreePositions, path)) != null) {
                    path.remove(position);
                    return result;
                }
            }
        }

        // 找不到，则返回null
        path.remove(position);
        return null;
    }

    /**
     * 判断形成的活三是否会被反四或反活三；如果会被反四，则返回false；如果会被反活三，且allowRevertedLiveThree为false，则也返回false
     */
    private static boolean checkRevert(Combo liveThree, boolean allowRevertedLiveThree, Set<Integer> opponentRushFourPositions, Set<Integer> opponentLiveThreePositions) {
        return noInteract(opponentRushFourPositions, liveThree.getNextPositions()) &&
                (allowRevertedLiveThree || noInteract(opponentLiveThreePositions, liveThree.getNextPositions()));
    }

    /**
     * 确保两个集合没有交集，即我方的活三不会被对方反四或反三
     */
    private static <T> boolean noInteract(Set<T> set, Collection<T> targets) {
        for (T target : targets) {
            if (set.contains(target)) {
                return false;
            }
        }
        return true;
    }
}