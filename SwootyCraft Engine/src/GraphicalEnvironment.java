import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.Timer;

public class GraphicalEnvironment extends JFrame{
	private static final long serialVersionUID = -4592966004232209324L;
	private boolean isDebugMode = false;
	private boolean isDebugVisible = false;
	private Rectangle renderArea;
	private Rectangle hotbarArea;
	private Rectangle armorArea;
	private Rectangle healthArea;
	private Rectangle hungerArea;
	private Rectangle xpArea;
	private Rectangle debugArea;
	private Rectangle statsArea;
	private ArrayList<String> statsList;
	private ArrayList<String> debugData;
	private String consoleInput = "";
	private EventTicker gameEvents = new EventTicker();
	private Timer gameTimer = new Timer(1, gameEvents);

	//First Run
	private boolean init = false;

	//Network Data
	private Thread[] networkThreads;
	private NetworkModule[] networkSlots;
	private int totalUsers = -1;
	private ArrayList<String> playerConnected;
	private ArrayList<String> playerDisconnected;

	private boolean isServer;
	private String ip;
	private int port;
	private String password;
	private String username;
	private boolean allowInput;
	private boolean autoSave;
	private String worldName;
	private boolean isLoaded;

	//World Data
	private boolean isNetworked;
	private World worldData;
	private int opsPerTick = -1;
	private Point cursorPos = null;
	private Point selectedBlock = null;

