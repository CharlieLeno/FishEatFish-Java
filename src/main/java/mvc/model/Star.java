package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.GameOp;
import mvc.controller.Sound;

import java.util.LinkedList;

public class Star extends Floater{
    public static final int SPAWN_STAR_FLOATER = Game.FRAMES_PER_SECOND * 40;
    // Images are from "https://github.com/mehmeteminyildiz/EatFishEat_Game"
    public Star() {
        super("/imgs/others/star.png");
    }

    @Override
    public void action(Movable move) {
        //generateProtector; TBD
        Sound.playSound("kill.wav");
        CommandCenter.getInstance().getOpsQueue().enqueue(new FishWarrior(CommandCenter.getInstance().getNemo()), GameOp.Action.ADD);
    }

}
