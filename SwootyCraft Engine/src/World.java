import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;

public class World implements Serializable{
	private static final long serialVersionUID = -6007976534678217634L;
	private Chunk[] worldData;
	private ArrayList<Block> queuedChanges;
	private ArrayList<WorldDrop> queuedDrops;
	private Rectangle blockViewport;
	private Rectangle coordViewport;
	private Point currentBlock;
	private WorldTick worldEvents;
	private Timer worldTick;
	private ArrayList<Player> players;
	private Player thisPlayer;
	private boolean isNet;
	private boolean isServer;
	private int dayCycle;
	private boolean isDay;
	private float worldTime;
	private int iterationsPT;
	private String worldName;

	public World(boolean isNet, boolean isServer, String playerName, boolean isLoaded, String worldName) {
		players = new ArrayList<Player>();
		players.add(thisPlayer = new Player(playerName));
		queuedChanges = new ArrayList<Block>();
		queuedDrops = new ArrayList<WorldDrop>();
		worldData = new Chunk[SwootyUtils.CHUNK_QTY];
		this.worldName = worldName; 
		this.isNet = isNet;
		this.isServer = isServer;
		if (!isNet && !isLoaded || isServer) {
			generateWorld();
			SwootyUtils.log("World", "Building Gamespace terrain...");
		}else {
			if (isLoaded) {
				SwootyUtils.log("World", "Loading save to Gamespace...");
				loadFromName();
			}else {
				SwootyUtils.log("World", "Error: Game state is null!");
			}
		}
		worldTick = new Timer(50, worldEvents = new WorldTick());
		worldTick.start();
	}

