package com.laidw.gobang.web.controller;

import com.laidw.gobang.core.GobangGame;
import com.laidw.gobang.core.util.XyUtil;
import com.laidw.gobang.core.player.IntelligentPlayer;
import com.laidw.gobang.web.chess.ViewableChess;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 在HTML页面中展示棋盘信息，并响应用户的指令
 */
@Controller
public class GobangController {
    private final GobangGame game = new GobangGame(15, ViewableChess.class, IntelligentPlayer.class, IntelligentPlayer.class);

    @GetMapping("/gobang")
    public ModelAndView gobang() {
        ModelAndView mav = new ModelAndView("gobang");
        mav.addObject("chess", game.getChess());
        return mav;
    }

    @RequestMapping("/reset")
    public ModelAndView reset() {
        game.reset();
        return gobang();
    }

    @RequestMapping("/next")
    public ModelAndView next(@RequestParam(required = false, value = "x") Integer x, @RequestParam(required = false, value = "y") Integer y, @RequestParam(value = "autoNext", defaultValue = "true") boolean autoNext) {
        game.next(x != null && y != null ? XyUtil.position(x, y) : XyUtil.NULL_POSITION);
        if (autoNext) {
            game.next(XyUtil.NULL_POSITION);
        }
        return gobang();
    }

    @RequestMapping("/back")
    public ModelAndView back() {
        game.back();
        return gobang();
    }
}
