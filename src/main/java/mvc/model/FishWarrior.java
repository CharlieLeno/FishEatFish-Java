package mvc.model;

import lombok.Data;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
// Fish warrior can kill any other enemy, and can help nemo score and grow.
public class FishWarrior extends Sprite {

    private static final int WARRIOR_EXPIRE_INIT = 200;
    private static final int WARRIOR_RADIUS = 35;
    private ImageState imageState = ImageState.WARRIOR_RIGHT;

    public enum ImageState {
        WARRIOR_LEFT, //for left moving
        WARRIOR_RIGHT, //for right moving
    }

    private Nemo nemo;

    public FishWarrior(Nemo nemo) {
        setTeam(Team.FRIEND);
        setRadius(WARRIOR_RADIUS);
        setExpiry(WARRIOR_EXPIRE_INIT);

        // Fish warrior is related to nemo, and initialize it with nemo
        this.nemo = nemo;
        // Shield nemo more time than FishWarrior.
        nemo.setShield(WARRIOR_EXPIRE_INIT + 30);

        Map<ImageState, BufferedImage> rasterMap = new HashMap<>();
        // Images are from "https://github.com/mehmeteminyildiz/EatFishEat_Game"
        rasterMap.put(ImageState.WARRIOR_LEFT, loadGraphic("/imgs/fishwarrior/warrior_left.png") ); //left moving nemo
        rasterMap.put(ImageState.WARRIOR_RIGHT, loadGraphic("/imgs/fishwarrior/warrior_right.png") ); //right moving nemo
        setRasterMap(rasterMap);
    }

    public void move() {
        super.move();
        // go with nemo
        if (nemo.getDeltaX() > 0) {
            setCenter(new Point((int) (nemo.getCenter().getX() + nemo.getRadius() + getRadius() + 10), (int) (nemo.getCenter().getY() - nemo.getRadius() - 20)));
        } else {
            setCenter(new Point((int) (nemo.getCenter().getX() - nemo.getRadius() - getRadius() - 10), (int) (nemo.getCenter().getY() - nemo.getRadius() - 20)));
        }
    }

    @Override
    public void draw(Graphics g) {
        // go with nemo
        if (nemo.getDeltaX() > 0) {
            imageState = ImageState.WARRIOR_RIGHT;
        } else {
            imageState = ImageState.WARRIOR_LEFT;
        }

        renderRaster((Graphics2D) g, getRasterMap().get(imageState));
    }

    @Override
    public void remove(LinkedList<Movable> list) {
        super.remove(list);
        nemo.setShield(30);
    }
}
