package adventuregameengine.graphics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import adventuregameengine.game.events.ConvoEvent;
import adventuregameengine.game.events.MoveActorEvent;
import adventuregameengine.game.events.ThoughtEvent;
import adventuregameengine.game.events.UseObjectOnEvent;
import adventuregameengine.io.Input.TouchEvent;
import adventuregameengine.starter.GameActivity;
import adventuregameengine.world.Actor;
import adventuregameengine.world.Conversation;
import adventuregameengine.world.Player;
import adventuregameengine.world.Room;
import adventuregameengine.world.RoomObject;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

public class GameScreen extends Screen {

	private Graphics g;
	private Room room;
	private GameActivity activity;
	int i = 0;

	private int[] fbXY = new int[2];
	private int[] worldXY = new int[2];

	private static double scaleX = 1;
	private static double scaleY = 1;
	private static final double Z_SCALE = 0.99;

	private String currentText = "";
	private float textTimer = 0;

	private boolean isUse = false;
	private static final Rect use = new Rect(10, 110, 30, 120);
	private static final Rect look = new Rect(40, 110, 60, 120);

	private boolean inventoryOpen = false;
	private static final Rect inv = new Rect(20, 20, 180, 90);
	private static final Rect invButton = new Rect(70, 110, 85, 120);
	private Map<String, int[]> invLoc = new HashMap<String, int[]>();
	private static final int[] INV_IMAGE_SIZE = { 15, 15 };
	private static final int INV_MARGIN = 5;
	private String pickedUp = "";
	private int zStep = 5;

	private boolean conversation = false;
	private Conversation currentConv = null;
	private static final Rect actLeft = new Rect(5, 5, 100, 60);
	private static final Rect actRight = new Rect(115, 5, 210, 60);
	private static final Rect leftPanel = new Rect(5, 70, 100, 100);
	private static final Rect rightPanel = new Rect(115, 70, 210, 100);
	private float convoTimer = 0;
	private static final float MILISECONDS_PER_WORD = 800000;
	private static final int[][] CONVO_LOCATIONS = { { 10, 70 }, { 10, 78 },
			{ 10, 86 }, { 10, 92 }, { 10, 98 } };
	private static final int CONVO_HEIGHT = 8;
	private static final int CONVO_WIDTH = 80;
	private static final int TEXT_SIZE = 32;

	public GameScreen(GameActivity game, Room room) {
		super(game);
		g = game.getGraphics();
		this.room = room;
		this.activity = game;

		fbXY[0] = g.getWidth();
		fbXY[1] = g.getHeight();

		worldXY[0] = room.getGrid()[0];
		worldXY[1] = room.getGrid()[1];

		scaleX = ((double) fbXY[0]) / ((double) worldXY[0]);
		scaleY = ((double) fbXY[1]) / ((double) worldXY[1]);
	}

	private void initInventoryLocations() {
		Set<RoomObject> inventory = activity.getGame().getPlayer()
				.getInventory();
		if (inventory != null && inventory.size() != 0) {
			int x = inv.left + INV_MARGIN;
			int y = inv.top + INV_MARGIN;
			for (RoomObject ob : inventory) {
				int[] xy = { x, y };
				invLoc.put(ob.getLabel(), xy);
				if (x + INV_IMAGE_SIZE[0] + INV_MARGIN > inv.right) {
					x += INV_IMAGE_SIZE[0] + INV_MARGIN;
					y += INV_IMAGE_SIZE[1] + INV_MARGIN;
				} else {
					x += INV_IMAGE_SIZE[0] + INV_MARGIN;
				}

			}
		}
	}

