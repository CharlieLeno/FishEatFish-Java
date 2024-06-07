package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.GameOp;
import mvc.controller.Sound;

// Bomb can kill nemo and fish warrior, but can not kill nemo with shield
public class Bomb extends Floater{
    public static final int SPAWN_BOMB_FLOATER = Game.FRAMES_PER_SECOND * 30;
    public Bomb() {
        super("/imgs/others/bomb.png");
//        setTeam(Team.FOE);
    }
    @Override
    public void action(Movable move) {
        //sound; TBD
        Sound.playSound("bomb.wav");
        move.dead();
        CommandCenter.getInstance().getOpsQueue().enqueue(move, GameOp.Action.REMOVE);
        CommandCenter.getInstance().getOpsQueue().enqueue(new WhiteCloudDebris(this), GameOp.Action.ADD);
    }
}
