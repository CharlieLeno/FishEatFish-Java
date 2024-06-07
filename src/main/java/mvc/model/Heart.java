package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.Sound;

import java.util.LinkedList;

// A heart can increase the nemo number by one, and nemo number limit is 5
public class Heart extends Floater {
    public static final int SPAWN_HEART_FLOATER = Game.FRAMES_PER_SECOND * 50;
    public static final int NEMO_NUMBER_LIMIT = 5;
    // Images are from "https://github.com/mehmeteminyildiz/EatFishEat_Game"
    public Heart() {
        super("/imgs/others/heart.png");
    }

    @Override
    public void action(Movable move) {
        Sound.playSound("heart.wav");
        int nemoNums = CommandCenter.getInstance().getNumNemos();
        if (nemoNums < NEMO_NUMBER_LIMIT) {
            CommandCenter.getInstance().setNumNemos(nemoNums + 1);
        } else {
            CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + 500);
        }
    }
}