	public void loadFromName() {
		try {
			File file = new File(worldName);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;

				String[] chunkData = new String[SwootyUtils.CHUNK_QTY];
				int index = 0;

				while ((line = br.readLine()) != null) {
					if (!line.startsWith("--")) {
						chunkData[index] = line;
						index++;
					}
				}
				SwootyUtils.log("World", "Loaded " + index + " saved chunks from saves/" + worldName + "/" + worldName + ".save...");
				br.close();
				generateFromFile(chunkData);
			}else {
				SwootyUtils.log("World", "No valid world file found. Resuming terrain generation...");
				generateWorld();
			}
		}catch (Exception f) {
			f.printStackTrace();
		}
	}

	public void generateFromFile(String[] data) {
		int chunk = 0;
		SwootyUtils.log("World", "Simulating chunks for a bit...");
		Chunk[] world = new Chunk[SwootyUtils.CHUNK_QTY];
		for (String s : data) {
			Block[][] chunkData = new Block[SwootyUtils.CHUNK_WIDTH][SwootyUtils.CHUNK_HEIGHT];
			String g[] = s.split(",");
			int x = 0;
			int y = 0;
			for (String block : g) {
				chunkData[x][y] = new Block(SwootyUtils.getByID(Integer.valueOf(block)), chunk, new Point(x, y));
				if (y < SwootyUtils.CHUNK_HEIGHT - 1) {
					y++;
				}else {
					y = 0;
					x++;
				}
			}
			Chunk c = new Chunk(chunk, true);
			c.setData(chunkData);
			world[chunk] = c;
			chunk++;
		}
		setWorld(world);
		SwootyUtils.log("World", "Loaded " + chunk + " unique chunks to Gamespace. World ready.");
	}

	//Player
	public void spawn() {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (!p.isAlive()) {
				boolean spawned = false;
				for (int x = new Random().nextInt(SwootyUtils.CHUNK_WIDTH * SwootyUtils.CHUNK_QTY - 1) + SwootyUtils.CHUNK_WIDTH; x < SwootyUtils.CHUNK_WIDTH * SwootyUtils.CHUNK_QTY; x++) {
					for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
						int chunk = x / SwootyUtils.CHUNK_WIDTH;
						if (worldData[chunk].getBlocks()[x - (SwootyUtils.CHUNK_WIDTH * chunk)][y].getType() == SwootyUtils.BlockType.GRASS && !spawned) {
							p.spawn(new Point(x, y - 2));
							p.setViewport(getViewCentered(new Point(x, y - 2)));
							if (p.getViewport() != null) {
								setPlayerCentered(p);
							}
							SwootyUtils.log("World", "Spawing " + p.getName() + " at chunk " + chunk + " at coordinate (" + (x - (SwootyUtils.CHUNK_WIDTH * chunk)) + ", " + (y - 2) + ").");
							spawned = true;
						}
						if (spawned) {
							break;
						}
					}
					if (spawned) {
						break;
					}
				}
			}
		}
	}

	public boolean updatePlayer(Player e) {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.getName().equals(e.getName())) {
				players.remove(i);
				players.add(e);
				return true;
			}
		}
		players.add(e);
		SwootyUtils.log("Network -> World", "Added new player to userspace: " + e.getName());
		return false;
	}

	public Player getMyPlayer() {
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getName().equals(thisPlayer.getName())) {
				return players.get(i);
			}
		}
		return null;
	}

	public void setPlayerCentered(Player p) {
		setBlockCentered(p.getPlayerBlockLocation());
		currentBlock = p.getPlayerBlockLocation();
	}

	public void setPlayers(ArrayList<Player> p) {
		for (Player a : p) {
			if (a.getName().equals(getMyPlayer().getName())) {
				thisPlayer = a;
			}
		}
		players = p;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void movePlayerLeft() {
		if (getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH) > 0) {
			if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][getMyPlayer().getPlayerBlockLocation().y].getType() == SwootyUtils.BlockType.AIR) { 
				if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][getMyPlayer().getPlayerBlockLocation().y + 1].getType() == SwootyUtils.BlockType.AIR) {
					getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x - (SwootyUtils.TILE_SIZE_X / 2), getMyPlayer().getPlayerCoordLocation().y));
				}
			}
		}else {
			if (worldData[getMyPlayer().getOccupiedChunk() - 1].getBlocks()[SwootyUtils.CHUNK_WIDTH - 1][getMyPlayer().getPlayerBlockLocation().y].getType() == SwootyUtils.BlockType.AIR) {
				if (worldData[getMyPlayer().getOccupiedChunk() - 1].getBlocks()[SwootyUtils.CHUNK_WIDTH - 1][getMyPlayer().getPlayerBlockLocation().y + 1].getType() == SwootyUtils.BlockType.AIR) {
					getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x - (SwootyUtils.TILE_SIZE_X / 2), getMyPlayer().getPlayerCoordLocation().y));
				}
			}
		}
	}

	public void movePlayerRight() {
		if (getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH) + 1 < 64) {
			if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH) + 1][getMyPlayer().getPlayerBlockLocation().y].getType() == SwootyUtils.BlockType.AIR) {
				if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH) + 1][getMyPlayer().getPlayerBlockLocation().y + 1].getType() == SwootyUtils.BlockType.AIR) {
					getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x + (SwootyUtils.TILE_SIZE_X / 2), getMyPlayer().getPlayerCoordLocation().y));
				}
			}
		}else {
			if (worldData[getMyPlayer().getOccupiedChunk() + 1].getBlocks()[0][getMyPlayer().getPlayerBlockLocation().y].getType() == SwootyUtils.BlockType.AIR) {
				if (worldData[getMyPlayer().getOccupiedChunk() + 1].getBlocks()[0][getMyPlayer().getPlayerBlockLocation().y + 1].getType() == SwootyUtils.BlockType.AIR) { 
					getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x + (SwootyUtils.TILE_SIZE_X / 2), getMyPlayer().getPlayerCoordLocation().y));
				}
			}
		}
	}

	public void playerJump() {
		worldEvents.doJump();
	}

	//Chunk and World
	public void setIndex(int i, Chunk c) {
		worldData[i] = c;
	}

	public void setWorld(Chunk[] world) {
		worldData = world;
	}

	public void generateWorld() {
		SwootyUtils.log("World", "Generating World terrain...");
		for (int i = 0; i < SwootyUtils.CHUNK_QTY; i++) {
			worldData[i] = new Chunk(i, false);
		}
		worldTime = 0;
		dayCycle = 0;
		isDay = true;
	}

	public void removeBlock(Point p) {
		Point block = p;
		int chunk = 0;
		chunk = block.x / SwootyUtils.CHUNK_WIDTH;
		int x = block.x - (chunk * SwootyUtils.CHUNK_WIDTH);
		int y = block.y;
		block = new Point(x, y);
		if (block.y > 0 && block.y <= SwootyUtils.CHUNK_HEIGHT) {
			if (worldData[0] != null) {
				if (worldData[chunk].getBlocks()[block.x][block.y].getType() != SwootyUtils.BlockType.AIR) {
					SwootyUtils.log("World", "Removing " + worldData[chunk].getBlocks()[block.x][block.y].getType() + " at position (" + block.x + ", " + block.y + ") from chunk " + chunk + ".");
					worldData[chunk].removeBlock(new Point(block.x, block.y));
				}
			}
		}
	}

	public void placeBlock(Point p, SwootyUtils.BlockType b) {
		try {
			Point block = p;
			int chunk = 0;
			chunk = block.x / SwootyUtils.CHUNK_WIDTH;
			int x = block.x - (chunk * SwootyUtils.CHUNK_WIDTH);
			int y = block.y;
			block = new Point(x, y);
			if (block.y > 0 && block.y <= SwootyUtils.CHUNK_HEIGHT) {
				if (worldData[0] != null) {
					if (worldData[chunk].getBlocks()[block.x][block.y].getType() == SwootyUtils.BlockType.AIR) {
						worldData[chunk].placeBlock(new Point(block.x, block.y), b);
						SwootyUtils.log("World", "Placing " + worldData[chunk].getBlocks()[block.x][block.y].getType() + " at position (" + block.x + ", " + block.y + ") in chunk " + chunk + ".");
						getMyPlayer().placeBlock();
					}
				}
			}
		}catch (Exception e) {

		}
	}

	public Chunk[] getChunks() {
		return worldData;
	}

	public Block getBlock(Point p) {
		Point block = p;
		int chunk = 0;
		if (block.x < 0) {
			chunk = SwootyUtils.CHUNK_QTY - (block.x / SwootyUtils.CHUNK_WIDTH) - 1;
			int x = block.x + SwootyUtils.CHUNK_WIDTH; 
			int y = block.y;
			block = new Point(x, y);
		}else if (block.x >= SwootyUtils.CHUNK_QTY * SwootyUtils.CHUNK_WIDTH) {

		}else{
			chunk = block.x / SwootyUtils.CHUNK_WIDTH;
			int x = block.x - (chunk * SwootyUtils.CHUNK_WIDTH);
			int y = block.y;
			block = new Point(x, y);
		}
		if (block.y < 0) {
			block = new Point(block.x, 0);
		}else if (block.y >= SwootyUtils.CHUNK_HEIGHT) {
			return null;
		}
		if (worldData[0] != null) {
			return worldData[chunk].getBlocks()[block.x][block.y];
		}else {
			return null;
		}
	}

	public WorldDrop getDrop(Point p) {
		Point block = p;
		int chunk = 0;
		if (block.x < 0) {
			chunk = SwootyUtils.CHUNK_QTY - (block.x / SwootyUtils.CHUNK_WIDTH) - 1;
			int x = block.x + SwootyUtils.CHUNK_WIDTH; 
			int y = block.y;
			block = new Point(x, y);
		}else if (block.x >= SwootyUtils.CHUNK_QTY * SwootyUtils.CHUNK_WIDTH) {

		}else{
			chunk = block.x / SwootyUtils.CHUNK_WIDTH;
			int x = block.x - (chunk * SwootyUtils.CHUNK_WIDTH);
			int y = block.y;
			block = new Point(x, y);
		}
		if (block.y < 0) {
			block = new Point(block.x, 0);
		}else if (block.y >= SwootyUtils.CHUNK_HEIGHT) {
			return null;
		}
		if (worldData[0] != null) {
			return worldData[chunk].getDropData()[block.x][block.y];
		}else {
			return null;
		}
	}


	//Network
	public boolean hasChanges() {
		if (queuedChanges.size() == 0) {
			return false;
		}else {
			return true;
		}
	}

	public ArrayList<Block> getQueue() {
		for (int i = 0; i < SwootyUtils.CHUNK_QTY; i++) {
			for (Block b : worldData[i].getChanges()) {
				queuedChanges.add(b);
			}
		}
		ArrayList<Block> queue = queuedChanges;
		queuedChanges = new ArrayList<Block>();
		return queue;
	}

	public int getQueueSize() {
		return queuedChanges.size();
	}

	public void updateWorld(ArrayList<Block> update) {
		for (Block b : update) {
			worldData[b.getChunkID()].updateBlock(b);
		}
	}

	public void updateWorldDrops(ArrayList<WorldDrop> update) {
		for (WorldDrop p : update) {
			worldData[p.getID()].updateDrop(p);
		}
	}

	public ArrayList<WorldDrop> getDrops() {
		for (int i = 0; i < SwootyUtils.CHUNK_QTY; i++) {
			for (WorldDrop b : worldData[i].getDrops()) {
				queuedDrops.add(b);
			}
		}
		ArrayList<WorldDrop> queue = queuedDrops;
		queuedDrops = new ArrayList<WorldDrop>();
		return queue;
	}

	//Viewport
	public Rectangle getBlockViewport() {
		return blockViewport;
	}

	public void moveViewLeft() {
		//coordViewport = new Rectangle(coordViewport.x - (SwootyUtils.TILE_SIZE_X / 8), coordViewport.y, coordViewport.width - (SwootyUtils.TILE_SIZE_X / 8), coordViewport.height);
		if (blockViewport.x - 1 > 0) {
			setBlockCentered(new Point (currentBlock.x - 1, currentBlock.y));
		}
	}

	public void moveViewRight() {
		//coordViewport = new Rectangle(coordViewport.x + (SwootyUtils.TILE_SIZE_X / 8), coordViewport.y, coordViewport.width + (SwootyUtils.TILE_SIZE_X / 8), coordViewport.height);
		if (blockViewport.x + 1 < SwootyUtils.CHUNK_WIDTH * SwootyUtils.CHUNK_QTY) {
			setBlockCentered(new Point (currentBlock.x + 1, currentBlock.y));
		}
	}

	public void moveViewUp() {
		//coordViewport = new Rectangle(coordViewport.x, coordViewport.y - (SwootyUtils.TILE_SIZE_Y / 2), coordViewport.width, coordViewport.height - (SwootyUtils.TILE_SIZE_Y / 2));
		setBlockCentered(new Point (currentBlock.x, currentBlock.y - 1));
	}

	public void moveViewDown() {
		//coordViewport = new Rectangle(coordViewport.x, coordViewport.y + (SwootyUtils.TILE_SIZE_Y / 2), coordViewport.width, coordViewport.height + (SwootyUtils.TILE_SIZE_Y / 2));
		setBlockCentered(new Point (currentBlock.x, currentBlock.y + 1));
	}

	public void setBlockCentered(Point q) {
		blockViewport = new Rectangle(q.x - (SwootyUtils.VIEWPORT_TILES_X / 2), q.y - (SwootyUtils.VIEWPORT_TILES_Y / 2), q.x - (SwootyUtils.VIEWPORT_TILES_X / 2) + SwootyUtils.VIEWPORT_TILES_X, q.y - (SwootyUtils.VIEWPORT_TILES_Y / 2) + SwootyUtils.VIEWPORT_TILES_Y);
		//setCoordCentered();
		currentBlock = q;
	}

	public Rectangle getViewCentered(Point q) {
		return new Rectangle(q.x - (SwootyUtils.VIEWPORT_TILES_X / 2), q.y - (SwootyUtils.VIEWPORT_TILES_Y / 2), q.x - (SwootyUtils.VIEWPORT_TILES_X / 2) + SwootyUtils.VIEWPORT_TILES_X, q.y - (SwootyUtils.VIEWPORT_TILES_Y / 2) + SwootyUtils.VIEWPORT_TILES_Y);
	}

	public void setCoordCentered() {
		coordViewport = new Rectangle(blockViewport.x * SwootyUtils.TILE_SIZE_X, blockViewport.y * SwootyUtils.TILE_SIZE_Y, blockViewport.width * SwootyUtils.TILE_SIZE_X, blockViewport.height * SwootyUtils.TILE_SIZE_Y);  
	}

	public Rectangle getCoordViewport() {
		return coordViewport;
	}

	public float getWorldTime() {
		return worldTime;
	}

	public void setWorldTime(float f, boolean verbose) {
		if (verbose) {
			SwootyUtils.log("World", "Setting world time to " + (f * 100));
			worldTime = f;
		}else {
			worldTime = f;
		}
	}

	public int getIterations() {
		return iterationsPT;
	}

	public int getWorldCycle() {
		return dayCycle;
	}

	public String[] dumpRegions() {
		String[] dump = new String[SwootyUtils.CHUNK_QTY];
		int index = 0;
		for (int i = 0; i < SwootyUtils.CHUNK_QTY; i++) {
			dump[index] = worldData[i].dumpRegion();
			index++;
		}
		return dump;
	}

	public class WorldTick implements ActionListener {
		private boolean lastPlayerState = false;
		private boolean isJumping = false;
		private boolean isJumpApex = false;
		private int pixelsToApex = 0; 

		@Override
		public void actionPerformed(ActionEvent arg0) {
			iterationsPT = 0;
			try {
				for (int i = 0; i < players.size(); i++) {
					Player p = players.get(i);
					Chunk c = worldData[p.getOccupiedChunk()];
					if (p.isAlive() && p.getPlayerCoordLocation() != null && !isJumping) {
						if (c.getBlocks()[p.getPlayerBlockLocation().x - (p.getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][p.getPlayerBlockLocation().y + 2].getType() == SwootyUtils.BlockType.AIR) {
							p.setPlayerLocation(new Point(p.getPlayerCoordLocation().x, p.getPlayerCoordLocation().y + SwootyUtils.GRAVITY));
							iterationsPT++;
						}
					}
					if (c != null) {
						for (int x = SwootyUtils.CHUNK_WIDTH - 1; x >= 0; x--) {
							for (int y = SwootyUtils.CHUNK_HEIGHT - 1; y >= 0; y--) {
								if (c.getDropData()[x][y] != null && c.getBlocks()[x][y + 1].getType() == SwootyUtils.BlockType.AIR) {
									WorldDrop d = c.getDropData()[x][y];
									d.addOffset(new Point(0, (SwootyUtils.TILE_SIZE_Y / 4)));
									iterationsPT++;
								}else {
									if (c.getDropData()[x][y] != null && c.getDropData()[x][y].getOffset().y < (SwootyUtils.TILE_SIZE_Y / 2) - 1) {
										WorldDrop d = c.getDropData()[x][y];
										d.addOffset(new Point(0, (SwootyUtils.TILE_SIZE_Y / 8)));
										iterationsPT++;
									}
								}
								if (c.getDropData()[x][y] != null && !c.getDropData()[x][y].getLocation().equals(new Point(x, y))) {
									WorldDrop d = c.getDropData()[x][y];
									if (c.getDropData()[d.getLocation().x][d.getLocation().y] != null) {
										c.getDropData()[d.getLocation().x][d.getLocation().y].addTypes(d.getTypes());
										SwootyUtils.log("World", "Catalogged a drop moving ontop of another drop.");
										c.getDropData()[x][y] = null;
										iterationsPT++;
									}else {
										c.getDropData()[x][y] = null;
										c.getDropData()[d.getLocation().x][d.getLocation().y] = d;
										iterationsPT++;
									}
								}
							}
						}
						WorldDrop d = c.getDropData()[p.getPlayerBlockLocation().x - (p.getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][p.getPlayerBlockLocation().y + 1];
						if (d != null && p != null) {
							if (d.hasNext()) {
								p.giveBlock(d.pickupType().getID(), 1);
								iterationsPT++;
							}else {
								c.getDropData()[p.getPlayerBlockLocation().x - (p.getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][p.getPlayerBlockLocation().y + 1] = null;
								iterationsPT++;
							}
						}
					}
				}
				if (isJumping) {
					iterateJump();
					iterationsPT++;
				}
			}catch (Exception e) {
				//e.printStackTrace();
			}
			try {
				if (!isNet || isNet && isServer) {
					float increment = SwootyUtils.WORLD_TIME_CAP / 10000;
					if (!isDay) {
						if (dayCycle > -SwootyUtils.WORLD_TIME_CAP) {
							dayCycle--;
							if (worldTime < 0.9f) {
								worldTime += 0.005f;
							}
						}else {
							isDay = !isDay;
							SwootyUtils.log("World", "Day time returns...");
						}
					}else {
						if (dayCycle < SwootyUtils.WORLD_TIME_CAP) {
							dayCycle++;
							if (worldTime > 0) {
								worldTime -= 0.005f;
							}
						}else {
							isDay = !isDay;
							SwootyUtils.log("World", "Night time approaches...");
						}
					}
				}
			}catch (Exception e) {

			}
		}

		public void doJump() {
			if (!isJumping) {
				isJumping = true;
				isJumpApex = false;
				pixelsToApex = (int)(SwootyUtils.TILE_SIZE_X);
			}
		}

		public void iterateJump() {
			if (isJumping) {
				if (isJumpApex) {
					if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][getMyPlayer().getPlayerBlockLocation().y + 2].getType() == SwootyUtils.BlockType.AIR) {
						getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x, getMyPlayer().getPlayerCoordLocation().y + SwootyUtils.GRAVITY));
					}else {
						isJumping = false;
					}
				}else {
					if (worldData[getMyPlayer().getOccupiedChunk()].getBlocks()[getMyPlayer().getPlayerBlockLocation().x - (getMyPlayer().getOccupiedChunk() * SwootyUtils.CHUNK_WIDTH)][getMyPlayer().getPlayerBlockLocation().y - 1].getType() == SwootyUtils.BlockType.AIR) {
						int increase = getMyPlayer().getPlayerCoordLocation().y - (SwootyUtils.GRAVITY);
						getMyPlayer().setPlayerLocation(new Point(getMyPlayer().getPlayerCoordLocation().x, increase));
						pixelsToApex -= SwootyUtils.GRAVITY;
						if (pixelsToApex <= 0) {
							isJumpApex = true;
						}
					}
				}
			}
		}
	}
}
