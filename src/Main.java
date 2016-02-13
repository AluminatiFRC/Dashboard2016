import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JWindow;

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
		
		BarGraph graph = new BarGraph(0, 255);
		frame.add(graph,BorderLayout.EAST);
		
		frame.setJMenuBar(menuBar);
		frame.setUndecorated(true);
		frame.setSize(1600, 660);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		EmbeddedMediaPlayer player = videoPlayer.getMediaPlayer();
		player.playMedia("http://roboRio-5495-FRC.local:5880/?action=stream",
				"network-caching=0", "drop-late-frames","skip-frames");
		player.setOverlay(mkOverlayWindow(new TestOverlay()));
		//player.enableOverlay(true);
		
		MessageClient client = new MessageClient("tcp://roboRIO-5495-FRC.local:5888");
		client.addMessageListener("5495.targetting", (String message) -> {
			System.out.println(message);
		});
		client.connect();
		
		client.publish("Testing", "Testing");
	}
	
	private JWindow mkOverlayWindow(JPanel overlayPanel){
		JWindow window = new JWindow();
		window.setContentPane(overlayPanel);
		window.getRootPane().setOpaque(false);
		window.setBackground(new Color(0,0,0,0));
		return window;
	}

	private void loadVlcNatives(){
		//This may have to change depending on your system.
		String nativePath = System.getProperty("os.arch").contains("64") ?  
				"C:\\Program Files\\VideoLAN\\VLC" :  "C:\\Program Files (x86)\\VideoLAN\\VLC";
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), nativePath);
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}
}
