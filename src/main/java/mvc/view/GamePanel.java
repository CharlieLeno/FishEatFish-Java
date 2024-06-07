package mvc.view;

import mvc.controller.CommandCenter;
import mvc.controller.Game;
import mvc.controller.Utils;
import mvc.model.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class GamePanel extends Panel {

    // ==============================================================
    // FIELDS
    // ==============================================================
    private final Font fontNormal = new Font("SansSerif", Font.BOLD, 18);
    private final Font fontBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
    private FontMetrics fontMetrics;
    private int fontWidth;
    private int fontHeight;
    Image backgroundImage;

    // ==============================================================
    // CONSTRUCTOR
    // ==============================================================

    public GamePanel(Dimension dim) {

        GameFrame gameFrame = new GameFrame();

        gameFrame.getContentPane().add(this);

        gameFrame.pack();
        initFontInfo();
        gameFrame.setSize(dim);
        //change the name of the game-frame to your game name
        gameFrame.setTitle("Nemo's Adventure");
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        setFocusable(true);
        backgroundImage = loadGraphic("/imgs/others/sea.png");
    }


    // ==============================================================
    // METHODS
    // ==============================================================

    private void drawNemoStatus(final Graphics graphics){


        //draw score always
        graphics.setColor(Color.YELLOW);
        graphics.setFont(fontBig);
        graphics.drawString("Score :  " + CommandCenter.getInstance().getScore(), Game.DIM.width / 2 - 100, fontHeight * 2); //middle

        //draw the level upper-left corner always
        graphics.setColor(Color.WHITE);
        graphics.setFont(fontNormal);
        String levelText = "Level: " + CommandCenter.getInstance().getLevel();
        graphics.drawString(levelText, fontWidth, 30); //upper-left corner

        //build the status string array with possible messages in middle of screen
        List<String> statusArray = new ArrayList<>();
        if (CommandCenter.getInstance().getNemo().getShowLevel() > 0) statusArray.add(levelText);

        //draw the statusArray strings to middle of screen
        if (!statusArray.isEmpty())
            displayTextOnScreen(graphics, statusArray.toArray(new String[0]));
    }

    //this is used for development, you can remove it from your final game
    private void setFont(Graphics g) {
        g.setColor(Color.white);
        g.setFont(fontNormal);
//        g.drawString("FRAME :  " + CommandCenter.getInstance().getFrame(), fontWidth,
//                Game.DIM.height  - (fontHeight + 22));

    }

    @Override
    public void update(Graphics g) {


        // The following "off" vars are used for the off-screen double-buffered image.
        //used for double-buffering
        Image imgOff = createImage(Game.DIM.width, Game.DIM.height);
        //get its graphics context
        Graphics grpOff = imgOff.getGraphics();

        //Fill the image background with a sea image.
        grpOff.drawImage(backgroundImage, 0, 0, Game.DIM.width, Game.DIM.height, this);

        //this is used for development, you may remove drawNumFrame() in your final game.
        setFont(grpOff);

        if (Game.gameStatus == Game.Status.BEGIN_STATUS) {
            displayTextOnScreen(grpOff,
                    "Welcome!",
                    "Eat Smaller Fish",
                    "Use Arrow Keys To Move",
                    "Be Careful About Shark And Bomb!",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"

            );
        } else if (Game.gameStatus == Game.Status.GAME_OVER_STATUS) {
            displayTextOnScreen(grpOff,
                    "GAME OVER!",
                    "Your Score: " + CommandCenter.getInstance().getScore(),
                    "Try Again!",
                    "'S' to Start",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"
            );
        } else if (Game.gameStatus == Game.Status.PAUSE_STATUS) {

            displayTextOnScreen(grpOff,
                    "Game Paused",
                    "'C' to Continue"
            );
        } else if (Game.gameStatus == Game.Status.RUN_STATUS) {
            moveDrawMovables(grpOff,
                    CommandCenter.getInstance().getMovFloaters(),
                    CommandCenter.getInstance().getMovFoes(),
                    CommandCenter.getInstance().getMovDebris(),
                    CommandCenter.getInstance().getMovFriends());

            drawNumberNemosRemaining(grpOff);
            drawNemoStatus(grpOff);
        } else if (Game.gameStatus == Game.Status.LEVEL_UP_STATUS) {
            displayTextOnScreen(grpOff,
                    "New Level!",
                    "Level " + (CommandCenter.getInstance().getLevel() - 1) + " Completed!",
                    "Current Score: " + CommandCenter.getInstance().getScore(),
                    "Next Score Target: " + CommandCenter.getInstance().getTargetScore(),
                    "'C' to Continue",
                    "'P' to Pause",
                    "'Q' to Quit",
                    "'M' to toggle music"
            );
        }

        //after drawing all the movables or text on the offscreen-image, copy it in one fell-swoop to graphics context
        // of the game panel, and show it for ~40ms. If you attempt to draw sprites directly on the gamePanel, e.g.
        // without the use of a double-buffered off-screen image, you will see flickering.
        g.drawImage(imgOff, 0, 0, this);
    }


    //this method causes all sprites to move and draw themselves
    @SafeVarargs
    private final void moveDrawMovables(final Graphics g, List<Movable>... teams) {

        BiConsumer<Movable, Graphics> moveDraw = (mov, grp) -> {
            mov.move();
            mov.draw(grp);
        };


        Arrays.stream(teams) //Stream<List<Movable>>
                //we use flatMap to flatten the teams (List<Movable>[]) passed-in above into a single stream of Movables
                .flatMap(Collection::stream) //Stream<Movable>
                .forEach(m -> moveDraw.accept(m, g));


    }


    // Draw the number of nemos remaining on the bottom-right of the screen.
    private void drawNumberNemosRemaining(Graphics g) {
        int numNemos = CommandCenter.getInstance().getNumNemos();
        Image heartImage = loadGraphic("/imgs/others/heart.png");
        while (numNemos > 0) {
            drawOneHeart(g, heartImage, numNemos--);
        }
    }


    private void drawOneHeart(Graphics g, Image image, int offSet) {

        int xVal = Game.DIM.width - (22 * offSet) - 40;
        int yVal = 15;

        g.drawImage(image, xVal, yVal,22,22, this);
    }

    private void initFontInfo() {
        Graphics g = getGraphics();            // get the graphics context for the panel
        g.setFont(fontNormal);                        // take care of some simple font stuff
        fontMetrics = g.getFontMetrics();
        fontWidth = fontMetrics.getMaxAdvance();
        fontHeight = fontMetrics.getHeight();
        g.setFont(fontBig);                    // set font info
    }


    // This method draws some text to the middle of the screen
    private void displayTextOnScreen(final Graphics graphics, String... lines) {

        //AtomicInteger is safe to pass into a stream
        final AtomicInteger spacer = new AtomicInteger(0);
        Arrays.stream(lines)
                .forEach(str ->
                            graphics.drawString(str, (Game.DIM.width - fontMetrics.stringWidth(str)) / 2,
                                    Game.DIM.height / 4 + fontHeight + spacer.getAndAdd(40))

                );


    }

    protected BufferedImage loadGraphic(String imagePath) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(Objects.requireNonNull(GamePanel.class.getResourceAsStream(imagePath)));
        }
        catch (IOException e) {
            e.printStackTrace();
            bufferedImage = null;
        }
        return bufferedImage;
    }
}
