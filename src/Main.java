import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

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
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {

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
