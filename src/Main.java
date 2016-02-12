import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.events.MediaPlayerEventType;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {
	int targetOffset = 0;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

	private void start() {
		loadVlcNatives();
		
		JFrame frame = new JFrame("5495_Dashboard");

		EmbeddedMediaPlayerComponent videoPlayer = new EmbeddedMediaPlayerComponent();
		frame.add(videoPlayer);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(ae -> { System.exit(0); });
		file.add(quit);
		menuBar.add(file);
		
		frame.setJMenuBar(menuBar);
		frame.setUndecorated(true);
		frame.setSize(1600, 660);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		EmbeddedMediaPlayer player = videoPlayer.getMediaPlayer();
		player.playMedia("http://roboRio-5495-FRC.local:5880/?action=stream", ":network-caching=0");
		player.setOverlay(mkOverlayWindow(new TestOverlay()));
		player.enableOverlay(true);
		
		try {
			MqttClient mqtt = new MqttClient("tcp://roboRIO-5495-FRC.local:5888", UUID.randomUUID().toString().substring(0,20), new MemoryPersistence());
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
	        mqtt.connect(connOpts);
	        
	        mqtt.setCallback(new MqttCallback(){
				@Override
				public void connectionLost(Throwable arg0) {
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken arg0) {
				}

				@Override
				public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
					if (arg0.contentEquals("5495.targetting")) {
						System.out.println("targetting: " + new String(arg1.getPayload()));
						//this.targetOffset = Integer.parseInt(new String(arg1.getPayload())); 
					} else {
						System.out.print("Message Recieved: ");
						System.out.println(new String(arg1.getPayload()));
					}
				}            
            });
	        mqtt.subscribe("Testing");
	        mqtt.subscribe("5495.targetting");
	        
	        mqtt.publish("Testing", new MqttMessage("Testing".getBytes()));
	        System.out.println("Connected to the MQTT butler.");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JWindow mkOverlayWindow(JPanel overlayPanel){
		JWindow window = new JWindow();
		window.setContentPane(overlayPanel);
		window.getRootPane().setOpaque(false);
		window.setBackground(new Color(0,0,0,0));
		return window;
	}

	private void loadVlcNatives(){
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}
}
