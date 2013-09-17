import java.awt.Point;
import java.io.Serializable;
import java.util.Random;


public class Block implements Serializable{
	private static final long serialVersionUID = -8886614470380749632L;
	private SwootyUtils.BlockType thisType;
	private int parentChunk;
	private int blockID;
	private Point myLocation;
	
	public Block(SwootyUtils.BlockType b, int parent, Point p) {
		thisType = b;
		blockID = new Random().nextInt(9999999);
		parentChunk = parent;
		myLocation = p;
	}
	
	public Point getLocation() {
		return myLocation;
	}
	
	public int getChunkID() {
		return parentChunk;
	}
	
	public int getID() {
		return blockID;
	}
	
	public SwootyUtils.BlockType getType() {
		return thisType;
	}
}
