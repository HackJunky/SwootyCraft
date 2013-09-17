import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Player implements Serializable{
	private static final long serialVersionUID = -5731298913566439873L;
	private String name;
	private Integer coordX;
	private Integer coordY;
	private double armor;
	private double health;
	private double exp;
	private double hunger;
	private Integer selectedIndex = 0;
	private SwootyUtils.BlockType[] hotbar = new SwootyUtils.BlockType[9];
	private int[] hotbarQTY = new int[9];
	//private Point coordLocation;
	private Rectangle thisViewport;
	private boolean isAlive = false;
	private int direction = 0;

	public Player(String name) {
		this.name = name;
	}

	//Utility
	public void spawn(Point p) {
		coordX = p.x * SwootyUtils.TILE_SIZE_X;
		coordY = p.y * SwootyUtils.TILE_SIZE_Y;
		armor = 10;
		health = 10;
		hunger = 10;
		exp = 0;
		isAlive = true;
	}

	public double getHunger() {
		return hunger;
	}

	public double getXP() {
		return exp;
	}

	public double getArmor() {
		return armor;
	}

	public double getHealth() {
		return health;
	}

	public void takeDamage (int dmgValue) {

	}

	public void die() {
		coordX = null;
		coordY = null;
		isAlive = false;
	}

	public SwootyUtils.BlockType getHotbarByIndex(int i) {
		return hotbar[i];
	}

	public int getHotbarQTYByIndex(int i) {
		return hotbarQTY[i];
	}

	public SwootyUtils.BlockType getSelectedBlock() {
		return hotbar[selectedIndex];
	}

	public int getSelectedBlockQTY() {
		return hotbarQTY[selectedIndex];
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int i) {
		selectedIndex = i;
	}

	public void addToIndex() {
		if (selectedIndex < 8) {
			selectedIndex++;
		}else {
			selectedIndex = 0;
		}
	}

	public void removeFromIndex() {
		if (selectedIndex > 0) {
			selectedIndex--;
		}else {
			selectedIndex = 8;
		}
	}

	public void giveBlock(int id, int qty) {
		int total = qty;
		for (int i = 0; i < hotbar.length; i++) {
			if (hotbar[i] != null && hotbar[i].getID() == id && hotbarQTY[i] < 64) {
				if (total > 64) {
					hotbar[i] = SwootyUtils.getByID(id);
					hotbarQTY[i] = 64;
					total -= (64 - hotbarQTY[i]);
				}else {
					hotbar[i] = SwootyUtils.getByID(id);
					hotbarQTY[i] += total;
					return;
				}
			}else if (hotbar[i] == null) {
				if (total > 64) {
					hotbar[i] = SwootyUtils.getByID(id);
					hotbarQTY[i] = 64;
					total -= 64;
				}else {
					hotbar[i] = SwootyUtils.getByID(id);
					hotbarQTY[i] = total;
					return;
				}
			}
		}
	}

	public void placeBlock() {
		if (hotbarQTY[selectedIndex] > 1) {
			hotbarQTY[selectedIndex]--;
		}else if (hotbarQTY[selectedIndex] < 2) {
			hotbar[selectedIndex] = null;
			hotbarQTY[selectedIndex]--;
		}
	}

	public void setViewport(Rectangle r) {
		thisViewport = r;
	}

	public Rectangle getViewport() {
		return thisViewport;
	}

	//Converters
	public Point convertCoordToBlock(Point p) {
		return new Point(p.x / SwootyUtils.TILE_SIZE_X, p.y / SwootyUtils.TILE_SIZE_Y);
	}

	//Getters
	public int getOccupiedChunk() {
		return (convertCoordToBlock(new Point(coordX, coordY)).x / SwootyUtils.CHUNK_WIDTH);
	}

	public Point getPlayerBlockLocation() {
		return convertCoordToBlock(new Point(coordX, coordY));
	}

	public void setPlayerLocation(Point p) {
		coordX = p.x;
		coordY = p.y;
	}

	public Point getPlayerCoordLocation() {
		return new Point(coordX, coordY);
	}

	public int getDirection() {
		return direction;
	}

	//Name
	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	//Life
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public boolean equals(Object o) {
		if (((Player) o).getName().equals(name)) {
			return true;
		}else {
			return false;
		}
	}
}
