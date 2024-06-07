package mvc.model;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class Floater extends Sprite {
    private BufferedImage image;
    private static final int MAX_X_SPEED = 5;
    private static final int MAX_Y_SPEED = 6;
    public Floater(String imagePath) {

        setTeam(Team.FLOATER);

        //default values, all of which can be overridden in the extending concrete classes
        setExpiry(200);
        setRadius(35);
        //set random xSpeed
        int xSpeed = 0;
        while (xSpeed == 0) {
            xSpeed = somePosNegValue(MAX_X_SPEED);
        }
        //set random ySpeed
        int ySpeed = 0;
        while (ySpeed == 0) {
            ySpeed = somePosNegValue(MAX_Y_SPEED);
        }

        setDeltaX(xSpeed);
        //set random DeltaY
        setDeltaY(ySpeed);

        // make sure new floater is far away from nemo
        Nemo nemo = CommandCenter.getInstance().getNemo();
        setCenter(new Point((int) (nemo.getCenter().getX() + Game.DIM.width / 2 + somePosNegValue(100)), (int) (nemo.getCenter().getY() + Game.DIM.height / 2 + somePosNegValue(100))));

        image = loadGraphic(imagePath);
    }

    public abstract void action(Movable move);

    @Override
    public void draw(Graphics g) {
        renderRaster((Graphics2D)g, image);
    }

}
