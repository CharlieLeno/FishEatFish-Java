package mvc.controller;

import mvc.model.*;
import mvc.view.GamePanel;


import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import static mvc.model.Movable.Team.DEBRIS;


// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension DIM = new Dimension(1100, 600); //the dimension of the game.
    private final GamePanel gamePanel;
    //this is used throughout many classes.
    public static final Random R = new Random();

    public final static int ANIMATION_DELAY = 40; // milliseconds between frames

    public final static int FRAMES_PER_SECOND = 1000 / ANIMATION_DELAY;

    private final Thread animationThread;

    public static Status gameStatus = Status.INITIALIZE_STATUS;
    public static enum Status {
        INITIALIZE_STATUS,
        BEGIN_STATUS,
        RUN_STATUS,
        PAUSE_STATUS,
        LEVEL_UP_STATUS,
        GAME_OVER_STATUS,
        STOP_STATUS
    }

    //key-codes
    private static final int
            PAUSE = 80, // p key
            QUIT = 81, // q key
            CONTINUE = 67, // c key
            LEFT = 37, // move left; left arrow
            RIGHT = 39, // move right; right arrow
            UP = 38, // move up; up arrow
            DOWN = 40, // move down; down arrow
            START = 83, // s key
            MUTE = 77; // m-key mute

    // for possible future use
    // HYPER = 68, 					// D key
    // SPECIAL = 70; 				// special key;  F key

    private final Clip soundBackground;

    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() {

        gamePanel = new GamePanel(DIM);
        gamePanel.addKeyListener(this); //Game object implements KeyListener
        soundBackground = Sound.clipForLoopFactory("background.wav");
        soundBackground.loop(Clip.LOOP_CONTINUOUSLY);

        animationThread = new Thread(this); // pass the animation thread a runnable object, the Game object
        animationThread.setDaemon(true);
        animationThread.start();
    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String[] args) {
        //typical Swing application start; we pass EventQueue a Runnable object.
        EventQueue.invokeLater(Game::new);
    }

    // Game implements runnable, and must have run method
    @Override
    public void run() {
        while (true) {
            try {
                // run a status machine
                switch (gameStatus) {
                    case INITIALIZE_STATUS:
                        animationThread.setPriority(Thread.MIN_PRIORITY);
                        gameStatus = Status.BEGIN_STATUS;
                        break;

                    case BEGIN_STATUS:
                        gamePanel.update(gamePanel.getGraphics());
                        //the change to RUN_STATUS is completed in keyboard listening
                        break;

                    case RUN_STATUS:
                        gameStep();
                        if (CommandCenter.getInstance().isGameOver()) {
                            CommandCenter.getInstance().clearAll();
                            gameStatus = Status.GAME_OVER_STATUS;
                        }
                        if (isLevelCompleted()) {
                            initNewLevel();
                            gameStatus = Status.LEVEL_UP_STATUS;
                        }
                        //the change to PAUSE_STATUS is completed in keyboard listening
                        //the change to STOP_STATUS is completed in keyboard listening
                        break;

                    case PAUSE_STATUS:
                        //the change to RUN_STATUS is completed in keyboard listening
                    case LEVEL_UP_STATUS:
                        //the change to RUN_STATUS is completed in keyboard listening
                        //the change to STOP_STATUS is completed in keyboard listening
                    case GAME_OVER_STATUS:
                        gamePanel.update(gamePanel.getGraphics());
                        //the change to RUN_STATUS is completed in keyboard listening
                        //the change to STOP_STATUS is completed in keyboard listening
                        break;

                    case STOP_STATUS:
                    default:
                        //do nothing
                }
            } catch (Exception e) {
                    // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
            }
        }
    }

    public void gameStep() {
        if (Thread.currentThread() != animationThread) {
            return;
        }
        long startTime = System.currentTimeMillis();
        //this call will cause all movables to move() and draw() themselves every ~40ms
        // see GamePanel class for details
        gamePanel.update(gamePanel.getGraphics());
        checkCollisions();
        checkDisappear();
        spawnEnemyFish();
        checkFloaters();
        //keep track of the frame for development purposes
        CommandCenter.getInstance().incrementFrame();
        holdTime(startTime);
    }

    // surround the sleep() in a try/catch block, this simply controls delay time between
    // the frames of the animation
    private void holdTime(long startTime) {
        if (Thread.currentThread() != animationThread) {
            return;
        }
        try {
            // The total amount of time is guaranteed to be at least ANIMATION_DELAY long.  If processing (update)
            // between frames takes longer than ANIMATION_DELAY, then the difference between startTime -
            // System.currentTimeMillis() will be negative, then zero will be the sleep time
            startTime += ANIMATION_DELAY;
            Thread.sleep(Math.max(0,
                    startTime - System.currentTimeMillis()));
        } catch (InterruptedException e) {
            // do nothing (bury the exception), and just continue, e.g. skip this frame -- no big deal
        }
    }

    private void checkFloaters() {
        if (CommandCenter.getInstance().getFrame() < 10) {
            return;
        }
        spawnHeartFloater();
        spawnStarFloater();
        spawnBombFloater();
    }

    private void checkCollisions() {

        //This has order-of-growth of O(FOES * FRIENDS)
        Point pntFriendCenter, pntFoeCenter;
        int radFriend, radFoe;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {

                pntFriendCenter = movFriend.getCenter();
                pntFoeCenter = movFoe.getCenter();
                radFriend = movFriend.getRadius();
                radFoe = movFoe.getRadius();
                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (radFriend + radFoe)) {

                    // fish warrior can kill any other enemy
                    if (movFriend instanceof FishWarrior) {
                        movFoe.dead();
                        Sound.playSound("kill.wav");
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        CommandCenter.getInstance().setCurrEatNumber(CommandCenter.getInstance().getCurrEatNumber() + 1);
                        CommandCenter.getInstance().setEatNumberForSize(CommandCenter.getInstance().getEatNumberForSize() + 1);
                        continue;
                    }

                    // smaller fish would be eaten
                    if (radFriend >= radFoe) {
                        //enqueue the foe
                        movFoe.dead();
                        Sound.playSound("eat.wav");
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
                        CommandCenter.getInstance().setCurrEatNumber(CommandCenter.getInstance().getCurrEatNumber() + 1);
                        CommandCenter.getInstance().setEatNumberForSize(CommandCenter.getInstance().getEatNumberForSize() + 1);
                    } else {
                        //enqueue the friend
                        movFriend.dead();
                        CommandCenter.getInstance().getOpsQueue().enqueue(movFriend, GameOp.Action.REMOVE);
                    }

                }
            }//end inner for
        }//end outer for

        //check for collisions between nemo and floaters. Order of growth of O(FLOATERS)
        Point pntFloaterCenter;
        int radFloater;
        for (Movable movFriend : CommandCenter.getInstance().getMovFriends()) {
            for (Movable movFloater : CommandCenter.getInstance().getMovFloaters()) {
                pntFriendCenter = movFriend.getCenter();
                radFriend = movFriend.getRadius();
                pntFloaterCenter = movFloater.getCenter();
                radFloater = movFloater.getRadius();
                //detect collision
                if (pntFriendCenter.distance(pntFloaterCenter) < (radFriend + radFloater)) {
                    //enqueue the floater
                    CommandCenter.getInstance().getOpsQueue().enqueue(movFloater, GameOp.Action.REMOVE);
                    Floater floater = (Floater) movFloater;
                    floater.action(movFriend);
                }//end if
            }//end for
        }

        processGameOpsQueue();

    }//end meth

    // remove all foreign objects that out of range
    private void checkDisappear() {
        Point pntFoeCenter;
        int radFoe;
        for (Movable movFoe : CommandCenter.getInstance().getMovFoes()) {
            pntFoeCenter = movFoe.getCenter();
            radFoe = movFoe.getRadius();
            if (isOutOfScale(pntFoeCenter.getX(), pntFoeCenter.getY(), radFoe)) {
                CommandCenter.getInstance().getOpsQueue().enqueue(movFoe, GameOp.Action.REMOVE);
            }
        }
    }

    private boolean isOutOfScale(double x, double y, int radius) {
        return (x < -radius || x > Game.DIM.width + radius || y < -radius || y > Game.DIM.height + radius);
    }
    //This method adds and removes movables to/from their respective linked-lists.
    private void processGameOpsQueue() {

        //deferred mutation: these operations are done AFTER we have completed our collision detection to avoid
        // mutating the movable linkedlists while iterating them above.
        while (!CommandCenter.getInstance().getOpsQueue().isEmpty()) {

            GameOp gameOp = CommandCenter.getInstance().getOpsQueue().dequeue();

            //given team, determine which linked-list this object will be added-to or removed-from
            LinkedList<Movable> list;
            Movable mov = gameOp.getMovable();
            switch (mov.getTeam()) {
                case FOE:
                    list = CommandCenter.getInstance().getMovFoes();
                    break;
                case FRIEND:
                    list = CommandCenter.getInstance().getMovFriends();
                    break;
                case FLOATER:
                    list = CommandCenter.getInstance().getMovFloaters();
                    break;
                case DEBRIS:
                default:
                    list = CommandCenter.getInstance().getMovDebris();
            }

            //pass the appropriate linked-list from above
            //this block will execute the add() or remove() callbacks in the Movable models.
            GameOp.Action action = gameOp.getAction();
            if (action == GameOp.Action.ADD)
                mov.add(list);
            else //REMOVE
                mov.remove(list);

        }//end while
    }

    private void spawnHeartFloater() {
        if (CommandCenter.getInstance().getFrame() % Heart.SPAWN_HEART_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Heart(), GameOp.Action.ADD);
        }
    }

    private void spawnStarFloater() {
        if (CommandCenter.getInstance().getFrame() % Star.SPAWN_STAR_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Star(), GameOp.Action.ADD);
        }
    }

    private void spawnBombFloater() {
        if (CommandCenter.getInstance().getFrame() % Bomb.SPAWN_BOMB_FLOATER == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new Bomb(), GameOp.Action.ADD);
        }
    }
    //this method spawns new Large (0) Asteroids
    private void spawnEnemyFish() {
        if (CommandCenter.getInstance().getFrame() % EnemyFish.SPAWN_ENEMY_FISH == 0) {
            CommandCenter.getInstance().getOpsQueue().enqueue(new EnemyFish(), GameOp.Action.ADD);
        }
    }


    private boolean isLevelCompleted() {
        return CommandCenter.getInstance().getScore() >= CommandCenter.getInstance().getTargetScore();
    }

    private void initNewLevel() {
        CommandCenter.getInstance().clearAll();
        int level = CommandCenter.getInstance().getLevel();
        level = level + 1;
        CommandCenter.getInstance().setTargetScore();
        CommandCenter.getInstance().setCurrEatNumber(0);
        CommandCenter.getInstance().setEatNumberForSize(0);
        CommandCenter.getInstance().setLevel(level);
        CommandCenter.getInstance().getNemo().initNemo();
        Sound.playSound("levelup.wav");
    }


    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        Arrays.stream(clpClips).forEach(clip -> clip.stop());
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        Nemo nemo = CommandCenter.getInstance().getNemo();
        int keyCode = e.getKeyCode();

        if (keyCode == START && (gameStatus == Status.BEGIN_STATUS || gameStatus == Status.GAME_OVER_STATUS)) {
            CommandCenter.getInstance().initGame();
            gameStatus = Status.RUN_STATUS;
            return;
        }

        if (keyCode == CONTINUE && (gameStatus == Status.LEVEL_UP_STATUS || gameStatus == Status.PAUSE_STATUS)) {
            gameStatus = Status.RUN_STATUS;
            return;
        }

        switch (keyCode) {
            case PAUSE:
                CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
                if (gameStatus == Status.RUN_STATUS) {
                    gameStatus = Status.PAUSE_STATUS;
                }
                break;
            case QUIT:
                System.exit(0);
                gameStatus = Status.STOP_STATUS;
                break;
            case UP:
                nemo.setUpSpeed(Nemo.VERTICAL_SPEED);
                nemo.setDownSpeed(0);
                break;
            case DOWN:
                nemo.setDownSpeed(Nemo.VERTICAL_SPEED);
                nemo.setUpSpeed(0);
                break;
            case LEFT:
                nemo.setLeftSpeed(Nemo.HORIZONTAL_SPEED);
                nemo.setRightSpeed(0);
                nemo.setTurnState(Nemo.TurnState.LEFT);
                break;
            case RIGHT:
                nemo.setRightSpeed(Nemo.HORIZONTAL_SPEED);
                nemo.setLeftSpeed(0);
                nemo.setTurnState(Nemo.TurnState.RIGHT);
                break;

            default:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        Nemo nemo = CommandCenter.getInstance().getNemo();
        int keyCode = e.getKeyCode();
        //show the key-code in the console
        System.out.println(keyCode);

        switch (keyCode) {
            //releasing LEFT, RIGHT, UP, DOWN arrow key will set the TurnState to IDLE
            case LEFT:
                nemo.setTurnState(Nemo.TurnState.IDLE);
                nemo.setLeftSpeed(Nemo.INITIAL_SPEED);
                nemo.setRightSpeed(0);
                nemo.setUpSpeed(0);
                nemo.setDownSpeed(0);
                break;
            case RIGHT:
                nemo.setTurnState(Nemo.TurnState.IDLE);
                nemo.setLeftSpeed(0);
                nemo.setRightSpeed(Nemo.INITIAL_SPEED);
                nemo.setUpSpeed(0);
                nemo.setDownSpeed(0);
                break;
            case UP:
            case DOWN:
                nemo.setTurnState(Nemo.TurnState.IDLE);
                nemo.setUpSpeed(0);
                nemo.setDownSpeed(0);
                break;
            case MUTE:
                CommandCenter.getInstance().setMuted(!CommandCenter.getInstance().isMuted());

                if (CommandCenter.getInstance().isMuted()) {
                    stopLoopingSounds(soundBackground);
                } else {
                    soundBackground.loop(Clip.LOOP_CONTINUOUSLY);
                }
                break;

            default:
                break;
        }

    }

    @Override
    // does nothing, but we need it b/c of KeyListener contract
    public void keyTyped(KeyEvent e) {
    }

}


