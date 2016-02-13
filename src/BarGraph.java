import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

public class BarGraph extends JComponent {
	private int lower;
	private int upper;
	private int target;
	private int value;
	
	public BarGraph (int lower, int upper) {
		this.setPreferredSize(new Dimension(100,100));
		this.lower = lower;
		this.upper = upper;
		this.target = this.value = this.lower;
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
	    int valueHeight = 6;
		int targetHeight = 6;
		int graphInset = 10;
		int barWidth = getWidth() - graphInset * 2;
		int barHeight = getHeight() - graphInset * 2;
		int range = upper - lower;
		float targetPercent = (target - lower) / (float)(range);
		float valuePercent = (value - lower) / (float)(range);
		int targetY = barHeight - Math.round(targetPercent * barHeight) + graphInset - (targetHeight / 2);
		int valueY = barHeight - Math.round(valuePercent * barHeight) + graphInset - (valueHeight / 2);
		
		Graphics2D g = (Graphics2D) graphics;
	
		// Draw the background/fill color
		g.setColor(Color.BLUE);
		g.fillRect(graphInset, graphInset, barWidth, barHeight);
		
		// Draw the target indicator
		g.setColor(Color.RED);
		g.fillRect(graphInset, targetY, barWidth, targetHeight);
		
		// Draw the value indicator
		g.setColor(Color.GREEN);
		g.fillRect(graphInset, valueY, barWidth, valueHeight);
		
		// Draw the border
		g.setColor(Color.BLACK);
		g.drawRect(graphInset, graphInset, barWidth, barHeight);
	}

	public int getLower() {
		return lower;
	}

	public int getUpper() {
		return upper;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
	    target = Math.min(target, upper);
	    target = Math.max(target, lower);
		this.target = target;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		value = Math.min(value,  upper);
		value = Math.max(value, lower);
		this.value = value;
	}
}
