package adventuregameengine.graphics;

import adventuregameengine.graphics.Graphics.PixmapFormat;
import android.graphics.Bitmap;
import android.graphics.Rect;

public interface Pixmap {
    public int getWidth();

    public int getHeight();

    public PixmapFormat getFormat();

    public void dispose();
    
    public Rect getCurrentRect(int i);
    
    public Bitmap getBitmap();
}