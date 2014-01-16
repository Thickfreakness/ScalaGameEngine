package adventuregameengine.game;

import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import adventuregameengine.assertions.AssertState;
import adventuregameengine.game.events.GameEvent;
import adventuregameengine.game.events.MoveActorEvent;
import adventuregameengine.graphics.GameScreen;
import adventuregameengine.graphics.Screen;
import adventuregameengine.graphics.twodee.ImageStore;
import adventuregameengine.starter.GameActivity;
import adventuregameengine.starter.WorldFactory;
import adventuregameengine.world.Actor;
import adventuregameengine.world.Player;
import adventuregameengine.world.Room;
import adventuregameengine.world.World;
import android.util.Log;

public class GameProcessor implements Game, Runnable {

	World world;
	volatile boolean running = false;
	Thread gameThread = null;
	GameActivity activity;
	Vector<AssertState> asserts;
	volatile ConcurrentLinkedQueue<GameEvent> eventQueue = new ConcurrentLinkedQueue<GameEvent>();
	Screen screen = null;
	boolean conversing = false;
	Actor speakingTo = null;
	public static final double FPS_DELTA = 16;
	ImageStore store;

	public GameProcessor(String gameState, GameActivity activity) {
		world = WorldFactory.createWorld(gameState, this);
		this.activity = activity;
		asserts = new Vector<AssertState>();
		setStartScreen();
	}

	public void consumeEvents() {
		synchronized (eventQueue) {
			if (eventQueue != null && !eventQueue.isEmpty()) {
				if (eventQueue.peek().running())
					return;
				if (eventQueue.peek().complete()) {
					eventQueue.poll();
					if (!eventQueue.isEmpty()) {
						synchronized (world) {
							eventQueue.peek().processEvent();
						}
					}
				} else {
					synchronized (world) {
						eventQueue.peek().processEvent();
					}
				}
			}
		}
	}

	public Player getPlayer() {
		return world.getPlayer();
	}

	public void resume() {
		running = true;
		gameThread = new Thread(this, "GAME THREAD");
		gameThread.start();
	}

	public void pause() {
		running = false;
		while (true) {
			try {
				gameThread.join();
				break;
			} catch (InterruptedException e) {
				// retry
			}
		}
	}

	public void run() {
		long time = System.nanoTime();
		try{
			Thread.sleep((long) FPS_DELTA);
		}catch(Exception e){
			
		}
		float deltaTime = 0;
		while (running) {
			deltaTime = (System.nanoTime() - time) / 1000;
			time = System.nanoTime();
			if (!conversing) {
				consumeEvents();
				getAssertStatus();
			} else {
				if (!eventQueue.isEmpty())
					eventQueue.clear(); // Don't want stale events to trigger
										// after a convo
			}
			world.update(deltaTime);

			try {
				// Sleep
//				deltaTime = System.nanoTime() - time;
				// Log.d("Thread", "Delta = " + deltaTime);
				if (deltaTime < FPS_DELTA)
					Thread.sleep((long) (FPS_DELTA - deltaTime)); // Aiming for pseudo 60 fps
			} catch (InterruptedException e) {
				Log.d("Game Thead", "Sleep interputed");
			}
		}
	}

	public Screen getCurrentScreen() {
		return screen;
	}

	public void setStartScreen() {
		screen = new GameScreen(activity, world.getActiveRoom());
	}

	public void stateChange(World world) {
		Set inv = getPlayer().getInventory();
		this.world = world;
		getPlayer().setInventory(inv);
	}

	public boolean getAssertStatus() {
		for (AssertState state : asserts) {
			if (!state.isTrue())
				return false;
		}
		return true;
	}

	public Room getCurrentRoom() {
		return world.getActiveRoom();
	}

	public void addToEventQueue(GameEvent event) {
		if(event instanceof MoveActorEvent && eventQueue.peek() instanceof MoveActorEvent)
			eventQueue.clear();
		eventQueue.add(event);
	}

	public World getWorld() {
		return world;
	}

	public void action(int[] xyz) {
		world.getActiveRoom().getAtXYZ(xyz).action();
	}

	public void sayThought(String thought) {
		screen.sayThought(thought);
	}

	public void startConversation(Actor a) {
		conversing = true;
		speakingTo = a;
		screen.startConversation(a);
		a.getConvo().reset();
	}
	
	public void envConversation(){
		conversing = false;
		speakingTo = null;
		screen.endConversation();
	}

	public boolean isConversing() {
		return conversing;
	}

	public String nextLine() {
		if (speakingTo != null)
			return speakingTo.getConvo().nextLine();
		else
			return "";
	}

	public String[] convoOptions() {
		if (speakingTo != null)
			return speakingTo.getConvo().getOptionsAndReset();
		else
			return null;
	}

	public void endConversation() {
		conversing = false;
		speakingTo = null;
		screen.endConversation();
	}

}
