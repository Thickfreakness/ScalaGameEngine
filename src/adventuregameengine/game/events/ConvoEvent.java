package adventuregameengine.game.events;

import adventuregameengine.game.Game;
import adventuregameengine.world.Actor;

public class ConvoEvent implements GameEvent {

	Game game;
	Actor a;
	boolean started = false;
	
	public ConvoEvent(Game game, Actor a){
		this.game = game;
		this.a = a;
	}
	
	public void processEvent() {
		game.startConversation(a);
		started = true;
	}
	
	public boolean complete(){
		if(!game.isConversing() && started){
			return true;
		}else
			return false;
	}
	
	public boolean running(){
		return started;
	}
}
