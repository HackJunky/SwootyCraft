import javax.swing.JFrame;
import javax.swing.JLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JProgressBar;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class StartupFrame extends JFrame {
	private static final long serialVersionUID = -2794438895707041418L;
	private JTextField txtWorldName;
	private JTextField txtHostIP;
	private JTextField txtHostPort;
	private JTextField txtHostPass;
	private JTextField txtServerIP;
	private JTextField txtServerPort;
	private JTextField txtServerPass;
	private JLabel lblStatus;
	private JLabel lblNetStatus;
	private JCheckBox chkSingleplayer;
	private JCheckBox chkHost;
	private JCheckBox chkConnect;
	private JPanel singlePanel;
	private JPanel connectPanel;
	private JPanel startPanel;
	private JPanel hostPanel;
	private JCheckBox singleAllowConsole;
	private JCheckBox singleAutoSave; 
	private JCheckBox singleDevMode;
	private JCheckBox multiDevMode;
	private JButton btnStartGame;

	private UIIterator uii = new UIIterator();
	private Timer UIWorker = new Timer(1, uii);

	private String version = "Alpha 0.2b";

	String username;
	String[] usernamePrefix = {"Red", "Orange", "Green", "Blue", "Yellow", "Purple"};
	String[] usernameSuffix = {"Cat", "Dog", "Wolf", "Rocket", "Sheep", "Goat"};

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				StartupFrame sf = new StartupFrame();
			}
		});
	}

	public StartupFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Register Font
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("data/MCFont.ttf")));
			System.out.println("Registered 1 new font with Java Native. MCFont.ttf loaded with a total of " + ge.getAllFonts().length + " font(s).");
		} catch (Exception e) {
			System.out.println("Could not locate MCFont file in the DATA directory!");
		}

		//Form Setups (Generated)
		setResizable(false);
		setType(Type.POPUP);
		setTitle("SwootyCraft Launcher");

		JLabel lblConfigureALocal = new JLabel("Configure a local or network SwootyCraft session below. This is version " + version + ".");
		lblConfigureALocal.setFont(new Font("Minecraft Regular", Font.ITALIC, 11));

		singlePanel = new JPanel();
		singlePanel.setBorder(new LineBorder(new Color(0, 0, 0)));

		startPanel = new JPanel();
		startPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

		connectPanel = new JPanel();
		connectPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

		hostPanel = new JPanel();
		hostPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

		chkSingleplayer = new JCheckBox("Singleplayer (Local) Game:");
		chkSingleplayer.setSelected(true);
		chkSingleplayer.setFont(new Font("Minecraft Regular", Font.BOLD, 10));

		chkHost = new JCheckBox("Host a Multiplayer (Networked) Game:");
		chkHost.setFont(new Font("Minecraft Regular", Font.BOLD, 10));

		chkConnect = new JCheckBox("Connect to a Multiplayer (Networked) Game:");
		chkConnect.setFont(new Font("Minecraft Regular", Font.BOLD, 10));
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
						.addGap(10)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(startPanel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 614, Short.MAX_VALUE)
								.addComponent(singlePanel, GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
								.addComponent(chkHost, Alignment.LEADING)
								.addComponent(connectPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
								.addComponent(chkConnect, Alignment.LEADING)
								.addComponent(chkSingleplayer, Alignment.LEADING)
								.addComponent(lblConfigureALocal, Alignment.LEADING)
								.addComponent(hostPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
								.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblConfigureALocal)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chkSingleplayer)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(singlePanel, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chkHost)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(hostPanel, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chkConnect)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(connectPanel, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(startPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(20, Short.MAX_VALUE))
				);
		hostPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JLabel lblHostIp = new JLabel("Host IP:");
		hostPanel.add(lblHostIp, "2, 2, right, default");

		txtHostIP = new JTextField();
		hostPanel.add(txtHostIP, "4, 2, fill, default");
		txtHostIP.setColumns(10);

		JLabel lblPort = new JLabel("Port:");
		hostPanel.add(lblPort, "2, 4, right, default");

		txtHostPort = new JTextField();
		hostPanel.add(txtHostPort, "4, 4, fill, default");
		txtHostPort.setColumns(10);

		multiDevMode = new JCheckBox("Enable Developer Mode");
		hostPanel.add(multiDevMode, "6, 4");

		JLabel lblHelpfulNetworkAnd = new JLabel("helpful network info");
		lblHelpfulNetworkAnd.setFont(new Font("Minecraft Regular", Font.ITALIC, 9));
		hostPanel.add(lblHelpfulNetworkAnd, "7, 4, fill, default");

		JLabel lblPassword = new JLabel("Password:");
		hostPanel.add(lblPassword, "2, 6, right, default");

		txtHostPass = new JTextField();
		hostPanel.add(txtHostPass, "4, 6, fill, default");
		txtHostPass.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(150);
		hostPanel.add(horizontalStrut, "4, 8");
		connectPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));

		JLabel lblServerIp = new JLabel("Server IP:");
		connectPanel.add(lblServerIp, "2, 2, right, default");

		txtServerIP = new JTextField();
		connectPanel.add(txtServerIP, "4, 2, fill, default");
		txtServerIP.setColumns(10);

		JLabel lblPort_1 = new JLabel("Port:");
		connectPanel.add(lblPort_1, "8, 2, right, default");

		txtServerPort = new JTextField();
		connectPanel.add(txtServerPort, "10, 2, fill, default");
		txtServerPort.setColumns(10);

		JLabel lblServerPort = new JLabel("Password:");
		connectPanel.add(lblServerPort, "2, 4, right, default");

		txtServerPass = new JTextField();
		connectPanel.add(txtServerPass, "4, 4, fill, default");
		txtServerPass.setColumns(10);

		JLabel lblNewLabel = new JLabel("Status:");
		connectPanel.add(lblNewLabel, "8, 4, right, default");

		lblNetStatus = new JLabel("Waiting...");
		connectPanel.add(lblNetStatus, "10, 4");
		startPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));

		JButton btnQuitLauncher = new JButton("Quit Launcher");
		startPanel.add(btnQuitLauncher, "2, 2");

		btnStartGame = new JButton("Start Game!");
		startPanel.add(btnStartGame, "2, 4");

		JLabel lblStatusHigh = new JLabel("Status:");
		startPanel.add(lblStatusHigh, "4, 4");

		lblStatus = new JLabel("Awaiting user configuration....");
		startPanel.add(lblStatus, "6, 4");
		singlePanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
				new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JLabel lblWorldName = new JLabel("World Name: ");
		singlePanel.add(lblWorldName, "2, 2, right, default");

		txtWorldName = new JTextField();
		singlePanel.add(txtWorldName, "4, 2, 3, 1, fill, fill");
		txtWorldName.setColumns(10);

		JButton loadWorld = new JButton("Load");
		loadWorld.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				File f = new File("saves/" + txtWorldName.getText() + "/" + txtWorldName.getText() + ".save");
				if (f.exists()) {
					txtWorldName.setText("saves/" + txtWorldName.getText() + "/" + txtWorldName.getText() + ".save");
				}else {
					JOptionPane.showMessageDialog(StartupFrame.this, "No such World save exists!", "World Loader", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		singlePanel.add(loadWorld, "8, 2");

		singleAllowConsole = new JCheckBox("Allow Console Input");
		singlePanel.add(singleAllowConsole, "4, 4, left, default");

		JLabel lblAllowsForCheats = new JLabel("allows for cheats");
		lblAllowsForCheats.setFont(new Font("Minecraft Regular", Font.ITALIC, 9));
		singlePanel.add(lblAllowsForCheats, "6, 4, fill, default");

		singleAutoSave = new JCheckBox("Auto-Save on Close");
		singlePanel.add(singleAutoSave, "4, 6");

		JLabel lblPreventsGameData = new JLabel("prevents game data loss/corruption");
		lblPreventsGameData.setFont(new Font("Minecraft Regular", Font.ITALIC, 9));
		singlePanel.add(lblPreventsGameData, "6, 6, fill, default");

		singleDevMode = new JCheckBox("Enable Developer Mode");
		singlePanel.add(singleDevMode, "4, 8");

		JLabel lblShowsLotsOf = new JLabel("shows in-game statistics");
		lblShowsLotsOf.setFont(new Font("Minecraft Regular", Font.ITALIC, 9));
		singlePanel.add(lblShowsLotsOf, "6, 8, fill, default");
		getContentPane().setLayout(groupLayout);

		btnStartGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (chkSingleplayer.isSelected()) {
					String worldName = txtWorldName.getText();
					boolean allowInput = singleAllowConsole.isSelected();
					boolean autoSave = singleAutoSave.isSelected();
					boolean devMode = singleDevMode.isSelected();

					boolean isLoaded = false;
					if (worldName.contains("\\")) {
						isLoaded = true;
					}else if (worldName.contains("/")) {
						isLoaded = true;
					}else {
						isLoaded = false;
					}
					GraphicalEnvironment ge = new GraphicalEnvironment(devMode, false, false, "", -1, "", username, allowInput, autoSave, worldName, isLoaded);
				}else if (chkHost.isSelected()) {
					String hostIP = txtHostIP.getText();
					String hostPort = txtHostPort.getText();
					String hostPass = txtHostPass.getText();
					boolean devMode = multiDevMode.isSelected();

					GraphicalEnvironment ge = new GraphicalEnvironment(devMode, true, true, hostIP, Integer.valueOf(hostPort), hostPass, username, false, false, null, false);
				}else if (chkConnect.isSelected()) {
					String serverIP = txtServerIP.getText();
					String serverPort = txtServerPort.getText();
					String serverPass = txtServerPass.getText();

					GraphicalEnvironment ge = new GraphicalEnvironment(false, true, false, serverIP, Integer.valueOf(serverPort), serverPass, username, false, false, null, false);
				}
				StartupFrame.this.setVisible(false);
				StartupFrame.this.dispose();
			}
		});
		btnQuitLauncher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		System.out.println("In-page controls loaded! Loading on runtime font...");

		int i = 0;

		for (Component c : this.getContentPane().getComponents()) {
			if (c instanceof JPanel) {
				for (Component a : ((JPanel) c).getComponents()) {
					try {
						int newSize = -1;
						if (a.getFont().getSize() > 9) {
							newSize = a.getFont().getSize() - 2;
						}else {
							newSize = a.getFont().getSize();
						}
						a.setFont(new Font("Minecraft Regular", a.getFont().getStyle(), newSize));
						i++;
					}catch (Exception e) {
						a.setFont(new Font("Minecraft Regular", Font.PLAIN, 9));
						i++;
					}
					a.validate();
					a.repaint();
				}
			}
		}

		System.out.println("Applied Minecraft Regular to " + i + " unique text object(s).");

		this.validate();
		this.repaint();

		this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - 320, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - 240);
		this.setSize(640, 480);
		this.setVisible(true);

		while (username == null) {
			username = JOptionPane.showInputDialog(this, "Enter a Username for this session:", usernamePrefix[new Random().nextInt(usernamePrefix.length)] + usernameSuffix[new Random().nextInt(usernamePrefix.length)] + new Random().nextInt(9999));
		}

		UIWorker.start();
	}


	public class UIIterator implements ActionListener {
		private JCheckBox checked;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (checked == null) {
				if (chkSingleplayer.isSelected()) {
					checked = chkSingleplayer;
					toggleState(singlePanel, true);
				}else {
					toggleState(singlePanel, false);
				}

				if (chkHost.isSelected()) {
					checked = chkHost;
					toggleState(hostPanel, true);
				}else {
					toggleState(hostPanel, false);
				}

				if (chkConnect.isSelected()) {
					checked = chkConnect;
					toggleState(connectPanel, true);
				}else {
					toggleState(connectPanel, false);
				}
			}
			if (checked != null) {
				if (chkSingleplayer.isSelected() && checked != chkSingleplayer) {
					checked.setSelected(false);
					checked = chkSingleplayer;
					toggleState(singlePanel, true);
				}else if (chkSingleplayer.isSelected()) {
					toggleState(singlePanel, true);
				}else {
					toggleState(singlePanel, false);
				}

				if (chkHost.isSelected() && checked != chkHost) {
					checked.setSelected(false);
					checked = chkHost;
					toggleState(hostPanel, true);
				}else if (chkHost.isSelected()) {
					toggleState(hostPanel, true);
				}else {
					toggleState(hostPanel, false);
				}

				if (chkConnect.isSelected() && checked != chkConnect) {
					checked.setSelected(false);
					checked = chkConnect;
					toggleState(connectPanel, true);
				}else if (chkConnect.isSelected()) {
					toggleState(connectPanel, true);
				}else {
					toggleState(connectPanel, false);
				}
			}

			if (chkSingleplayer.isSelected() && !chkHost.isSelected() && !chkConnect.isSelected()) {
				if (txtWorldName.getText().length() > 0) {
					if (singleAutoSave.isSelected()) {
						lblStatus.setText("Ready to launch a local game!");
					}else {
						lblStatus.setText("While not required, Autosave is recommended.");
					}
					btnStartGame.setEnabled(true);
				}else{
					lblStatus.setText("Please enter a World Name.");
					btnStartGame.setEnabled(false);
				}
				toggleState(startPanel, true);
			}else if (!chkSingleplayer.isSelected() && chkHost.isSelected() && !chkConnect.isSelected()) {
				int charCount = txtHostIP.getText().replaceAll("[^.]", "").length();
				if (charCount == 3) {
					if (txtHostPort.getText().length() > 0) {
						if (txtHostPass.getText().length() > 0) {
							lblStatus.setText("Ready to host a network game!");
						}else{
							lblStatus.setText("While not required, a password is recommended.");
						}
						btnStartGame.setEnabled(true);
					}else{
						lblStatus.setText("Please enter a valid Port.");
						btnStartGame.setEnabled(false);
					}
				}else if (txtHostIP.getText().equals("localhost")) {
					if (txtHostPort.getText().length() > 0) {
						if (txtHostPass.getText().length() > 0) {
							lblStatus.setText("Ready to host a network game!");
						}else{
							lblStatus.setText("While not required, a password is recommended.");
						}
						btnStartGame.setEnabled(true);
					}else{
						lblStatus.setText("Please enter a valid Port.");
						btnStartGame.setEnabled(false);
					}
				}else {
					lblStatus.setText("Please enter a valid IP.");
					btnStartGame.setEnabled(false);
				}
				toggleState(startPanel, true);
			}else if (!chkSingleplayer.isSelected() && !chkHost.isSelected() && chkConnect.isSelected()) {
				int charCount = txtServerIP.getText().replaceAll("[^.]", "").length();
				if (charCount == 3) {
					if (txtServerPort.getText().length() > 0) {
						lblStatus.setText("Ready to connect to the network game!");
						btnStartGame.setEnabled(true);
					}else {
						lblStatus.setText("Please enter a valid Port.");
						btnStartGame.setEnabled(false);
					}
				}else if (txtServerIP.getText().equals("localhost")) {
					if (txtServerPort.getText().length() > 0) {
						lblStatus.setText("Ready to connect to the network game!");
						btnStartGame.setEnabled(true);
					}else {
						lblStatus.setText("Please enter a valid Port.");
						btnStartGame.setEnabled(false);
					}
				}else {
					lblStatus.setText("Please enter a valid IP.");
					btnStartGame.setEnabled(false);
				}
				toggleState(startPanel, true);
			}else {
				toggleState(startPanel, false);
			}
		}

		public void toggleState (JPanel p, boolean state) {
			for (Component c : p.getComponents()) {
				c.setEnabled(state);
			}
		}
	}
}
