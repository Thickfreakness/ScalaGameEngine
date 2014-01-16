package adventuregameengine.graphics.twodee;

import adventuregameengine.graphics.Pixmap;
import android.util.Log;

public class SimpleTextureHolder implements TextureHolder {

	private Pixmap image;
	
	public SimpleTextureHolder(Pixmap bitmap){
		image = bitmap;
	}
	
	public Pixmap getImage() {
		return image;
	}

}
