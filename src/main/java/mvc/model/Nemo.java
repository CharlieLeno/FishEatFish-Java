package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
public class Nemo extends Sprite {

    //number of frames that the nemo will be protected after a spawn
    public static final int INITIAL_SPAWN_TIME = 60;
    public static final int INVISIBLE_TIME = 20;
    public static final int INITIAL_SPEED = 3;
    //number of frames nemo will be nemo after consuming a Heart
    public static final int MAX_SHIELD = 200;

    public static final int VERTICAL_SPEED = 8;
    public static final int HORIZONTAL_SPEED = 12;


    public static final int SMALL_RADIUS = 25;
    public static final int MEDIUM_RADIUS = 40;
    public static final int LARGE_RADIUS = 55;

    public static final int MEDIUM_SIZE_NUM_THRESH = 10;
    public static final int LARGE_SIZE_NUM_THRESH = 20;

    //images states
    public enum ImageState {
        NEMO_INVISIBLE, //for pre-spawning
        NEMO_LEFT, //for left moving
        NEMO_RIGHT, //for right moving
        NEMO_PRO // for protected nemo
    }

    //instance fields (getters/setters provided by Lombok @Data above)
    private int shield;
    private int invisible;
    private int leftSpeed;
    private int rightSpeed;
    private int upSpeed;
    private int downSpeed;

    private int showLevel;

    //enum used for turnState field
    public enum TurnState {IDLE, LEFT, RIGHT}
    private TurnState turnState = TurnState.IDLE;
    private ImageState imageState = ImageState.NEMO_RIGHT;

    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================
    public Nemo() {
        setTeam(Team.FRIEND);
        setRadius(SMALL_RADIUS);

        //We use HashMap which has a seek-time of O(1)
        //See the resources directory in the root of this project for pngs.
        //Using enums as keys is safer b/c we know the value exists when we reference the consts later in code.
        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        rasterMap.put(Nemo.ImageState.NEMO_INVISIBLE, null );
        // Images are from "https://github.com/mehmeteminyildiz/EatFishEat_Game"
        rasterMap.put(Nemo.ImageState.NEMO_LEFT, loadGraphic("/imgs/nemo/nemo_left.png") ); //left moving nemo
        rasterMap.put(Nemo.ImageState.NEMO_RIGHT, loadGraphic("/imgs/nemo/nemo_right.png") ); //right moving nemo

        setRasterMap(rasterMap);
    }

    // ==============================================================
    // METHODS
    // ==============================================================
    public void move() {
        super.move();

        if (invisible > 0) invisible--;
        if (shield > 0) shield--;
        if (showLevel > 0) showLevel--;

        grow();
        setDeltaX(rightSpeed - leftSpeed);
        setDeltaY(downSpeed - upSpeed);

    }

    public void grow() {
        int radius = getRadius();
        switch (radius){
            case SMALL_RADIUS:
                if (CommandCenter.getInstance().getEatNumberForSize() >= MEDIUM_SIZE_NUM_THRESH) {
                    Sound.playSound("grow.wav");
                    setRadius(MEDIUM_RADIUS);
                }
                break;
            case MEDIUM_RADIUS:
                if (CommandCenter.getInstance().getEatNumberForSize() >= LARGE_SIZE_NUM_THRESH) {
                    Sound.playSound("grow.wav");
                    setRadius(LARGE_RADIUS);
                }
                break;
            case LARGE_RADIUS:
            default:
                //do nothing
        }
    }

    @Override
    public void draw(Graphics g) {

        if (invisible > 0){
            imageState = ImageState.NEMO_INVISIBLE;
        } else {
            if (shield > 0) {
                drawShield(g);
            }

            if (getDeltaX() > 0) {
                imageState = ImageState.NEMO_RIGHT;
            } else {
                imageState = ImageState.NEMO_LEFT;
            }
        }

        renderRaster((Graphics2D) g, getRasterMap().get(imageState));
    }

    private void drawShield(Graphics g){
        g.setColor(Color.CYAN);
        g.drawOval(getCenter().x - getRadius(), getCenter().y - getRadius(), getRadius() * 2, getRadius() * 2);
    }

    @Override
    public void remove(LinkedList<Movable> list) {
        //The nemo is never actually removed from the game-space; instead we decrement numNemos
        //only execute the initNemo() method if shield is down.
        if (shield > 0) {
            return;
        }
        initNemo();
    }

    public void initNemo(){
        imageState = ImageState.NEMO_RIGHT;
        setShield(Nemo.INITIAL_SPAWN_TIME);
        setInvisible(INVISIBLE_TIME);
        //put nemo in the middle of the game-space
        setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
        rightSpeed = 2;
        leftSpeed = 0;
        setRadius(Nemo.SMALL_RADIUS);
    }

    public void dead() {
        if (CommandCenter.getInstance().getNumNemos() <= 0) {
            return;
        };
        if (shield > 0) {
            return;
        }

        Sound.playSound("dead.wav");
        CommandCenter.getInstance().setNumNemos(CommandCenter.getInstance().getNumNemos() -1);
        CommandCenter.getInstance().setEatNumberForSize(0);

    }

}
