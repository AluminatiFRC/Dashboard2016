package org.usfirst.frc.team5495;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

public class PropertiesPanel extends JPanel {
	private static final double SLIDER_RESOLUTION = .01;
	private JPanel controlLabels;
	private JPanel controlSliders;
	private JPanel controlValues;
	private MessageClient client;
	private Properties robotProperties;
	private Path propertiesPath;

	public PropertiesPanel(MessageClient client) {
		this.client = client;
		loadProperties();
		setupUI();

		addSlider("proximity", 100);
		addSlider("max-speed", 120);
		addSlider("targeting-rotation-rate", 1);
	}

	private void loadProperties() {
		robotProperties = new Properties();
		propertiesPath = FileSystems.getDefault().getPath("configuration","config.properties");
		
		try {
			if (!Files.exists(propertiesPath.getParent()))
				Files.createDirectories(propertiesPath.getParent());
			if (!Files.exists(propertiesPath))
				Files.createFile(propertiesPath);
			
			robotProperties.load(Files.newBufferedReader(propertiesPath));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading properties file");
		}
	}

	private void setupUI() {
		controlLabels = new JPanel();
		controlLabels.setLayout(new BoxLayout(controlLabels, BoxLayout.Y_AXIS));
		add(controlLabels, BorderLayout.WEST);

		controlSliders = new JPanel();
		controlSliders.setLayout(new BoxLayout(controlSliders, BoxLayout.Y_AXIS));
		add(controlSliders);

		controlValues = new JPanel();
		controlValues.setLayout(new BoxLayout(controlValues, BoxLayout.Y_AXIS));
		add(controlValues, BorderLayout.EAST);
	}

	private void addSlider(String name, float maximum) {
		JLabel label = new JLabel();
		label.setText(name + ": ");
		controlLabels.add(label);

		JSlider slider = new JSlider();
		slider.setMaximum((int) (maximum * 100));
		controlSliders.add(slider);

		JLabel valueLabel = new JLabel();
		valueLabel.setPreferredSize(new Dimension(40, valueLabel.getHeight()));
		controlValues.add(valueLabel);

		slider.addChangeListener((ChangeEvent event) -> {
			double newValue = slider.getValue() * SLIDER_RESOLUTION;
			String valueString = String.valueOf(newValue);
			client.publishPorperty(name, newValue);
			valueLabel.setText(valueString);
			robotProperties.put(name, valueString);
		});
		
		// Call this after we register the change listener to update its dependencies on load 
		slider.setValue((int) (getProperty(name) / SLIDER_RESOLUTION));
	}

	private double getProperty(String name) {
		return Double.valueOf(robotProperties.getProperty(name, "0.0"));
	}

	public void save() {
		save(propertiesPath);
	}
	
	public void save(Path destination) {
		try {
			robotProperties.store(Files.newOutputStream(destination), "Robot properties FRC 5495");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error saving properties");
		}
	}
}
