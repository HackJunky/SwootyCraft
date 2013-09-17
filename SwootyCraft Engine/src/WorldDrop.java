import java.awt.Point;
import java.io.Serializable;

public class WorldDrop implements Serializable {
	private static final long serialVersionUID = 9191987546353559128L;
	private Point location;
	private Point offset;
	private SwootyUtils.BlockType[] types;
	int typeIndex;
	private int ID;

	public WorldDrop(SwootyUtils.BlockType b, Point p, int id) {
		location = p;
		offset = new Point(SwootyUtils.TILE_SIZE_X / 4, 0);
		types = new SwootyUtils.BlockType[1000];
		types[0] = convertToBroken(b);
		typeIndex = 1;
		ID = id;
	}

	public SwootyUtils.BlockType convertToBroken(SwootyUtils.BlockType b) {
		switch (b) {
		case STONE:
			return SwootyUtils.BlockType.COBBLESTONE;
		case GRASS:
			return SwootyUtils.BlockType.DIRT;
		}
		return b;
	}

	public void addOffset(Point p) {
		offset = new Point(offset.x + p.x, offset.y + p.y);
		if (offset.y > SwootyUtils.TILE_SIZE_Y) {
			location = new Point(location.x, location.y + 1);
			offset = new Point (offset.x, offset.y - SwootyUtils.TILE_SIZE_Y);
		}else if (offset.y < 0) {
			location = new Point(location.x, location.y - 1);
			offset = new Point (offset.x, offset.y + SwootyUtils.TILE_SIZE_Y);
		}
	}

	public Point getOffset() {
		return offset;
	}

	public void setLocation(Point p) {
		location = p;
	}

	public Point getLocation() {
		return location;
	}

	public int getID() {
		return ID;
	}

	public void addTypes(SwootyUtils.BlockType[] b) {
		for (SwootyUtils.BlockType a : b) {
			if (a != null) {
				types[typeIndex] = a;
				typeIndex++;
			}else {
				return;
			}
		}
	}

	public boolean hasNext() {
		if (typeIndex > 0) {
			return true;
		}
		return false;
	}

	public SwootyUtils.BlockType pickupType() {
		SwootyUtils.BlockType b = types[typeIndex - 1];
		types[typeIndex - 1] = null; 
		typeIndex--;
		return b;
	}

	public SwootyUtils.BlockType[] getTypes() {
		return types;
	}

}
