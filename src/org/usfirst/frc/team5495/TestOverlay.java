package org.usfirst.frc.team5495;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class TestOverlay extends JPanel {
	@Override
	public void paintComponent(Graphics graphics){
		Graphics2D g = (Graphics2D) graphics;
		
		g.fillRect(100, 100, 100, 100);
	}
	
}
