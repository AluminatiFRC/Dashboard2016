package org.usfirst.frc.team5495;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JWindow;
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
	int targetOffset = 0;
	JSONParser parser = new JSONParser();
	private JPanel controlLabels;
	private JPanel controlSliders;
	private JPanel controlValues;
	private MessageClient client;

	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

	private void start() {
		loadVlcNatives();


		client = new MessageClient("tcp://roboRIO-5495-FRC.local:5888");
		client.connect();
		
		JFrame frame = new JFrame("5495_Dashboard");

		EmbeddedMediaPlayerComponent videoPlayer = new EmbeddedMediaPlayerComponent();
		EmbeddedMediaPlayer player = videoPlayer.getMediaPlayer();
		//player.playMedia("http://roboRio-5495-FRC.local:5880/?action=stream", 
				//":network-caching=0", ":drop-late-frames", ":skip-frames", ":no-audio", ":live-caching=0");
		player.setOverlay(mkOverlayWindow(new TestOverlay()));
		// player.enableOverlay(true);
		frame.add(videoPlayer);

		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(ae -> {
			System.exit(0);
		});
		file.add(quit);
		menuBar.add(file);
		
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));
		frame.add(cPanel, BorderLayout.WEST);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		cPanel.add(buttonPanel);
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		cPanel.add(controls);
		
		controlLabels = new JPanel();
		controlLabels.setLayout(new BoxLayout(controlLabels, BoxLayout.Y_AXIS));
		controls.add(controlLabels, BorderLayout.EAST);
		
		controlSliders = new JPanel();
		controlSliders.setLayout(new BoxLayout(controlSliders, BoxLayout.Y_AXIS));
		controls.add(controlSliders);
		
		controlValues = new JPanel();
		controlValues.setLayout(new BoxLayout(controlValues, BoxLayout.Y_AXIS));
		controls.add(controlValues, BorderLayout.WEST);
		
		
		
		JButton picture = new JButton("Take A Picture");
		picture.addActionListener((ActionEvent takePicture) -> {
			client.publish("robot/vision/screenshot", "picklePayload");
		});
		buttonPanel.add(picture, BorderLayout.EAST);
		
		addSlider("proximity", 100);
		addSlider("max-speed", 120);
		addSlider("targeting-rotation-rate", 1);
		
		BarGraph graph = new BarGraph(0, 255, 50);
		frame.add(graph, BorderLayout.EAST);
		client.addMessageListener("robot/vision/telemetry", (String message) -> {
			System.out.println(message);
			JSONObject object = parse(message);
			boolean target = (boolean) object.get("hasTarget");
			if (target == true) {
				double distance = (Double) object.get("targetDistance");
				graph.setValue((int) distance);
			} else {
				graph.setValue((graph.getUpper()-graph.getLower())/2);
			}
		});
		
		frame.setJMenuBar(menuBar);
		frame.setUndecorated(true);
		frame.setSize(1600, 660);
		frame.setLocation(0, 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void addSlider(String name, int maximum){
		JLabel label = new JLabel();
		label.setText(name + ": ");
		controlLabels.add(label);
				
		JSlider slider = new JSlider();
		slider.setMaximum(maximum * 100);
		controlSliders.add(slider);
		
		JLabel valueLabel = new JLabel();
		valueLabel.setText(String.valueOf(slider.getValue() / 100.0));
		valueLabel.setPreferredSize(new Dimension(40, valueLabel.getHeight()));
		controlValues.add(valueLabel);
		
		slider.addChangeListener((ChangeEvent event) -> {
			client.publish("robot/setting/" + name, String.valueOf((slider.getValue() / 100.0)));
			valueLabel.setText(String.valueOf(slider.getValue() / 100.0));
		});
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
