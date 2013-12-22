import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;


public class SwootyUtils {
	//StartupFrame.java Variables
	static int ENV_SIZE_X = Toolkit.getDefaultToolkit().getScreenSize().width;
	static int ENV_SIZE_Y = Toolkit.getDefaultToolkit().getScreenSize().height;
	
	//Network Variables
	static int NUM_SLOTS = 16;
	
	//World.java Variables
	static int CHUNK_QTY = 16;
	static int TILE_SIZE_X = 32;
	static int TILE_SIZE_Y = 32;
	static int VIEWPORT_TILES_X = ENV_SIZE_X / TILE_SIZE_X;
	static int VIEWPORT_TILES_Y = (ENV_SIZE_Y / TILE_SIZE_Y) + 1;
	static int WORLD_TIME_CAP = 500;
	
	//Chunk.java Variables
	static int CHUNK_HEIGHT = 256;
	static int CHUNK_WIDTH = 64;
	static double CHUNK_PERCENT_AIR = 0.3;
	static int ORE_CLUSTER_SIZE_MIN = 4;
	static int ORE_CLUSTER_SIZE_MAX = 6;
	static int VIEWPORT_CHANGE_THRESH = 15;
	static int GRAVITY = TILE_SIZE_Y / 2;
	static int KEY_PRESS_THRESH = 15;
	
	static int[][] ORE_CLUSTER_PATTERNS = {{0, 1, 1, 0} , {1, 0, 0, 1}, {1, 0, 1, 1}, {0, 0, 0, 1}, {1, 1, 1, 0}};
	static int[][] DUNGEON_PATTERN = {{1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 2, 0, 3, 0, 2, 1}, {1, 1, 1, 1, 1, 1, 1, 1}};
	static int[][] TREE_PATTERN = {{0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0}, {0, 0, 2, 1, 2, 0, 0}, {0, 2, 2, 2, 2, 2, 0}, {0, 2, 2, 2, 2, 2, 0}, {0, 0, 2, 2, 2, 0, 0}, {0, 0, 0, 2, 0, 0, 0}};
	
	
	//Block.java Variables
	static enum BlockType {
		AIR("Air", 0, loadImage("Air.png")), BEDROCK("Bedrock", 1, loadImage("Bedrock.png")), BRICK("Brick", 2, loadImage("Brick.png")), CLAY("Clay", 3, loadImage("clay.png")),
		COAL_ORE("Coal Ore", 4, loadImage("Coal Ore.png")), COBBLESTONE("Cobblestone", 5, loadImage("Cobblestone.png")), DIAMOND_ORE("Diamond Ore", 6, loadImage("Diamond Ore.png")),
		DIRT("Dirt", 7, loadImage("Dirt.png")), GOLD_ORE("Gold Ore", 8, loadImage("Gold Ore.png")), GRASS("Grass", 9, loadImage("Grass.png")), GRAVEL("Gravel", 10, loadImage("Gravel.png")), 
		ICE("Ice", 11, loadImage("Ice.png")), IRON_ORE("Iron Ore", 12, loadImage("Iron Ore.png")), LAPIZ_ORE("Lapiz Ore", 13, loadImage("Lapiz Ore.png")), LAVA("Lava", 14, loadImage("Lava.png")), 
		LOG("Log", 15, loadImage("Log.png")), MOSSY_COBBLESTONE("Mossy Cobblestone", 16, loadImage("Mossy Cobblestone.png")), REDSTONE_ORE("Redstone Ore", 17, loadImage("Redstone Ore.png")),
		SAND("Sand", 18, loadImage("Sand.png")), SANDSTONE("Sandstone", 19, loadImage("Sandstone.png")), SNOW_BLOCK("Snow Block", 20, loadImage("Snow.png")), SNOW_GRASS("Snow Grass", 21, loadImage("Snowy Grass.png")),
		STONE_BRICK("Stone Brick", 22, loadImage("Stone Brick.png")), STONE("Stone", 23, loadImage("Stone.png")), WATER("Water", 24, loadImage("Water.png")), WOOD("Wood", 25, loadImage("Wood.png")), LEAVES("Leaves", 26, loadImage("Leaves.png"));

