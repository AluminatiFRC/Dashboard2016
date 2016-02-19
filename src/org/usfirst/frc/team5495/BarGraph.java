package org.usfirst.frc.team5495;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

public class BarGraph extends JComponent {
	private double lower;
	private double upper;
	private double target;
	private double value;
	private double interval;
	
	public BarGraph (double lower, double upper) {
		this(lower, upper, ((upper - lower + 1)/10.0));
	}
		
	public BarGraph (double lower, double upper, double interval) {
		if (interval <= 0) {
			throw new IllegalArgumentException("Interval must be a positive value above zero");
		}
		this.setPreferredSize(new Dimension(50,200));
		this.lower = lower;
		this.upper = upper;
		this.target = this.value = this.lower;
		this.interval = interval;
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
	    int valueHeight = 6;
		int targetHeight = 6;
		int graphInset = 5;
		int barWidth = getWidth() - graphInset * 2;
		int barHeight = getHeight() - graphInset * 2;
		double range = upper - lower;
		double targetPercent = (target - lower) / (float)(range);
		double valuePercent = (value - lower) / (float)(range);
		int targetY = barHeight - (int) Math.round(targetPercent * barHeight) + graphInset - (targetHeight / 2);
		int valueY = barHeight - (int) Math.round(valuePercent * barHeight) + graphInset - (valueHeight / 2);
		
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
		
		//calculate the tick marks
		double p2URatio = barHeight/range;
		double pix = interval*p2URatio;
		
		g.setColor(Color.ORANGE);
		
		Stroke tick = new BasicStroke(2.0f);
		g.setStroke(tick);
		int yStart = getHeight() - graphInset;
		for (int i = 1;i <= (barHeight/pix); i++) {
			g.drawLine(graphInset, yStart-(int) (i*pix), graphInset + (barWidth/2), yStart-(int) (i*pix));
		}
		
		// Draw the border
		Stroke border = new BasicStroke(1.0f);
		g.setStroke(border);
		g.setColor(Color.BLACK);
		g.drawRect(graphInset, graphInset, barWidth, barHeight);
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}

	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
	    target = Math.min(target, upper);
	    target = Math.max(target, lower);
		this.target = target;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		value = Math.min(value,  upper);
		value = Math.max(value, lower);
		this.value = value;
		this.repaint();
	}
}
