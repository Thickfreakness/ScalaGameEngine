package adventuregameengine.game;

import java.util.Set;

import adventuregameengine.game.events.GameEvent;
import adventuregameengine.graphics.Screen;
import adventuregameengine.world.Actor;
import adventuregameengine.world.Player;
import adventuregameengine.world.Room;
import adventuregameengine.world.World;


public interface Game {

	public Player getPlayer();
	
	public Screen getCurrentScreen();

	public void resume();

	public void pause();
	
	public void stateChange(World world);
	
	public Room getCurrentRoom();
	
	public void addToEventQueue(GameEvent event);
	
	public World getWorld();
	
	public void sayThought(String text);
	
	public void startConversation(Actor a);
	
	public void endConversation();
	
	public String nextLine();
	
	public String[] convoOptions();
	
	public boolean isConversing();
	
}