		private String name;
		private int ID;
		private BufferedImage blockImage;
		
		BlockType(String name, int ID, BufferedImage blockImage){
			this.name = name;
			this.ID = ID;
			this.blockImage = blockImage;
		}
		
		public String getName() {
			return name;
		}
		
		public int getID() {
			return ID;
		}
		
		public BufferedImage getImage() {
			return blockImage;
		}
	}
	
	static BlockType getByID(int i) {
		for (BlockType b : BlockType.values()) {
			if (b.getID() == i) {
				return b;
			}
		}
		return null;
	}
	
	static enum EntityType {
		BED("Bed", 0, true, loadImage("Bed.png")), CACTUS("CACTUS", 1, true, loadImage("Cactus.png")), CRAFTING_TABLE("Crafting Table", 2, false, loadImage("Crafting Table.png")), DOUBLE_CHEST("Chest", 3, true, loadImage("Double Chest.png")),
		FURNACE_OFF("Furnace", 4, false, loadImage("Furnace Off.png")), FURNACE_ON("Furnace", 5, false, loadImage("Furnace On.png")), ROSE("Rose", 6, false, loadImage("Rose.png")), SAPLING("Sapling", 7, false, loadImage("Sapling.png")), SUGAR_CANE("Sugar Cane", 8, true, loadImage("Sugar Cane.png")),
		TNT("TNT", 8, false, loadImage("TNT.png")), YELLOW_FLOWER("Yellow Flower", 9, false, loadImage("Yellow Flower.png")), PLAYER_FRONT("Player", 10, true, loadImage("Player Front.png")), PLAYER_LEFT("Player", 11, true, loadImage("Player Left.png")), PLAYER_RIGHT("Player", 12, true, loadImage("Player Right.png"));
		
		private String name;
		private int ID;
		private boolean isMultiblock;
		private BufferedImage entityImage;
		
		EntityType(String name, int ID, boolean isMulti, BufferedImage entityImage){
			this.name = name;
			this.ID = ID;
			this.isMultiblock = isMulti;
			this.entityImage = entityImage;
		}
		
		public String getName() {
			return name;
		}
		
		public int getID() {
			return ID;
		}
		
		public BufferedImage getImage() {
			return entityImage;
		}
		
		public boolean isMultiblock() {
			return isMultiblock;
		}
	}
	
	static enum UIItem {
		HOTBAR_UNSELECTED(loadImage("../UI/Tile Unselected.png")), HOTBAR_SELECTED(loadImage("../UI/Tile Selected.png")), ICON_ARMOR(loadImage("../UI/Armor.png")), ICON_ARMOR_HALF(loadImage("../UI/Armor Half.png")), ICON_ARMOR_EMPTY(loadImage("../UI/Armor Empty.png")), ICON_HEART(loadImage("../UI/Heart.png")),
		ICON_HEART_HALF(loadImage("../UI/Heart Half.png")), ICON_HEART_EMPTY(loadImage("../UI/Heart Empty.png")), ICON_HUNGER(loadImage("../UI/Hunger.png")), ICON_HUNGER_HALF(loadImage("../UI/Hunger Half.png")), ICON_HUNGER_EMPTY(loadImage("../UI/Hunger Empty.png")), XP_BAR_EMPTY(loadImage("../UI/XPBar Empty.png")),
		XP_BAR_FULL(loadImage("../UI/XPBar Full.png")); 
		
		private BufferedImage image;
		
		UIItem(BufferedImage image) {
			this.image = image;
		}
		
		public BufferedImage getImage() {
			return image;
		}
	}
	
	
	//Methods 
	static BufferedImage loadImage(String filename) {
		try {
			return ImageIO.read(new File("data/textures/" + filename));
		}catch (Exception e) {
			log("SwootyUtils", "Failed to load image for " + filename + ".");
			return null;
		}
	}
	
	static void log(String className, String message) {
		Date d = new Date();
		System.out.println(d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + " (" + className + "): " + message);
	}
}