	// Remember to set vanishing points in room in order to determine z value on
	// touch
	public void handleEvent(TouchEvent event) {
		// Scale FB locations to game world locations
		int x = scaleY(event.x, 0, false);
		int y = scaleY(event.y, 0, false);
		int z = 10 - (int) (getDistFromVPoint(x, y) / zStep);
		if (z < 0)
			z = 0;
//		Log.d("Hit points", "eventx " + event.x + " eventy " + event.y
//				+ " roomx " + x + " roomy " + y);

		if (event.type == TouchEvent.TOUCH_UP) {
			if (conversation) {
				int i = 0;
				for (int[] loc : CONVO_LOCATIONS) {
					if (loc[0] <= x && x <= loc[0] + CONVO_WIDTH && loc[1] <= y
							&& y <= loc[1] + CONVO_HEIGHT) {
						currentConv.chooseConvo(i);
						break;
					}
					i++;
				}
			}
			if (pickedUp != null && !pickedUp.equals("")) {
				if (room.getAtXY(x, y) != null) {
					activity.getGame().addToEventQueue(
							new MoveActorEvent(activity.getGame().getPlayer()
									.getLabel(), activity.getGame(), x, y, z));
					activity.getGame().addToEventQueue(
							new UseObjectOnEvent(room.getAtXY(x, y), activity
									.getGame().getPlayer()
									.popItemFromInventory(pickedUp)));
					pickedUp = "";
					return;
				} else {
					pickedUp = "";
					initInventoryLocations();
				}
			}
			if (inventoryOpen) {
				if ((x <= inv.left || x >= inv.right)
						|| (y <= inv.top || y >= inv.bottom)) {
					inventoryOpen = false;
				}
				return;
			}
			if (!checkButtons(event)) { // Rearrange to add move event
										// regardless (double code etc)
				activity.getGame().addToEventQueue(
						new MoveActorEvent(activity.getGame().getPlayer()
								.getLabel(), activity.getGame(), x, y, z));
				if (room.getAtXY(x, y) != null) {
					if (room.getAtXY(x, y) instanceof Actor
							&& !(room.getAtXY(x, y) instanceof Player)) {
						if (isUse)
							activity.getGame().addToEventQueue(
									new ConvoEvent(activity.getGame(),
											(Actor) room.getAtXY(x, y)));
						else
							activity.getGame().addToEventQueue(
									new ThoughtEvent(room.getAtXY(x, y)
											.getDescription(), activity.getGame()));
					} else if (isUse && room.getAtXY(x, y).action()) { // ****CHANGE
																		// create
																		// a use
																		// event,
																		// this
																		// wont
																		// work
																		// with
																		// event
																		// queue

					} else {
						activity.getGame().addToEventQueue(
								new ThoughtEvent(room.getAtXY(x, y)
										.getDescription(), activity.getGame()));
					}
				}
				return;
			}
		}
		if (event.type == TouchEvent.TOUCH_DRAGGED) {
			if (pickedUp != null && !pickedUp.equals("")) {
				if (inventoryOpen) {
					if ((x <= inv.left || x >= inv.right)
							|| (y <= inv.top || y >= inv.bottom)) {
						inventoryOpen = false;
					}
				}
				int[] xy = { x, y };
				invLoc.put(pickedUp, xy);
			} else {
				for (String label : invLoc.keySet()) {
					int[] xy = invLoc.get(label);
					if (x >= xy[0] && x < (xy[0] + INV_IMAGE_SIZE[0])
							&& y > xy[1] && y < (xy[1] + INV_IMAGE_SIZE[1])) {
						pickedUp = label;
					}
				}
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		// List<TouchEvent> events = activity.getInput().getTouchEvents();
		// synchronized (events) {
		// for (TouchEvent event : events) {
		//
		// }
		// }

	}

	@Override
	public void present(float deltaTime) {
		RoomObject ob = null;
		Pixmap pix = null;
		AnimationPixmap aniPix = null;
		g.clear(Color.BLACK);

		g.drawPixmap(g.getStore().getImage(room.getId()), 0, 0);

		String label = "";
		synchronized (room.getObjects()) {
			for (String key : room.getObjects().keySet()) {
				ob = room.getObjects().get(key);
				double[] pos = ob.getFudgedPosition();
				int z = ob.getZ();
				int x = scaleX(pos[0], z, true);
				int y = scaleY(pos[1], z, true);

				int xSize = scaleX(ob.getxSize(), z, true);
				int ySize = scaleY(ob.getySize(), z, true);

				if (ob instanceof Actor) {
					Actor act = (Actor) ob;
					label = act.getLabel() + act.getAction();
				} else
					label = ob.getState() + ob.getLabel();
				pix = g.getStore().getImage(label);
				if (pix != null) {
					if (pix instanceof AndroidPixmap) {

						g.drawPixmap(pix, x, y, new Rect(x, y, x + xSize, y
								+ ySize));
					} else if (pix instanceof AnimationPixmap) {
						aniPix = (AnimationPixmap) pix;
						aniPix.update(deltaTime);
						g.drawThisPixmap(aniPix, x, y, xSize, ySize,
								aniPix.current);
					}
				} else
					g.drawRect(x, y, xSize, ySize, Color.BLUE);

			}
			if (currentText != null && !currentText.equals("")) {
				g.drawText(currentText, 200, 100, 20, Color.WHITE);
				textTimer = textTimer - deltaTime;
				if (textTimer < 0)
					currentText = "";
			}
		}
		drawButtons();

		if (conversation)
			drawConversationScreen(deltaTime);
		else {
			// Draw Inventory over everything else
			if (inventoryOpen)
				drawInventoryScreen();
			else if (pickedUp != null && !pickedUp.equals("")) { // Not the best
																	// implementation
				g.drawText(pickedUp, scaleX(invLoc.get(pickedUp)[0], 0, true),
						scaleY(invLoc.get(pickedUp)[1], 0, true), 35,
						Color.BLACK);
			}
		}
		g.drawPixel(scaleX(room.getvPoint()[0], 0, true),
				scaleY(room.getvPoint()[1], 0, true), Color.WHITE);

	}

	private void drawInventoryScreen() {
		int left = scaleX(inv.left, 0, true);
		int top = scaleY(inv.top, 0, true);
		int width = scaleX(inv.width(), 0, true);
		int height = scaleY(inv.height(), 0, true);
		g.drawRect(left, top, width, height, Color.GRAY);
		for (RoomObject ob : activity.getGame().getPlayer().getInventory()) {
			String name = ob.getLabel();
			g.drawText(name, scaleX(invLoc.get(name)[0], 0, true),
					scaleY(invLoc.get(name)[1], 0, true), 35, Color.BLACK);
		}
	}

	private void drawConversationScreen(float time) {
		int left = scaleX(actLeft.left, 0, true);
		int top = scaleY(actLeft.top, 0, true);
		int right = scaleX(actLeft.right, 0, true);
		int bottom = scaleY(actLeft.bottom, 0, true);
		Pixmap image = g.getStore().getImage(
				currentConv.getCurrentEffect() + "_" + currentConv.getAct1());

		if (image != null)
			g.drawPixmap(image, left, top, new Rect(left, top, right, bottom));
		else
			g.drawRect(left, top, right - left, bottom - top, Color.YELLOW);

		left = scaleX(actRight.left, 0, true);
		top = scaleY(actRight.top, 0, true);
		right = scaleX(actRight.right, 0, true);
		bottom = scaleY(actRight.bottom, 0, true);

		image = g.getStore().getImage(
				currentConv.getCurrentEffect() + "_" + currentConv.getAct2());
		if (image != null)
			g.drawPixmap(image, left, top, new Rect(left, top, right, bottom));
		else
			g.drawRect(left, top, right - left, bottom - top, Color.YELLOW);

		Rect panel = null;

		left = scaleX(leftPanel.left, 0, true);
		top = scaleY(leftPanel.top, 0, true);
		right = scaleX(leftPanel.right, 0, true);
		bottom = scaleY(leftPanel.bottom, 0, true);

		g.drawRect(left, top, right - left, bottom - top,
				Color.argb(100, 40, 40, 50));

		left = scaleX(rightPanel.left, 0, true);
		top = scaleY(rightPanel.top, 0, true);
		right = scaleX(rightPanel.right, 0, true);
		bottom = scaleY(rightPanel.bottom, 0, true);

		g.drawRect(left, top, right - left, bottom - top,
				Color.argb(100, 40, 40, 50));

		if (currentConv.whosTalking() == null) {
			String[] opts = currentConv.getOptionsAndReset();
			int s = 0;

			for (String op : opts) {
				g.drawText(op, scaleX(CONVO_LOCATIONS[s][0], 0, true),
						scaleY(CONVO_LOCATIONS[s][1], 0, true) + TEXT_SIZE,
						TEXT_SIZE, Color.CYAN);
				s++;
			}
		} else {
			if (currentConv.whosTalking().equalsIgnoreCase("player"))
				panel = leftPanel;
			else
				panel = rightPanel;
			String line = currentConv.nextLine();
			g.drawText(line, scaleX(panel.left, 0, true),
					scaleY(panel.top + 10, 0, true) + TEXT_SIZE, TEXT_SIZE,
					Color.WHITE);
			int count = line.split(" ").length;
			convoTimer += time;
			if (convoTimer > MILISECONDS_PER_WORD * count) {
				currentConv.incrementConvo();
				convoTimer = 0;
			}
		}
	}

	public void startConversation(Actor act) {
		conversation = true;
		currentConv = act.getConvo();
		// convoTimer = 0;
	}

	public void endConversation() {
		conversation = false;
		currentConv = null;
	}

	/**
	 * Returns true if a button has been pushed, false otherwise
	 * 
	 * @param event
	 * @return
	 */
	private boolean checkButtons(TouchEvent event) {
		int x = scaleX(event.x, 0, false); // Scale screen location to in world
											// location (used for buttons)
		int y = scaleY(event.y, 0, false);

		if (x >= use.left && x <= use.right && y >= use.top && y <= use.bottom) {
			isUse = true;
			return true;
		} else if (x >= look.left && x <= look.right && y >= look.top
				&& y <= look.bottom) {
			isUse = false;
			return true;
		} else if (x >= invButton.left && x <= invButton.right
				&& y >= invButton.top && y <= invButton.bottom) {
			inventoryOpen = true;
			initInventoryLocations();
			return true;
		}

		return false;
	}

	private void drawButtons() {
		int usebk = 0;
		int usetxt = 0;
		int lookbk = 0;
		int looktxt = 0;

		if (isUse) {
			usebk = Color.RED;
			usetxt = Color.WHITE;
			lookbk = Color.YELLOW;
			looktxt = Color.BLACK;
		} else {
			usebk = Color.YELLOW;
			usetxt = Color.BLACK;
			lookbk = Color.RED;
			looktxt = Color.WHITE;
		}

		g.drawRect(scaleX(use.left, 0, true), scaleY(use.top, 0, true),
				scaleX(use.width(), 0, true), scaleY(use.height(), 0, true),
				usebk);
		g.drawText("Use", scaleX(use.left, 0, true),
				scaleY(use.top + use.height() / 2, 0, true), 20, usetxt);
		g.drawRect(scaleX(look.left, 0, true), scaleY(look.top, 0, true),
				scaleX(look.width(), 0, true), scaleY(look.height(), 0, true),
				lookbk);
		g.drawText("Look", scaleX(look.left, 0, true),
				scaleY(look.top + look.height() / 2, 0, true), 20, looktxt);
		g.drawRect(scaleX(invButton.left, 0, true),
				scaleY(invButton.top, 0, true),
				scaleX(invButton.width(), 0, true),
				scaleY(invButton.height(), 0, true), Color.GRAY);
		g.drawText("Inventory", scaleX(invButton.left, 0, true),
				scaleY(invButton.top + invButton.height() / 2, 0, true), 20,
				Color.BLACK);

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sayThought(String text) {
		currentText = text;
		textTimer = 10000000;
	}

	@Override
	public void converse(Actor actor, String line) {
		// TODO Auto-generated method stub

	}

	public int scaleX(double value, double zDepth, boolean toFB) {
		double holder = 0;
		double zVal = 0;
		zDepth = 0;
		if (toFB) {
			holder = (value * scaleX);
			zVal = holder;
			for (int i = 0; i < zDepth; i++) {
				zVal *= Z_SCALE;
			}
			if (value < room.getvPoint()[0])
				holder += (holder - zVal);
			else
				holder = zVal;
		} else {
			holder = (value / scaleX);
			zVal = holder;
			for (int i = 0; i < zDepth; i++) {
				zVal /= Z_SCALE;
			}
			if (holder < room.getvPoint()[0])
				holder += (holder - zVal);
			else
				holder = zVal;
		}
		return (int) holder;
	}

	public int scaleY(double value, double zDepth, boolean toFB) {
		double holder = 0;
		double zVal = 0;
		zDepth = 0;
		if (toFB) {
			holder = (value * scaleY);
			zVal = holder;
			for (int i = 0; i < zDepth; i++) {
				zVal *= Z_SCALE;
			}
			if (value < room.getvPoint()[1])
				holder += (holder - zVal);
			else
				holder = zVal;
		} else {
			holder = (value / scaleY);
			zVal = holder;
			for (int i = 0; i < zDepth; i++) {
				zVal /= Z_SCALE;
			}
			if (holder < room.getvPoint()[1])
				holder += (holder - zVal);
			else
				holder = zVal;
		}
		return (int) holder;
	}

	private int getDistFromVPoint(int x, int y) {
		x = (x - room.getvPoint()[0]) * (x - room.getvPoint()[0]);
		y = (y - room.getvPoint()[1]) * (y - room.getvPoint()[1]);

		return (int) Math.sqrt(x + y);
	}

}
