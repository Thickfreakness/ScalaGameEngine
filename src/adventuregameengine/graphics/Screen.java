package adventuregameengine.graphics;

import adventuregameengine.io.Input.TouchEvent;
import adventuregameengine.starter.GameActivity;
import adventuregameengine.world.Actor;

public abstract class Screen {
    protected final GameActivity game;

    public Screen(GameActivity game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);

    public abstract void present(float deltaTime);

    public abstract void pause();

    public abstract void resume();

    public abstract void dispose();
    
    public abstract void sayThought(String thought);
    
    public abstract void converse(Actor actor, String line);
    
    public abstract void handleEvent(TouchEvent event);
    
    public abstract void startConversation(Actor act);
    
    public abstract void endConversation();
}
