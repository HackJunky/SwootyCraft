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
	private int seamlessHeight;
	int terrainHeight;

	public Chunk(int id, boolean loaded, int sHeight) {
		seamlessHeight = sHeight;
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

	public int getSeamlessHeight() {
		return seamlessHeight;
	}

	public void generateTerrain() {
		parseAir();
		parseBasic();
		parseOre();
		parseWater();
		parseStructures();
		parseLava();
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
		if (seamlessHeight == -1) {
			terrainHeight = (int)(SwootyUtils.CHUNK_HEIGHT - (SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR)));
		}else {
			terrainHeight = seamlessHeight;
		}
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
					seamlessHeight = y;
				}else if (y < terrainHeight + (new Random().nextInt(height) + 8)) {
					chunkData[x][y] = new Block(SwootyUtils.BlockType.DIRT, ID, new Point(x, y));
				}else if (y > SwootyUtils.CHUNK_HEIGHT - 2){
					chunkData[x][y] = new Block(SwootyUtils.BlockType.BEDROCK, ID, new Point(x, y));
				}
			}
			if (terrainHeight - (int)(SwootyUtils.CHUNK_HEIGHT - (SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR))) > new Random().nextInt(6)) {
				rise = false;
			}else if (terrainHeight - (int)(SwootyUtils.CHUNK_HEIGHT - (SwootyUtils.CHUNK_HEIGHT * (1.0 - SwootyUtils.CHUNK_PERCENT_AIR))) < new Random().nextInt(6) * -1) {
				rise = true;
			}
		}

	}

	public void parseOre() {
		int oreSize = new Random().nextInt(SwootyUtils.ORE_CLUSTER_SIZE_MAX) + SwootyUtils.ORE_CLUSTER_SIZE_MIN;
		int[][] oreMap = new int[oreSize][oreSize];
		for (int y = (int)(terrainHeight + 10); y < SwootyUtils.CHUNK_HEIGHT - oreSize; y += oreSize * new Random().nextInt(6)) {
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
		int oreSize = SwootyUtils.DUNGEON_PATTERN.length;
		int[][] dungeonMap = new int[oreSize][oreSize];
		for (int y = (int)(terrainHeight + 10 + new Random().nextInt(SwootyUtils.CHUNK_HEIGHT / 8)); y < SwootyUtils.CHUNK_HEIGHT - oreSize; y += oreSize * new Random().nextInt(6)) {
			for (int x = oreSize + new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x < SwootyUtils.CHUNK_WIDTH - oreSize - new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x += oreSize * new Random().nextInt(32)) {
				for (int a = 0; a < oreSize; a++) {
					dungeonMap[a] = SwootyUtils.DUNGEON_PATTERN[a];
				}
				for (int a = 0; a < dungeonMap.length; a++) {
					for (int b = 0; b < dungeonMap.length; b++) {
						int newX = x + a;
						int newY = y + b;
						if (newX > SwootyUtils.CHUNK_WIDTH) {
							break;
						}
						if (newY > SwootyUtils.CHUNK_HEIGHT) {
							newY = SwootyUtils.CHUNK_HEIGHT - 1;
						}
						try {
							if (chunkData[newX][newY].getType() != SwootyUtils.BlockType.AIR) {
								if (dungeonMap[a][b] == 1) {
									chunkData[newX][newY] = new Block(SwootyUtils.BlockType.MOSSY_COBBLESTONE, ID, new Point(newX, newY));
								}else if (dungeonMap[a][b] == 2) { //Change to Chest
									chunkData[newX][newY] = new Block(SwootyUtils.BlockType.SANDSTONE, ID, new Point(newX, newY));
								}else if (dungeonMap[a][b] == 3) {
									//Add Mob Spawner code here
								}else if (dungeonMap[a][b] == 0) {
									chunkData[newX][newY] = new Block(SwootyUtils.BlockType.AIR, ID, new Point(newX, newY));
								}
							}
						}catch (Exception e) {

						}
					}
				}
			}
			break;
		}
	}

	public void parseWater() {
		//Water
		try {
			int startX = new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 2);
			int startY = 0;
			int endX = 0;
			int depth = 0;
			for (int y = 0; y < SwootyUtils.CHUNK_HEIGHT; y++) {
				if (chunkData[startX][y].getType() == SwootyUtils.BlockType.GRASS) {
					startY = y;
				}
			}
			
			if (startX > 0 && startY > 0) {
				int distance = 0;
				for (int x = startX; x < SwootyUtils.CHUNK_WIDTH; x++) {
					if (distance > 4 && chunkData[x][startY].getType() == SwootyUtils.BlockType.GRASS) {
						endX = x;
						break;
					}
					distance++;
					
					for (int y = startY; y < SwootyUtils.CHUNK_HEIGHT; y++) {
						if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS && y > depth) {
							depth = y + 1;
						}
					}
				}
			}
			
			if (endX > startX && depth - startY > 2) {
				for (int x = startX; x <= endX; x++) {
					for (int y = startY; y <= depth; y++) {
						if (chunkData[x][y].getType() == SwootyUtils.BlockType.AIR) {
							chunkData[x][y] = new Block(SwootyUtils.BlockType.WATER, ID, new Point(x, y));
						}else if (chunkData[x][y].getType() == SwootyUtils.BlockType.GRASS) {
							chunkData[x][y] = new Block(SwootyUtils.BlockType.SAND, ID, new Point(x, y));
						}
					}
					for (int y = startY; y > 0; y--) {
						chunkData[x][y] = new Block(SwootyUtils.BlockType.AIR, ID, new Point(x, y));
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parseLava() {
		int oreSize = new Random().nextInt(SwootyUtils.ORE_CLUSTER_SIZE_MAX) + SwootyUtils.ORE_CLUSTER_SIZE_MIN;
		int[][] oreMap = new int[oreSize][oreSize];
		for (int y = (int)(terrainHeight + 32 + new Random().nextInt(32)); y < SwootyUtils.CHUNK_HEIGHT - oreSize; y += oreSize * new Random().nextInt(6)) {
			for (int x = oreSize + new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x < SwootyUtils.CHUNK_WIDTH - oreSize - new Random().nextInt(SwootyUtils.CHUNK_WIDTH / 4); x += oreSize * new Random().nextInt(6)) {
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
									chunkData[newX][newY] = new Block(SwootyUtils.BlockType.LAVA, ID, new Point(newX, newY));
								}
							}
						}catch (Exception e) {

						}
					}
				}
			}
		}
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
