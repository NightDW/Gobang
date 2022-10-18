import com.laidw.gobang.core.GobangChess;
import com.laidw.gobang.core.constant.Color;
import com.laidw.gobang.core.player.IntelligentPlayer;
import com.laidw.gobang.core.util.XyUtil;

/**
 * 对接五子棋比赛程序的组件
 */
public class LaidwJavaPlayer {
    private GobangChess chess = new GobangChess(15);
    private IntelligentPlayer i;
    private Color opponent;

    public int run(int position, int round) {
        position = revertPosition(position);

        // i为null，说明是第一次调用，此时判断我方是黑棋还是白棋，并初始化
        if (i == null) {
            Color me;
            if (position == XyUtil.NULL_POSITION) {
                i = new IntelligentPlayer(me = Color.BLACK, chess);
            } else {
                i = new IntelligentPlayer(me = Color.WHITE, chess);
            }
            opponent = me.reversed();
        }

        if (position != XyUtil.NULL_POSITION) {
            chess.set(position, opponent);
        }
        int myPosition = i.play(position, XyUtil.NULL_POSITION);
        return revertPosition(myPosition);
    }

    private static int revertPosition(int position) {
        int x = XyUtil.x(position);
        int y = XyUtil.y(position);
        return XyUtil.position(y, x);
    }
}