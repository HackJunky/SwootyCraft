import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


public class Chunk implements Serializable{
	private static final long serialVersionUID = 9219233697653745771L;
	private Block[][] chunkData;
	private WorldDrop[][] dropData;
	private int ID = -1;
	private ArrayList<Block> queuedChanges;
	private ArrayList<WorldDrop> queuedDrops;

	public Chunk(int id, boolean loaded) {
		ID = id;
		chunkData = new Block[SwootyUtils.CHUNK_WIDTH][SwootyUtils.CHUNK_HEIGHT];
		dropData = new WorldDrop[SwootyUtils.CHUNK_WIDTH][SwootyUtils.CHUNK_HEIGHT];
		queuedDrops = new ArrayList<WorldDrop>();
		queuedChanges = new ArrayList<Block>();
		if (!loaded) {
			generateTerrain();
		}
	}

	public void setData(Block[][] b) {
		chunkData = b;
	}
	
	public void generateTerrain() {
		parseAir();
		parseBasic();
		parseOre();
		//		parseWater();
		parseStructures();
		//		parseLava();
	}

	public void parseAir() {
		for (int x = 0; x < SwootyUtils.CHUNK_WIDTH; x++) {
			for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				chunkData[x][y] = new Block(SwootyUtils.BlockType.AIR, ID, new Point(x, y));
			}
		}
	}

	public int getID() {
		return ID;
	}

	public void parseBasic() {
		int terrainHeight = (int)(SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR));
		for (int x = 0; x < SwootyUtils.CHUNK_WIDTH; x++) {
			for (int y = terrainHeight; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				chunkData[x][y] = new Block(SwootyUtils.BlockType.STONE, ID, new Point(x, y));
			}
		}

		//Randomize Terrain
		boolean rise = false;
		for (int x = 0; x < SwootyUtils.CHUNK_WIDTH; x++) {
			if (rise) {
				terrainHeight += new Random().nextDouble() * 1.3;
			}else{
				terrainHeight -= new Random().nextDouble() * 1.3;
			}
			for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				int height = new Random().nextInt(2) + 2;
				if (y < terrainHeight) {	
					chunkData[x][y] = new Block(SwootyUtils.BlockType.AIR, ID, new Point(x, y));
				}else if (y == terrainHeight) {
					chunkData[x][y] = new Block(SwootyUtils.BlockType.GRASS, ID, new Point(x, y));
				}else if (y < terrainHeight + (new Random().nextInt(height) + 8)) {
					chunkData[x][y] = new Block(SwootyUtils.BlockType.DIRT, ID, new Point(x, y));
				}else if (y > SwootyUtils.CHUNK_HEIGHT - 2){
					chunkData[x][y] = new Block(SwootyUtils.BlockType.BEDROCK, ID, new Point(x, y));
				}

			}
			if (terrainHeight - (int)(SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR)) > new Random().nextInt(6)) {
				rise = false;
			}else if (terrainHeight - (int)(SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR)) < new Random().nextInt(6) * -1) {
				rise = true;
			}
		}
	}

	public void parseOre() {
		int oreSize = new Random().nextInt(SwootyUtils.ORE_CLUSTER_SIZE_MAX) + SwootyUtils.ORE_CLUSTER_SIZE_MIN;
		int[][] oreMap = new int[oreSize][oreSize];
		for (int y = (int)(SwootyUtils.CHUNK_HEIGHT * (1 - SwootyUtils.CHUNK_PERCENT_AIR) + 10); y < SwootyUtils.CHUNK_HEIGHT - oreSize; y += oreSize * new Random().nextInt(6)) {
			for (int x = oreSize + new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x < SwootyUtils.CHUNK_WIDTH - oreSize - new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x += oreSize * new Random().nextInt(6)) {
				int theChosenOre = new Random().nextInt(50);
				for (int a = 0; a < oreSize; a++) {
					oreMap[a] = SwootyUtils.ORE_CLUSTER_PATTERNS[new Random().nextInt(SwootyUtils.ORE_CLUSTER_PATTERNS.length)];
				}
				for (int a = 0; a < oreSize; a++) {
					for (int b = 0; b < oreSize; b++) {
						int newX = x + a;
						int newY = y + b;
						if (newX > SwootyUtils.CHUNK_WIDTH) {
							break;
						}
						if (newY > SwootyUtils.CHUNK_HEIGHT) {
							newY = SwootyUtils.CHUNK_HEIGHT - 1;
						}
						try {
							if (chunkData[newX][newY].getType() == SwootyUtils.BlockType.STONE) {
								if (oreMap[a][b] == 1) {
									if (theChosenOre <= 10) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.COAL_ORE, ID, new Point(newX, newY));
									}else if (theChosenOre <= 20) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.IRON_ORE, ID, new Point(newX, newY));
									}else if (theChosenOre <= 25) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.REDSTONE_ORE, ID, new Point(newX, newY));
									}else if (theChosenOre <= 28) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.GOLD_ORE, ID, new Point(newX, newY));
									}else if (theChosenOre <= 35) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.LAPIZ_ORE, ID, new Point(newX, newY));
									}else if (theChosenOre <= 37) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.DIAMOND_ORE, ID, new Point(newX, newY));
									}
								}
							}
						}catch (Exception e) {

						}
					}
				}
			}
		}
	}

	public void parseStructures() {
		//Trees
		for (int x = SwootyUtils.TREE_PATTERN[0].length; x < SwootyUtils.CHUNK_WIDTH - SwootyUtils.TREE_PATTERN[0].length; x+=SwootyUtils.TREE_PATTERN[0].length) {
			for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS) {
					if (new Random().nextInt(100) < 20) {
						for (int a = 0; a < SwootyUtils.TREE_PATTERN.length; a++) {
							for (int b = 0; b < SwootyUtils.TREE_PATTERN[a].length; b++) {
								try {
									int newX = x + b - (SwootyUtils.TREE_PATTERN[a].length / 2);
									int newY = y - a - 1;
									if (newX < 0 || newX >= SwootyUtils.CHUNK_WIDTH) {
										break;
									}
									if (newY < 0 || newY >= SwootyUtils.CHUNK_HEIGHT) {
										break;
									}
									if (SwootyUtils.TREE_PATTERN[a][b] == 0) {
										//chunkData[newX][newY] = new Block(SwootyUtils.BlockType.AIR, ID);
									}else if (SwootyUtils.TREE_PATTERN[a][b] == 1) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.LOG, ID, new Point(newX, newY));
									}else if (SwootyUtils.TREE_PATTERN[a][b] == 2) {
										chunkData[newX][newY] = new Block(SwootyUtils.BlockType.LEAVES, ID, new Point(newX, newY));
									}
								}catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						chunkData[x][y] = new Block(SwootyUtils.BlockType.DIRT, ID, new Point(x, y));
					}
				}
			}
		}

		//Dungeons

	}

	public void parseWater() {
		//Water
		try {
			int startX = new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 2);
			int endX = -1;
			int startHeight = -1;
			for (int x = 0; x < SwootyUtils.CHUNK_WIDTH; x++) {
				for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
					if (startHeight > 0 && y == startHeight) {
						if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS) {
							endX = x;
							break;
						}
					}
					if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS && startHeight < 0) {
						startHeight = y;
					}
				}
			}
			int water = 0;
			if (startX > 0 && startX < SwootyUtils.CHUNK_WIDTH && endX > 0 && endX < SwootyUtils.CHUNK_WIDTH - 4 && endX > startX) {
				for (int x = startX; startX < endX; x++) {
					for (int y = startHeight; y < SwootyUtils.CHUNK_HEIGHT; y++) {
						if (chunkData[x][y].getType() == SwootyUtils.BlockType.AIR) {
							water++;
						}
					}
				}
			}
			if (startX > 0 && startX < SwootyUtils.CHUNK_WIDTH && endX > 0 && endX < SwootyUtils.CHUNK_WIDTH - 4 && endX > startX && water > (endX - startX)) {
				for (int x = startX; startX < endX; x++) {
					for (int y = startHeight; y < SwootyUtils.CHUNK_HEIGHT; y++) {
						if (chunkData[x][y].getType() == SwootyUtils.BlockType.AIR) {
							chunkData[x][y] = new Block(SwootyUtils.BlockType.WATER, ID, new Point(x, y));
						}else if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS) {
							chunkData[x][y] = new Block(SwootyUtils.BlockType.SAND, ID, new Point(x, y));
						}
					}
				}
			}
		}catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void parseLava() {
		
	}

	public void removeBlock(Point p) {
		queuedDrops.add(new WorldDrop(chunkData[p.x][p.y].getType(), p, ID));
		dropData[p.x][p.y] = new WorldDrop(chunkData[p.x][p.y].getType(), p, ID);
		chunkData[p.x][p.y] = new Block(SwootyUtils.BlockType.AIR, ID, p);
		queuedChanges.add(new Block(SwootyUtils.BlockType.AIR, ID, p));
	}

	public void placeBlock(Point p, SwootyUtils.BlockType b) {
		chunkData[p.x][p.y] = new Block(b, ID, p);
		queuedChanges.add(new Block(b, ID, p));
	}

	public ArrayList<Block> getChanges() {
		ArrayList<Block> queue = queuedChanges;
		queuedChanges = new ArrayList<Block>();
		return queue;
	}

	public ArrayList<WorldDrop> getDrops() {
		ArrayList<WorldDrop> data = queuedDrops;
		queuedDrops = new ArrayList<WorldDrop>();
		return data;
	}
	
	public String dumpRegion() {
		String begin = "-- BEGIN: SwootyUtils Chunk Region " + ID;
		String end = "-- END: SwootyUtils Chunk Region " + ID;
		String dump = "";
		String newline = System.getProperty("line.separator");
		
		dump = begin + newline;
		for (int x = 0; x < SwootyUtils.CHUNK_WIDTH; x++) {
			for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				dump += chunkData[x][y].getType().getID() + ",";
			}
		}
		dump += newline + end + newline;
		return dump;
	}
	
	public WorldDrop[][] getDropData() {
		return dropData;
	}

	public Block[][] getBlocks() {
		return chunkData;
	}

	public void updateBlock(Block b) {
		chunkData[b.getLocation().x][b.getLocation().y] = b;
		//queuedChanges.add(b);
	}
	
	public void updateDrop(WorldDrop b) {
		dropData[b.getLocation().x][b.getLocation().y] = b;
		//queuedDrops.add(b);
	}

}
