package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.Sound;
import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import static mvc.model.EnemyFish.FishIndex.*;

// EnemyFish include 4 fishes with different size. They are generated randomly and have different probabilities.
@Data
public class EnemyFish extends Sprite {
    private int size;
    private BufferedImage image;
    private int yCoordinate;
    private int point;
    private int speed;

    public static final int SPAWN_ENEMY_FISH = Game.FRAMES_PER_SECOND;

    public static enum FishType {
        RED_LEFT,
        RED_RIGHT,
        BLUE_LEFT,
        BLUE_RIGHT,
        YELLOW_LEFT,
        YELLOW_RIGHT,
        BLACK_LEFT,
        BLACK_RIGHT
    }

    private static Object[][] fishes = {
            // Images are from "https://github.com/mehmeteminyildiz/EatFishEat_Game"
            {0, 25, "/imgs/enemy/red_left.png", 100, 5, 10},
            {1, 25, "/imgs/enemy/red_right.png", 100, 5, 10},
            {0, 40, "/imgs/enemy/blue_left.png", 200, 3, 8},
            {1, 40, "/imgs/enemy/blue_right.png", 200, 3, 8},
            {0, 50, "/imgs/enemy/yellow_left.png", 300, 2, 5},
            {1, 50, "/imgs/enemy/yellow_right.png", 300, 2, 5},
            {0, 80, "/imgs/enemy/black_left.png", 400, 5, 8},
            {1, 80, "/imgs/enemy/black_right.png", 400, 5, 8},
    };

    public static enum FishIndex {
        DIRECTION,
        SIZE,
        IMAGE_PATH,
        POINT,
        SPEED_DOWN_LIMIT,
        SPEED_UP_LIMIT,
    }

    public EnemyFish() {
        //EnemyFish is FOE
        setTeam(Team.FOE);

        // generate different size fish with different probability
        double[] probabilities = {65, 80, 95, 100};
        int random = somePosValue(101);
        int type = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (random < probabilities[i]) {
                type = i * 2 + somePosValue(2);
                break;
            }
        }
        image = loadGraphic((String) fishes[type][IMAGE_PATH.ordinal()]);
        size = (int) fishes[type][SIZE.ordinal()];
        point = (int) fishes[type][POINT.ordinal()];
        int direction = (int) fishes[type][DIRECTION.ordinal()];


        int yCenter = size * (somePosValue(Game.DIM.height / size - 3) + 1);
        int speed = somePosValue((int)fishes[type][SPEED_UP_LIMIT.ordinal()] - (int) fishes[type][SPEED_DOWN_LIMIT.ordinal()] + 1) + (int) fishes[type][SPEED_DOWN_LIMIT.ordinal()];
        if (direction == 0) { // left
            setCenter(new Point(Game.DIM.width, yCenter));
            setDeltaX(-speed);
        } else { // right
            setCenter(new Point(0, yCenter));
            setDeltaX(speed);
        }
        setDeltaY(0);

        setRadius(size);
    }

    @Override
    // Enemy fish can not cross the frame edge, they would be removed after moving out of the frame.
    public void move() {

        double newXPos = getCenter().getX() + getDeltaX();
        double newYPos = getCenter().getY() + getDeltaY();
        setCenter(new Point((int) newXPos, (int) newYPos));

        //expire (decrement expiry) on short-lived objects only
        //the default value of expiry is zero, so this block will only apply to expiring sprites
        if (getExpiry() > 0) super.expire();
    }

    public void dead() {
        // When enemy fish dead, add corresponding points to the game. When they disappear, no points added.
        CommandCenter.getInstance().setScore(CommandCenter.getInstance().getScore() + point);
    }

    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D) g, image);
    }

}