	public GraphicalEnvironment (boolean isDebug, boolean isNetworked, boolean isServer, String ip, int port, String password, String username, boolean allowInput, boolean autoSave, String worldName, boolean isLoaded) {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.isNetworked = isNetworked;
		this.isServer = isServer;
		this.ip = ip;
		this.port = port;
		this.password = password;
		this.username = username;
		this.allowInput = allowInput;
		this.autoSave = autoSave;
		this.worldName = worldName;
		this.isLoaded = isLoaded;

		if (!isNetworked) {
			SwootyUtils.log("Environment", "This is a local (non-networked) game!");
		}else {
			if (isServer) {
				SwootyUtils.log("Environment", "This is a networked game -> Allocated " + SwootyUtils.NUM_SLOTS + " Thread slots.");
				networkThreads = new Thread[SwootyUtils.NUM_SLOTS];
				networkSlots = new NetworkModule[SwootyUtils.NUM_SLOTS];	
			}else {
				SwootyUtils.log("Environment", "This is a networked game -> 1 Thread allocated to Network.");
				networkThreads = new Thread[1];
				networkSlots = new NetworkModule[1];
			}
		}

		isDebugMode = isDebug;
		debugData = new ArrayList<String>();
		statsList = new ArrayList<String>();
		playerConnected = new ArrayList<String>();
		playerDisconnected = new ArrayList<String>();
		worldData = new World(isNetworked, isServer, username, isLoaded, worldName);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (worldData != null && GraphicalEnvironment.this.autoSave && !GraphicalEnvironment.this.isLoaded) {
					try {
						File file = new File("saves/" + GraphicalEnvironment.this.worldName +"/" + GraphicalEnvironment.this.worldName + ".save");
						file.getParentFile().mkdirs();
						FileWriter writer = new FileWriter(file);
						for (String s : worldData.dumpRegions()) {
							writer.write(s);
						}
						writer.close();
					}catch (Exception f) {
						f.printStackTrace();
					}
				}
				System.exit(0);
			}
		});

		SwootyUtils.log("Environment", "Loading main code system to game...");
		this.setUndecorated(true);
		this.setSize(SwootyUtils.ENV_SIZE_X, SwootyUtils.ENV_SIZE_Y);
		this.addMouseListener(gameEvents);
		this.addKeyListener(gameEvents);
		this.addMouseWheelListener(gameEvents);

		this.setVisible(true);
		this.setCursor(Cursor.CROSSHAIR_CURSOR);

		SwootyUtils.log("Environment", "Applying BufferStrategy...");
		configureLayout();
		this.createBufferStrategy(2);
		gameTimer.start();	

		//worldData.setCoordCentered(new Point((((SwootyUtils.CHUNK_QTY * SwootyUtils.CHUNK_WIDTH) / 2) * SwootyUtils.TILE_SIZE_X), ((SwootyUtils.CHUNK_QTY * SwootyUtils.CHUNK_HEIGHT) / 2) * SwootyUtils.TILE_SIZE_Y));
		worldData.setBlockCentered(new Point((SwootyUtils.CHUNK_QTY * SwootyUtils.CHUNK_WIDTH) / 2, (int)(SwootyUtils.CHUNK_HEIGHT * (1 - SwootyUtils.CHUNK_PERCENT_AIR))));
	}

	public void configureLayout() {
		renderArea = new Rectangle (0, 0, SwootyUtils.ENV_SIZE_X, SwootyUtils.ENV_SIZE_Y);
		statsArea = new Rectangle (0, 0, SwootyUtils.ENV_SIZE_X, 25);
		debugArea = new Rectangle (0, SwootyUtils.ENV_SIZE_Y - (SwootyUtils.ENV_SIZE_Y / 6), renderArea.width, SwootyUtils.ENV_SIZE_Y / 6);
		hotbarArea = new Rectangle ((int)(SwootyUtils.ENV_SIZE_X / 2 - (3.5 * 40)), SwootyUtils.ENV_SIZE_Y - 40, (9 * 40), 40);
		armorArea = new Rectangle (hotbarArea.x, hotbarArea.y - 35, hotbarArea.width / 4, 20);
		healthArea = new Rectangle (hotbarArea.x + hotbarArea.width - 150, hotbarArea.y - 35, hotbarArea.x + hotbarArea.width, 20);
		hungerArea = new Rectangle (armorArea.x, armorArea.y - 20, armorArea.width, 20);
		xpArea = new Rectangle (hotbarArea.x, hotbarArea.y - 15, hotbarArea.width, 10);
		while (debugArea.y + debugArea.height > SwootyUtils.ENV_SIZE_Y) {
			debugArea.height--;
		}
	}

	public void populateStats() {
		statsList = new ArrayList<String>();
		if (isNetworked) {
			statsList.add("Network");
			if (isServer) {
				statsList.add("Server");
			}else {
				statsList.add("Client");
			}
			if (isServer) {
				int numThreads = 0;
				int overhead = 0;
				for (int i = 0; i < SwootyUtils.NUM_SLOTS; i++) {
					NetworkModule n = networkSlots[i];
					if (n != null) {
						numThreads++;
						overhead += networkSlots[i].overhead;
					}else {
						break;
					}
				}
				numThreads--;
				statsList.add("Slots: " + numThreads + "/" + SwootyUtils.NUM_SLOTS);
				numThreads += 1;
				overhead = overhead / numThreads;
				statsList.add("Overhead Mux: " + overhead);
			}
			statsList.add("Players: " + worldData.getPlayers().size());
		}else {
			statsList.add("Local");
		}
		try {
			statsList.add("Physics Iterations/Tick: " + worldData.getIterations());
		}catch (Exception e) {
			statsList.add("Physics Iterations/Tick: ???");
		}
		try {
			statsList.add("World Time: " + worldData.getWorldCycle());
		}catch (Exception e) {
			statsList.add("World Time: ?");
		}
		statsList.add("Block Updates/Tick: " + worldData.getQueueSize());
		try {
			statsList.add("Viewport: (" + worldData.getBlockViewport().x + ", " + worldData.getBlockViewport().y + ", " + worldData.getBlockViewport().width + ", " + worldData.getBlockViewport().height + ")");
			//statsList.add("Viewport: (" + (worldData.getCoordViewport().x / SwootyUtils.TILE_SIZE_X) + ", " + (worldData.getCoordViewport().y / SwootyUtils.TILE_SIZE_Y) + ", " + (worldData.getCoordViewport().width / SwootyUtils.TILE_SIZE_X) + ", " + (worldData.getCoordViewport().height / SwootyUtils.TILE_SIZE_Y) + ")");
		}catch (Exception e) {
			statsList.add("Viewport: (??, ??, ??, ??)");
		}

		statsList.add("Render Load: " + opsPerTick);
		opsPerTick = -1;
		if (cursorPos != null && selectedBlock != null) {
			statsList.add("Cursor: (" + cursorPos.x + ", " + cursorPos.y + "), (" + selectedBlock.x + " , " + selectedBlock.y + ")");
		}else {
			statsList.add("Cursor: No Cursor Data");
		}
	}

	public void draw() {
		//Get the Graphical Environment
		BufferStrategy bs = this.getBufferStrategy();
		Graphics2D g2d = (Graphics2D)bs.getDrawGraphics();
		g2d.setFont(new Font("Minecraft Regular", Font.BOLD, 12));
		FontMetrics fm = g2d.getFontMetrics();
		Stroke baseStroke = g2d.getStroke();
		Stroke thickStroke = new BasicStroke(2);

		if (worldData.getChunks()[0] != null) {
			//Game Area
			g2d.setColor(Color.MAGENTA);
			g2d.fillRect(renderArea.x, renderArea.y, renderArea.width, renderArea.height);
			g2d.setColor(Color.ORANGE);
			for (int x = 0; x < SwootyUtils.VIEWPORT_TILES_X; x++) {
				for (int y = 0; y < SwootyUtils.VIEWPORT_TILES_Y; y++) {
					Point blockPoint = new Point(x + worldData.getBlockViewport().x, y + worldData.getBlockViewport().y);
					//Point blockPoint = new Point(x + worldData.getCoordViewport().x / SwootyUtils.TILE_SIZE_X, y + worldData.getCoordViewport().y / SwootyUtils.TILE_SIZE_Y);
					//Point blockPoint = new Point(x + (worldData.getCoordViewport().x / SwootyUtils.TILE_SIZE_X), y + (worldData.getCoordViewport().y / SwootyUtils.TILE_SIZE_Y)); 
					Block block = worldData.getBlock(blockPoint);
					Point coordinatePoint = new Point(x * SwootyUtils.TILE_SIZE_X, y * SwootyUtils.TILE_SIZE_Y);
					if (block != null) {
						if (block.getType() == SwootyUtils.BlockType.LEAVES || block.getType() == SwootyUtils.BlockType.WATER) {
							g2d.drawImage(SwootyUtils.BlockType.AIR.getImage(), coordinatePoint.x, coordinatePoint.y, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y, this);
							opsPerTick++;
						}
						g2d.drawImage(block.getType().getImage(), coordinatePoint.x, coordinatePoint.y, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y, this);

						opsPerTick++;
						if (selectedBlock != null) {
							if (x + worldData.getBlockViewport().x == selectedBlock.x && y + worldData.getBlockViewport().y == selectedBlock.y) {
								Color c = g2d.getColor();
								g2d.setColor(Color.BLACK);
								g2d.drawRect(coordinatePoint.x, coordinatePoint.y, SwootyUtils.TILE_SIZE_X - 1, SwootyUtils.TILE_SIZE_Y - 1);
								String info = "ERR_UNDEF";
								if (worldData.getDrop(selectedBlock) != null) { 
									info = "Pickup: ";
									SwootyUtils.BlockType[] known = new SwootyUtils.BlockType[1000];
									int[] knownQTY = new int[1000];
									int index = 0;
									for (SwootyUtils.BlockType b : worldData.getDrop(selectedBlock).getTypes()) {
										if (b != null) {
											boolean found = false;
											for (int i = 0; i < index; i++) {
												if (known[i] != null && known[i].getName().equals(b.getName())) {
													found = true;
													knownQTY[i]++;
													break;
												}
											}
											if (!found) {
												known[index] = b;
												knownQTY[index] = 1;
												index++;
											}
										}	
									}
									for (int i = 0; i < index; i++) {
										SwootyUtils.BlockType a = known[i];
										if (a != null) {
											info += a.getName() + "(" + knownQTY[i] + "), ";
										}
									}
									info = info.substring(0, info.length() - 2);
								}else {
									info = block.getType().getName() + " (" + blockPoint.x + ", " + blockPoint.y + ")";
								}
								Rectangle infoBox = new Rectangle(coordinatePoint.x - fm.stringWidth(info) - 10, coordinatePoint.y - fm.getHeight() - 10, fm.stringWidth(info), fm.getHeight());
								//							g2d.fillRect(infoBox.x, infoBox.y, infoBox.width + 3, infoBox.height);
								//							g2d.drawRect(infoBox.x, infoBox.y, infoBox.width + 3, infoBox.height);
								g2d.setColor(Color.CYAN);
								g2d.drawString(info, infoBox.x + 2, infoBox.y + fm.getAscent());
								g2d.setColor(c);
								opsPerTick++;
							}
						}	
					}else {
						Color c = g2d.getColor();
						g2d.setColor(Color.BLACK);
						g2d.fillRect(coordinatePoint.x, coordinatePoint.y, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y);
						g2d.setColor(c);
						opsPerTick++;
					}
				}
			}

			//Drops
			for (int x = 0; x < SwootyUtils.VIEWPORT_TILES_X; x++) {
				for (int y = 0; y < SwootyUtils.VIEWPORT_TILES_Y; y++) {
					Point blockPoint = new Point(x + worldData.getBlockViewport().x, y + worldData.getBlockViewport().y);
					Point coordinatePoint = new Point(x * SwootyUtils.TILE_SIZE_X, y * SwootyUtils.TILE_SIZE_Y);
					if (worldData.getDrop(blockPoint) != null) {
						for (SwootyUtils.BlockType b : worldData.getDrop(blockPoint).getTypes()) {
							if (b != null) {
								g2d.drawImage(b.getImage(), coordinatePoint.x + worldData.getDrop(blockPoint).getOffset().x, coordinatePoint.y + worldData.getDrop(blockPoint).getOffset().y, SwootyUtils.TILE_SIZE_X / 2, SwootyUtils.TILE_SIZE_Y / 2, this);
								opsPerTick++;
							}else {
								break;
							}
						}
					}
				}
			}

			//Players
			ArrayList<Player> players = worldData.getPlayers();
			if (players != null) {
				for (int i = 0; i < players.size(); i++) {
					Player p = players.get(i);
					if (p.isAlive() && p.getPlayerCoordLocation() != null && worldData.getBlockViewport().contains(p.getPlayerBlockLocation())) {
						int playerX = p.getPlayerCoordLocation().x - (worldData.getBlockViewport().x * SwootyUtils.TILE_SIZE_X);
						int playerY = p.getPlayerCoordLocation().y - (worldData.getBlockViewport().y * SwootyUtils.TILE_SIZE_Y);
						if (p.getDirection() == -1) {
							g2d.drawImage(SwootyUtils.EntityType.PLAYER_LEFT.getImage(), playerX, playerY, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y * 2, this);
							if (worldData.getMyPlayer().getSelectedBlock() != null) {
								g2d.drawImage(p.getSelectedBlock().getImage(), playerX + (SwootyUtils.TILE_SIZE_X - (SwootyUtils.TILE_SIZE_X / 2)), playerY + (SwootyUtils.TILE_SIZE_Y - (SwootyUtils.TILE_SIZE_Y / 4)), SwootyUtils.TILE_SIZE_X / 2, SwootyUtils.TILE_SIZE_Y / 2, this);
							}
						}else if (p.getDirection() == 0) {
							g2d.drawImage(SwootyUtils.EntityType.PLAYER_FRONT.getImage(), playerX, playerY, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y * 2, this);
							if (worldData.getMyPlayer().getSelectedBlock() != null) {
								g2d.drawImage(p.getSelectedBlock().getImage(), playerX + (SwootyUtils.TILE_SIZE_X - (SwootyUtils.TILE_SIZE_X / 4)), playerY + (SwootyUtils.TILE_SIZE_Y - (SwootyUtils.TILE_SIZE_Y / 4)), SwootyUtils.TILE_SIZE_X / 2, SwootyUtils.TILE_SIZE_Y / 2, this);
							}
						}else if (p.getDirection() == 1) {
							g2d.drawImage(SwootyUtils.EntityType.PLAYER_RIGHT.getImage(), playerX, playerY, SwootyUtils.TILE_SIZE_X, SwootyUtils.TILE_SIZE_Y * 2, this);
							if (worldData.getMyPlayer().getSelectedBlock() != null) {
								g2d.drawImage(p.getSelectedBlock().getImage(), playerX + (SwootyUtils.TILE_SIZE_X / 2), playerY + (SwootyUtils.TILE_SIZE_Y - (SwootyUtils.TILE_SIZE_Y / 4)), SwootyUtils.TILE_SIZE_X / 2, SwootyUtils.TILE_SIZE_Y / 2, this);
							}
						}

						int drawX = playerX + (SwootyUtils.TILE_SIZE_X / 4) - (fm.stringWidth(p.getName()) / 2) + 2;
						int drawY = (int)(playerY - (SwootyUtils.TILE_SIZE_Y + (SwootyUtils.TILE_SIZE_Y / 4)) + fm.getHeight()) + 2;
						int drawWidth = fm.stringWidth(p.getName()) + 5;
						int drawHeight = fm.getHeight() + 5;
						
						Color c = g2d.getColor();
						
						Color b = new Color(0.0f, 0.0f, 0.0f, 0.75f);
						g2d.setColor(b);
						g2d.fillRect(drawX - 2, drawY - 2, drawWidth, drawHeight);
						g2d.setColor(Color.WHITE);  
						g2d.drawString(p.getName(), drawX, drawY + fm.getHeight());
						g2d.setColor(c);
						opsPerTick++;
					}
				}
			}

			//Alpha
			Color c1 = g2d.getColor();
			Color a1 = new Color(0.0f, 0.0f, 0.0f, worldData.getWorldTime());  
			g2d.setColor(a1);
			g2d.fillRect(renderArea.x, renderArea.y, renderArea.width, renderArea.height);
			g2d.setColor(c1);


			if (worldData.getMyPlayer() != null && worldData.getMyPlayer().isAlive()) {
				//Hotbar
				int increment = SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage().getWidth();
				int offsetX = 0;
				for (int i = 0; i < 9; i++) {
					if (i != worldData.getMyPlayer().getSelectedIndex()) {
						g2d.drawImage(SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage(), hotbarArea.x + offsetX, hotbarArea.y, SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage().getWidth(), SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage().getHeight(), this);
					}else {
						g2d.drawImage(SwootyUtils.UIItem.HOTBAR_SELECTED.getImage(), hotbarArea.x + offsetX, hotbarArea.y, SwootyUtils.UIItem.HOTBAR_SELECTED.getImage().getWidth(), SwootyUtils.UIItem.HOTBAR_SELECTED.getImage().getHeight(), this);
					}
					if (worldData.getMyPlayer().getHotbarByIndex(i) != null) {
						g2d.drawImage(worldData.getMyPlayer().getHotbarByIndex(i).getImage(), hotbarArea.x + offsetX + 10, hotbarArea.y + 10, SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage().getWidth() - 20, SwootyUtils.UIItem.HOTBAR_UNSELECTED.getImage().getHeight() - 20, this);
						String num = String.valueOf(worldData.getMyPlayer().getHotbarQTYByIndex(i));
						Color c = g2d.getColor();
						g2d.setColor(Color.WHITE);
						g2d.drawString(num, hotbarArea.x + offsetX + 10, hotbarArea.y + hotbarArea.height - fm.getHeight());
						g2d.setColor(c);
					}
					offsetX += increment;
				}

				//XP Bar
				g2d.drawImage(SwootyUtils.UIItem.XP_BAR_EMPTY.getImage(), xpArea.x, xpArea.y, xpArea.width, xpArea.height, this);
				g2d.drawImage(SwootyUtils.UIItem.XP_BAR_FULL.getImage(), xpArea.x, xpArea.y, (int)(xpArea.width / (10 / worldData.getMyPlayer().getXP())), xpArea.height, this);

				//Armor
				increment = 15;
				offsetX = 0;
				for (int i = 1; i < 11; i++) {
					if (i <= worldData.getMyPlayer().getArmor()) {
						g2d.drawImage(SwootyUtils.UIItem.ICON_ARMOR.getImage(), armorArea.x + offsetX, armorArea.y, 15, 15, this);
					}else {
						if (i - worldData.getMyPlayer().getArmor() == 0.5) {
							g2d.drawImage(SwootyUtils.UIItem.ICON_ARMOR_HALF.getImage(), armorArea.x + offsetX, armorArea.y, 15, 15, this);
						}else {
							g2d.drawImage(SwootyUtils.UIItem.ICON_ARMOR_EMPTY.getImage(), armorArea.x + offsetX, armorArea.y, 15, 15, this);
						}
					}
					offsetX += increment;
				}

				//Health
				increment = 15;
				offsetX = 0;
				for (int i = 1; i < 11; i++) {
					if (i <= worldData.getMyPlayer().getHealth()) {
						g2d.drawImage(SwootyUtils.UIItem.ICON_HEART.getImage(), healthArea.x + offsetX, healthArea.y, 15, 15, this);
					}else {
						if (i - worldData.getMyPlayer().getHealth() == 0.5) {
							g2d.drawImage(SwootyUtils.UIItem.ICON_HEART_HALF.getImage(), healthArea.x + offsetX, healthArea.y, 15, 15, this);
						}else {
							g2d.drawImage(SwootyUtils.UIItem.ICON_HEART_EMPTY.getImage(), healthArea.x + offsetX, healthArea.y, 15, 15, this);
						}
					}
					offsetX += increment;
				}

				//Hunger
				increment = 15;
				offsetX = 0;
				for (int i = 1; i < 11; i++) {
					if (i <= worldData.getMyPlayer().getHunger()) {
						g2d.drawImage(SwootyUtils.UIItem.ICON_HUNGER.getImage(), hungerArea.x + offsetX, hungerArea.y, 15, 15, this);
					}else {
						if (i - worldData.getMyPlayer().getHunger() == 0.5) {
							g2d.drawImage(SwootyUtils.UIItem.ICON_HUNGER_HALF.getImage(), hungerArea.x + offsetX, hungerArea.y, 15, 15, this);
						}else {
							g2d.drawImage(SwootyUtils.UIItem.ICON_HUNGER_EMPTY.getImage(), hungerArea.x + offsetX, hungerArea.y, 15, 15, this);
						}
					}
					offsetX += increment;
				}
			}

			Point notification = new Point(renderArea.x + 10, renderArea.y + statsArea.height);
			ArrayList<String> connections = playerConnected;

			int offsetY = 0;
			if (connections != null && connections.size() > 0) {
				int i = 0;
				for (String s : connections) {
					String display = connections.get(i) + " has connected!";
					int size = fm.stringWidth(display);
					Rectangle drawArea = new Rectangle(notification.x, notification.y + fm.getHeight() + 10 + offsetY, size + 10, fm.getHeight() + 5);
					Color c = g2d.getColor();
					g2d.setColor(Color.BLACK);
					g2d.fillRect(drawArea.x, drawArea.y, drawArea.width, drawArea.height);
					g2d.setColor(Color.WHITE);
					g2d.drawString(display, drawArea.x + 5, drawArea.y + fm.getHeight());
					g2d.setColor(c);
					//System.out.println("Drawing connection message for " + s + "...");
					offsetY += fm.getHeight() + 10;
					i++;
				}
			}

			ArrayList<String> disconnections = playerDisconnected;
			if (disconnections != null && disconnections.size() > 0) {
				int i = 0;
				for (String s : disconnections) {
					String display = disconnections.get(i) + " has disconnected!";
					int size = fm.stringWidth(display);
					Rectangle drawArea = new Rectangle(notification.x, notification.y + fm.getHeight() + 10 + offsetY, size + 10, fm.getHeight() + 5);
					Color c = g2d.getColor();
					g2d.setColor(Color.BLACK);
					g2d.fillRect(drawArea.x, drawArea.y, drawArea.width, drawArea.height);
					g2d.setColor(Color.BLUE);
					g2d.drawString(display, drawArea.x + 5, drawArea.y + fm.getHeight());
					g2d.setColor(c);
					//System.out.println("Drawing disconnection message for " + s + "...");
					offsetY += fm.getHeight() + 10;
					i++;
				}
			}
		}else {
			g2d.setColor(Color.GRAY);
			g2d.fillRect(renderArea.x, renderArea.y, renderArea.width, renderArea.height);
			g2d.setColor(Color.WHITE);
			String message;
			if (isNetworked) {
				message = "Receiving Terrain Data: " + networkSlots[0].downloadProgress + "/" + SwootyUtils.CHUNK_QTY + " Objects...";	
			}else {
				message = "Generating Terrain...";
			}
			g2d.drawString(message, renderArea.width / 2 - (fm.stringWidth(message) / 2), renderArea.height / 2);
		}

		if (isDebugMode && isDebugVisible) {
			//Draw Background
			g2d.setColor(Color.BLACK);
			g2d.fillRect(debugArea.x, debugArea.y, debugArea.width, debugArea.height);
			g2d.setColor(Color.DARK_GRAY);
			g2d.setStroke(thickStroke);
			g2d.drawRect(debugArea.x, debugArea.y, debugArea.width, debugArea.height);
			g2d.setStroke(baseStroke);

			//Draw Text
			int numLines = (debugArea.height / fm.getHeight()) - 1;
			g2d.setColor(Color.GREEN);
			if (debugData.size() > numLines) {
				int offsetY = fm.getHeight() * 2;
				for (int i = debugData.size() - 1; i > debugData.size() - numLines; i--) {
					g2d.drawString(debugData.get(i), debugArea.x + 5, debugArea.y + debugArea.height - offsetY);
					opsPerTick++;
					offsetY += fm.getHeight();
				}
			}else {
				int offsetY = fm.getHeight() * 2;
				for (int i = debugData.size() - 1; i >= 0; i--) {
					g2d.drawString(debugData.get(i), debugArea.x + 5, debugArea.y + debugArea.height - offsetY);
					opsPerTick++;
					offsetY += fm.getHeight();
				}
			}
			g2d.drawString("\\>" + consoleInput + "_", debugArea.x + 5, debugArea.y + debugArea.height - fm.getHeight() + 5);
		}

		//Stats Area
		g2d.setColor(Color.BLACK);
		g2d.fillRect(statsArea.x, statsArea.y, statsArea.width, statsArea.height);
		g2d.setColor(Color.DARK_GRAY);
		g2d.setStroke(thickStroke);
		g2d.drawRect(statsArea.x, statsArea.y, statsArea.width, statsArea.height);
		g2d.setStroke(baseStroke);
		g2d.setColor(Color.GREEN);
		int offset = 20;
		for (String s : statsList) {
			g2d.drawString(s, offset, statsArea.height - (fm.getHeight() / 2));
			opsPerTick++;
			offset += fm.stringWidth(s) + 20;
		}

		Toolkit.getDefaultToolkit().sync();
		bs.show();
	}

	public class EventTicker implements ActionListener, MouseListener, KeyListener, MouseWheelListener {
		private ByteArrayOutputStream newout = new ByteArrayOutputStream();
		private PrintStream ps = new PrintStream(newout);
		private PrintStream old = System.out;
		private int viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH;

		//Keys
		private boolean keyA = false;
		private boolean keyW = false;
		private boolean keyD = false;
		private int keyThresh = SwootyUtils.KEY_PRESS_THRESH;

		public EventTicker() {
			//Redirect Output to internal console
			if (!isNetworked) {
				PrintStream old = System.out;
				System.setOut(ps);
			}else if (isNetworked && isServer) {
				PrintStream old = System.out;
				System.setOut(ps);
			}
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {		
			try {
				if (isNetworked && !isServer) {
					System.setOut(old);
				}
				cursorPos = GraphicalEnvironment.this.getMousePosition();
				if (cursorPos != null) {
					selectedBlock = new Point(worldData.getBlockViewport().x + (cursorPos.x / SwootyUtils.TILE_SIZE_X), worldData.getBlockViewport().y + (cursorPos.y / SwootyUtils.TILE_SIZE_Y));
					if (cursorPos.x == 0) {
						viewportThresh--;
					}else if (cursorPos.x == renderArea.width - 1) {
						viewportThresh--;
					}else if (cursorPos.y <= SwootyUtils.TILE_SIZE_Y) {
						viewportThresh--;
					}else if (cursorPos.y >= renderArea.height - 1) {
						viewportThresh--;
					}else {
						viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH;
					}
					if (cursorPos.x == 0 && viewportThresh <= 0) {
						worldData.moveViewLeft();
						viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH; 
					}
					if (cursorPos.x == renderArea.width - 1 && viewportThresh <= 0) {
						worldData.moveViewRight();
						viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH;
					}
					if (cursorPos.y <= SwootyUtils.TILE_SIZE_Y && viewportThresh <= 0) {
						worldData.moveViewUp();
						viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH;
					}
					if (cursorPos.y >= renderArea.height - 1 && viewportThresh <= 0) {
						worldData.moveViewDown();
						viewportThresh = SwootyUtils.VIEWPORT_CHANGE_THRESH;
					}
				}
			}catch (Exception e) {

			}
			if (isNetworked) {
				if (isServer) {
					int numThreads = 0;
					boolean isListening = false;
					for (int i = 0; i < SwootyUtils.NUM_SLOTS; i++) {
						Thread t = networkThreads[i];
						if (t != null) {
							if (networkSlots[i].terminated) {
								break;
							}
							if (!networkSlots[i].isConnected()) {
								isListening = true;
							}
							numThreads = i + 1;
						}else {
							break;
						}
					}
					if (!isListening) {
						networkThreads[numThreads] = new Thread(networkSlots[numThreads] = new NetworkModule(isServer, ip, port, password, username));
						port += 1;	
						networkThreads[numThreads].start();
						SwootyUtils.log("Environment", "Initialized Thread " + numThreads + " to handle NetworkModule " + numThreads + " (Slot " + numThreads + ")");
					}
				}else {
					if (networkThreads[0] == null) {
						networkThreads[0] = new Thread(networkSlots[0] = new NetworkModule(isServer, ip, port, password, username));
						networkThreads[0].start();
						SwootyUtils.log("Environment", "Initialized Thread 0 to handle NetworkModule 1 (Slot 1)");
					}
				}
			}
			if (isNetworked && isServer || isNetworked && !isServer) {
				totalUsers = 0;
				for (NetworkModule m : networkSlots) {
					if (m != null && m.isConnected()) {
						totalUsers++;
					}
				}
			}

			//Console Redirect
			for (String s : newout.toString().split("\n")) {
				debugData.add(s);
			}

			populateStats();

			//Drawing
			draw();

			debugData = new ArrayList<String>();

			if (!isNetworked) {
				worldData.spawn();
			}else if (isNetworked && isServer) {
				worldData.spawn();
			}

			boolean trigger = false;
			if (keyA && keyThresh <= 0) {
				if (worldData.getMyPlayer().isAlive()) {
					worldData.movePlayerLeft();
					trigger = true;
				}
			}else if (keyD && keyThresh <= 0) {
				if (worldData.getMyPlayer().isAlive()) {
					worldData.movePlayerRight();
					trigger = true;
				}
			}
			if (keyW && keyThresh <= 0) {
				if (worldData.getMyPlayer().isAlive()) {
					worldData.playerJump();
					trigger = true;
				}
			}

			if (trigger) {
				keyThresh = SwootyUtils.KEY_PRESS_THRESH;
			}

			if (keyA || keyD) {
				keyThresh--;
			}else if (keyW) {
				keyThresh--;
			}
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			//log("Event Logger", "KEY_PRESS: " + arg0.getKeyChar());
			if (!isDebugVisible) {
				if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
					if (worldData.getMyPlayer().getPlayerBlockLocation() != null && worldData.getMyPlayer().isAlive()) {
						worldData.setPlayerCentered(worldData.getMyPlayer());
					}
				}
				if (arg0.getKeyCode() == KeyEvent.VK_A) {
					keyA = true;
				}
				if (arg0.getKeyCode() == KeyEvent.VK_D) {
					keyD = true;
				}
				if (arg0.getKeyCode() == KeyEvent.VK_W) {
					keyW = true;
				}
				if (arg0.getKeyCode() == KeyEvent.VK_T && isDebugMode) {
					isDebugVisible = true;
				}
				try {
					if (worldData.getMyPlayer().isAlive()) {
						switch (arg0.getKeyCode()) { 
						case KeyEvent.VK_1:
							worldData.getMyPlayer().setSelectedIndex(0);
							break;
						case KeyEvent.VK_2:
							worldData.getMyPlayer().setSelectedIndex(1);
							break;
						case KeyEvent.VK_3:
							worldData.getMyPlayer().setSelectedIndex(2);
							break;
						case KeyEvent.VK_4:
							worldData.getMyPlayer().setSelectedIndex(3);
							break;
						case KeyEvent.VK_5:
							worldData.getMyPlayer().setSelectedIndex(4);
							break;
						case KeyEvent.VK_6:
							worldData.getMyPlayer().setSelectedIndex(5);
							break;
						case KeyEvent.VK_7:
							worldData.getMyPlayer().setSelectedIndex(6);
							break;
						case KeyEvent.VK_8:
							worldData.getMyPlayer().setSelectedIndex(7);
							break;
						case KeyEvent.VK_9:
							worldData.getMyPlayer().setSelectedIndex(8);
							break;
						}
					}
				}catch (Exception e) {

				}
			}else {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER && isDebugMode) {
					if (processConCMD(consoleInput)) { 
						isDebugVisible = false;
					}
					consoleInput = "";
				}
				try {
					if (!isNetworked && allowInput || isNetworked) {
						if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
							consoleInput = consoleInput.substring(0, consoleInput.length() - 1);
						}else if (arg0.getKeyCode() == KeyEvent.VK_SPACE) {
							consoleInput += " ";
						}else if (KeyEvent.getKeyText(arg0.getKeyCode()).length() < 2) {
							consoleInput += arg0.getKeyChar();
						}
					}
				}catch (Exception e) {

				}
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			//log("Event Logger", "KEY_RELEASE: " + arg0.getKeyChar());
			if (!isDebugVisible) {
				if (arg0.getKeyCode() == KeyEvent.VK_A) {
					keyA = false;
				}
				if (arg0.getKeyCode() == KeyEvent.VK_D) {
					keyD = false;
				}
				if (arg0.getKeyCode() == KeyEvent.VK_W) {
					keyW = false;
				}
			}
		}

		public boolean processConCMD(String s) {
			String[] params = splitParams(s);
			switch (params[0]) {
			case "give":
				if (params.length == 4) {
					String username = params[1];
					int blockID = Integer.valueOf(params[2]);
					int blockQTY = Integer.valueOf(params[3]);

					boolean exists = false;

					if (worldData.getMyPlayer().getName().toLowerCase().equals(username.toLowerCase())) {
						worldData.getMyPlayer().giveBlock(blockID, blockQTY);
						exists = true;
					}else {	
						for (int i = 0; i < worldData.getPlayers().size(); i++) {
							if (worldData.getPlayers().get(i).getName().toLowerCase().equals(username.toLowerCase())) {
								exists = true;
							}
						}
					}
					if (exists) {
						SwootyUtils.log("CONCMD -> WORLD", "Giving " + username + " " + blockQTY + " of " + blockID + "...");
						return true;
					}else {
						SwootyUtils.log("CONCMD", "Player '" + username + " does not exist in this instance!");
						return false;
					}
				}else {
					SwootyUtils.log("CONCMD", "Usage: give [name] [blockid] [blockqty]. Use 'help' for more.");
					return false;
				}
			case "help":
				SwootyUtils.log("CONCMD", "SwootyCraft Console Help Screen");
				SwootyUtils.log("CONCMD", "      give [name] [blockid] [blockqty]");
				//SwootyUtils.log("CONCMD", "		->");
				SwootyUtils.log("CONCMD", "		 time [set/get] [time in ms]");
				SwootyUtils.log("CONCMD", "      clear");
				SwootyUtils.log("CONCMD", "      quit");
				return false;
			case "clear":
				debugData = new ArrayList<String>();
				return true;
			case "time": 
				if (params.length > 1) {
					if (params[1].equals("set")) {
						String a = "0." + params[2];
						float d = Float.valueOf(a);
						if (d > 0.90f) {
							worldData.setWorldTime(0.90f, true);
						}else if (d < 0) {
							worldData.setWorldTime(0.0f, true);
						}else {
							worldData.setWorldTime(d, true);							
						}
						return true;
					}else if (params[1].equals("get")) {
						SwootyUtils.log("CONCMD", "World time is " + worldData.getWorldTime());
						return false;
					}else {
						SwootyUtils.log("CONCMD", "Usage: time [get/set] [time in ms]");
						return false;
					}
				}else {
					SwootyUtils.log("CONCMD", "Usage: time [get/set] [time in ms]");
					return false;
				}
			case "quit":
				SwootyUtils.log("CONCMD", "User forced program to terminate by Console.");
				System.exit(0);
				return true;
			}
			return true;
		}

		public String[] splitParams(String a) {
			if (a.toLowerCase().split("\\s+").length == 0) {
				String[] s = new String[1];
				s[0] = a;
				return s;
			}else {
				return a.toLowerCase().split("\\s+");
			}
		}

		@Override
		public void keyTyped(KeyEvent arg0) {

		}

		@Override
		public void mouseClicked(MouseEvent arg0) {


		}

		@Override
		public void mouseEntered(MouseEvent arg0) {


		}

		@Override
		public void mouseExited(MouseEvent arg0) {


		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if (arg0.getButton() == 1 && selectedBlock != null) {
				worldData.removeBlock(selectedBlock);
			}
			if (arg0.getButton() == 3 && selectedBlock != null) {
				if (worldData.getBlock(selectedBlock).getType() == SwootyUtils.BlockType.AIR) {
					SwootyUtils.BlockType b;
					if (worldData.getMyPlayer().getSelectedBlock() != null) {
						b = worldData.getMyPlayer().getSelectedBlock();
						worldData.placeBlock(selectedBlock, b);
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {


		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			if (worldData.getMyPlayer() != null && worldData.getMyPlayer().isAlive()) {
				if (arg0.getWheelRotation() > 0) {
					worldData.getMyPlayer().removeFromIndex();
				}else if (arg0.getWheelRotation() < 0) {
					worldData.getMyPlayer().addToIndex();
				}
			}
		}
	}

	public class NetworkModule implements Runnable {
		private boolean isServer;
		private String ip;
		private int port;
		private String password;
		private int id;
		private String username;

		private ServerSocket serverSocket;
		private Socket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private ArrayList<Block> output;
		private ArrayList<WorldDrop> dropsOut;
		private ArrayList<Block> input;
		private ArrayList<WorldDrop> dropsIn;
		private Player remotePlayer;
		private Point remoteLocation;
		private ArrayList<Player> players;
		private ArrayList<Point> playerLocations;
		private float worldTime;

		private int downloadProgress = 1;
		private boolean terminated = false;
		private boolean connected = false;
		private int overhead = 0;

		public NetworkModule(boolean isServer, String ip, int port, String password, String username) {
			this.isServer = isServer;
			this.ip = ip;
			this.port = port;
			this.password = password;
			this.username = username;
			this.id = username.hashCode();
			if (isServer) {
				SwootyUtils.log("Network Module", "Initialized ServerModule on " + ip + ":" + port + "...");
			}else {
				SwootyUtils.log("Network Module", "Initialized ClientModule on " + ip + ":" + port + "...");
			}
		}

		@Override
		public void run() {
			if (isServer) {
				if (!terminated) {
					try {
						try {
							serverSocket = new ServerSocket();
							serverSocket.bind(new InetSocketAddress(ip, port));
						} catch (IOException ex) {
							SwootyUtils.log("Network Module","Server could not be created. " + ex.getMessage());
							return;
						}
						SwootyUtils.log("Network Module","Waiting for connections...");
						socket = null;
						try {
							socket = serverSocket.accept();
						} catch (IOException ex) {
							SwootyUtils.log("Network Module","The Socket could not be read.");
							return;
						}
						SwootyUtils.log("Network Module","Connection created with Client (" + socket.getInetAddress() + ").");
						while (!terminated) {
							connected = socket.isConnected();
							if (out == null) {
								out = new ObjectOutputStream(socket.getOutputStream());
								SwootyUtils.log("Network Module", "Uploading terrain to remote client...");
								for (int i = worldData.getChunks().length - 1; i >= 0; i--) {
									Chunk c = worldData.getChunks()[i];
									out.writeObject(c);
								}
								SwootyUtils.log("Network Module", "Upload complete!");
							}
							if (in == null) {
								in = new ObjectInputStream(socket.getInputStream());
							}
							//Read changes first
							input = (ArrayList<Block>)in.readObject();
							dropsIn = (ArrayList<WorldDrop>)in.readObject();
							remotePlayer = (Player)in.readObject();
							remoteLocation = (Point)in.readObject();
							decode();

							//Write changes back
							encode();
							out.writeObject(output);
							out.writeObject(dropsOut);
							out.writeObject(players);
							out.writeObject(playerLocations);
							out.writeObject(worldData.getWorldTime());
						}
					}catch (Exception ex) {
						if (!terminated) {
							if (ex.getMessage() != null) { 
								SwootyUtils.log("Network Module", ex.getMessage());
								ex.printStackTrace();
							}else{
								ex.printStackTrace();
							}
							terminated = true;
						}
						try {
							out.flush();
						}catch (Exception e) {
							terminated = true;
						}
					}
				}
			}else {
				if (!terminated) {
					try {
						boolean foundPort = false;
						Exception p = null;
						int portEnd = port + SwootyUtils.NUM_SLOTS;
						int portCurr = port;
						while (portCurr < portEnd) {
							try {
								socket = new Socket(ip, portCurr);
								foundPort = true;
								SwootyUtils.log("Network Module","Connection Established with Server (" + socket.getInetAddress() + ")");
								break;
							} catch (Exception ex) {
								p = ex;
							}
							portCurr++;
						}
						if (!foundPort) {
							SwootyUtils.log("Network Module","Connection failed due to an error: " + p.getMessage());
						}
						while (!terminated) {
							connected = socket.isConnected();
							if (out == null) {
								out = new ObjectOutputStream(socket.getOutputStream());
							}
							if (in == null) {
								in = new ObjectInputStream(socket.getInputStream());
								SwootyUtils.log("Network Module", "Downloading terrain...");
								int index = SwootyUtils.CHUNK_QTY - 1;
								Chunk c = (Chunk)in.readObject();
								while (index >= 0) {
									worldData.setIndex(index, c);
									if (index == 0) {
										break;
									}
									c = (Chunk)in.readObject();
									downloadProgress++;
									index--;
								}
								SwootyUtils.log("Network Module", "Download complete!");
								worldData.spawn();
							}
							//Write changes first
							encode();
							out.writeObject(output);
							out.writeObject(dropsOut);
							out.writeObject(worldData.getMyPlayer());
							out.writeObject(worldData.getMyPlayer().getPlayerCoordLocation());

							//Read changes back
							input = (ArrayList<Block>)in.readObject();
							dropsIn = (ArrayList<WorldDrop>)in.readObject();
							players = (ArrayList<Player>)in.readObject();
							playerLocations = (ArrayList<Point>)in.readObject();
							worldTime = (float)in.readObject();
							decode();
						}
					}catch (Exception ex) {
						if (!terminated) {
							if (ex.getMessage() != null) { 
								SwootyUtils.log("Network Module", ex.getMessage());
							}else{
								ex.printStackTrace();
							}
							terminated = true;
						}
						try {
							out.flush();
						}catch (Exception e) {
							terminated = true;
						}
					}
				}
			}
		}

		public void encode() {
			try {
				output = worldData.getQueue();
				dropsOut = worldData.getDrops();
				players = worldData.getPlayers();
				playerLocations = new ArrayList<Point>();
				for (int i = 0; i < players.size(); i++) {
					playerLocations.add(players.get(i).getPlayerCoordLocation());
				}
				overhead = output.size() + players.size();
			}catch (Exception e) {
				e.printStackTrace();
				SwootyUtils.log("Network Manager", "Failed to encode one or more network entities!");
			}
		}

		public void decode() {
			try {
				worldData.updateWorld(input);
				worldData.updateWorldDrops(dropsIn);
				if (!isServer) {
					worldData.setWorldTime(worldTime, false);
				}
				if (isServer && remotePlayer != null) {
					remotePlayer.setPlayerLocation(remoteLocation);
					worldData.updatePlayer(remotePlayer);
				}
				if (!isServer) {
					for (int i = 0; i < players.size(); i++) {
						if (!worldData.getMyPlayer().getName().equals(players.get(i).getName())) {
							players.get(i).setPlayerLocation(playerLocations.get(i));
						}
					}
					worldData.setPlayers(players);
				}
				input = null;
				players = null;
				playerLocations = null;
			}catch (Exception e) {
				e.printStackTrace();
				SwootyUtils.log("Network Manager", "Failed to decode one or more network entities!");
			}
		}

		public boolean isConnected() {
			return connected;
		}
	}
}
