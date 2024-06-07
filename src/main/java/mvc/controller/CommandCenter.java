package mvc.controller;



import mvc.model.*;
import lombok.Data;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//The CommandCenter is a singleton that manages the state of the game.
//the lombok @Data gives us automatic getters and setters on all members
@Data
public class CommandCenter {

	private  int numNemos;  // at most five
	private  int level;
	private  long targetScore;  // the target score for this level
	private  long currEatNumber;    // the number has completed in the level
	private  long eatNumberForSize; // the number have eaten since newborn
	private  long score;
	private  boolean paused;
	private  boolean muted;


	//this value is used to count the number of frames (full animation cycles) in the game
	private long frame;

	//the nemo is located in the movFriends list, but since we use this reference a lot, we keep track of it in a
	//separate reference. Use final to ensure that the nemo ref always points to the single falcon object on heap.
	//Lombok will not provide setter methods on final members
	private final Nemo nemo  = new Nemo();

	//lists containing our movables subdivided by team
	private final LinkedList<Movable> movDebris = new LinkedList<>();
	private final LinkedList<Movable> movFriends = new LinkedList<>();
	private final LinkedList<Movable> movFoes = new LinkedList<>();
	private final LinkedList<Movable> movFloaters = new LinkedList<>();

	private final GameOpsQueue opsQueue = new GameOpsQueue();

	//for sound playing. Limit the number of threads to 5 at a time.
	private final ThreadPoolExecutor soundExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	//singleton
	private static CommandCenter instance = null;

	// Constructor made private
	private CommandCenter() {
		opsQueue.enqueue(nemo, GameOp.Action.ADD);
	}

	public static CommandCenter getInstance(){
		if (instance == null){
			instance = new CommandCenter();
		}
		return instance;
	}

	public void initGame(){
		setLevel(1);
		setScore(0);
		setCurrEatNumber(0);
		setEatNumberForSize(0);
		setTargetScore();
		setPaused(false);
		setNumNemos(3);
		nemo.initNemo();
	}

	public void setTargetScore() {
		targetScore = score + calcLevelScore(level);
	}

	public int calcLevelScore(int level) {
		return 2000 + level * 2000;
	}

	public void incrementFrame(){
		//use of ternary expression to simplify the logic to one line
		frame = frame < Long.MAX_VALUE ? frame + 1 : 0;
	}

	public void clearAll(){
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
		movDebris.clear();

		movFriends.add(nemo);
	}

	public boolean isGameOver() {		//if the number of nemo is zero, then game over
		return numNemos < 1;
	}

}
