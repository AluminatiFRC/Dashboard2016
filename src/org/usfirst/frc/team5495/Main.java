package org.usfirst.frc.team5495;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.events.MediaPlayerEventType;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {
	private JSONParser parser = new JSONParser();
	private MessageClient client;
	private Properties robotProperties;
	private PropertiesPanel propertiesPanel;

	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

	private void start() {
		loadVlcNatives();

		client = new MessageClient("tcp://10.54.95.85:5888");
		client.connect();
		
		JFrame frame = new JFrame("5495_Dashboard");

		EmbeddedMediaPlayerComponent videoPlayer = new EmbeddedMediaPlayerComponent();
		EmbeddedMediaPlayer player = videoPlayer.getMediaPlayer();
		//player.playMedia("http://roboRio-5495-FRC.local:5880/?action=stream", 
				//":network-caching=0", ":drop-late-frames", ":skip-frames", ":no-audio", ":live-caching=0");
		player.setOverlay(mkOverlayWindow(new TestOverlay()));
		// player.enableOverlay(true);
		frame.add(videoPlayer);
		
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setBorder(new EmptyBorder(4,4,4,4)); // For padding
		frame.add(sidePanel, BorderLayout.WEST);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		sidePanel.add(topPanel, BorderLayout.NORTH);
				
		JButton picture = new JButton("Take a Picture");
		picture.addActionListener((ActionEvent event) -> {
			client.publish("robot/vision/screenshot", "dummy");
		});
		topPanel.add(picture, BorderLayout.EAST);
		
		propertiesPanel = new PropertiesPanel(client);
		propertiesPanel.addSlider("target/distance/max", 250);
		propertiesPanel.addSlider("target/distance/min", 250);
		propertiesPanel.addSlider("target/rotation/speed", 1);
		propertiesPanel.addSlider("target/rotation/tolerance", .5);
		propertiesPanel.addSlider("target/distance/tolerance", .15);
		propertiesPanel.addSlider("drive/move-forward/speed", 1);
		propertiesPanel.addSlider("drive/crawl/speed", 1);
		sidePanel.add(propertiesPanel, BorderLayout.CENTER);
		
		JPanel diagnostics = new JPanel();
		diagnostics.setLayout(new BoxLayout(diagnostics, BoxLayout.X_AXIS));
		
		JTextArea telemetryDisplay = new JTextArea();
		telemetryDisplay.setLineWrap(true);
		telemetryDisplay.setSize(300, 400);
		diagnostics.add(telemetryDisplay);
		
		BarGraph graph = new BarGraph(0, 255, 50, 4);
		diagnostics.add(graph);
		
		client.addMessageListener("robot/vision/telemetry", (String message) -> {
//			System.out.println(message);
			JSONObject object = parse(message);
			telemetryDisplay.setText(message);
			boolean target = (boolean) object.get("hasTarget");
			if (target == true) {
				double distance = (Double) object.get("targetDistance");
				graph.setValue((int) distance);
				
				double horizError = ((Double) object.get("horizDelta")) - ((distance * .001406397) - .38978127940);
				telemetryDisplay.append("\nHorizontal Error: " + horizError);
			} else {
				graph.setValue((graph.getUpper()-graph.getLower())/2);
			}
			
		});
		
		frame.add(diagnostics, BorderLayout.EAST);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
				
		JMenuItem save = new JMenuItem("Save Properties...");
		save.addActionListener(ae -> {
			JFileChooser fc = new JFileChooser(Paths.get(".").toFile());
	        int returnVal = fc.showSaveDialog(frame);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	propertiesPanel.save(Paths.get(fc.getSelectedFile().toURI()));
	        } 
		});
		file.add(save);
		
		JMenuItem load = new JMenuItem("Load Properties...");
		load.addActionListener(ae -> {
			JFileChooser fc = new JFileChooser(Paths.get(".").toFile());
	        int returnVal = fc.showOpenDialog(frame);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	propertiesPanel.load(Paths.get(fc.getSelectedFile().toURI()));
	        } 
		});
		file.add(load);

		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(ae -> {
			frame.dispose();
		});
		file.add(quit);
		
		menuBar.add(file);
		
		frame.setJMenuBar(menuBar);
		frame.setUndecorated(true);
		frame.setSize(1600, 660);
		frame.setLocation(0, 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() { // Cleanup
            @Override
            public void windowClosed(WindowEvent e) {
				propertiesPanel.save();
				System.exit(0);
            }
		});
		frame.setVisible(true);
	}
	
	private JWindow mkOverlayWindow(JPanel overlayPanel) {
		JWindow window = new JWindow();
		window.setContentPane(overlayPanel);
		window.getRootPane().setOpaque(false);
		window.setBackground(new Color(0, 0, 0, 0));
		return window;
	}

	private void loadVlcNatives() {
		// This may have to change depending on your system.
		String nativePath = System.getProperty("os.arch").contains("64") ? "C:\\Program Files\\VideoLAN\\VLC"
				: "C:\\Program Files (x86)\\VideoLAN\\VLC";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), nativePath);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}

	public JSONObject parse(String message) {
		try {
			JSONObject result = (JSONObject) parser.parse(message);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
