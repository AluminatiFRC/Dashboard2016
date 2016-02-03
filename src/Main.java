import java.awt.BorderLayout;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Main {

	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

	private void start() {
		loadVlcNatives();
		
		JFrame frame = new JFrame("5495_Dashboard");
		
		float value = 999999901;
		
		ValueLabel label2 = new ValueLabel("Your Cookie Count");
		label2.setValue(value);
		frame.add(label2, BorderLayout.WEST);
		
		JLabel label = new JLabel("GET TO 15,000");
		frame.add(label, BorderLayout.NORTH);
		
		JButton button2 = new JButton("CLICK FOR COOKIES");
		button2.addActionListener(event -> {
			label2.setValue(label2.getValue() + 15);
		});
		frame.add(button2);
		
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void loadVlcNatives(){
		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files (x86)\\VideoLAN\\VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}
}
