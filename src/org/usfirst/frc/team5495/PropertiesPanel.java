package org.usfirst.frc.team5495;

import java.awt.BorderLayout;
import java.awt.Component;
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

/**
 * Manages a properties file and some sliders to control it. Also publishes these values 
 * to an MQTT topic. This class should most definitely have the model split from the view.
 * @author Ethan
 */

public class PropertiesPanel extends JPanel {
	private static final double SLIDER_RESOLUTION = .01;
	private static final String LABEL_FORMAT = "%.2f";
	private JPanel controlLabels;
	private JPanel controlSliders;
	private JPanel controlValues;
	private MessageClient client;
	private Properties propertiesFile;
	private Path DEFAULT_PATH = FileSystems.getDefault().getPath("configuration","config.properties");

	public PropertiesPanel(MessageClient client) {
		this.client = client;
		setupUI();
		load(DEFAULT_PATH);
	}

	private void setupUI() {
		setLayout(new BorderLayout());
		
		controlLabels = new JPanel();
		controlLabels.setLayout(new BoxLayout(controlLabels, BoxLayout.Y_AXIS));
		add(controlLabels, BorderLayout.WEST);

		controlSliders = new JPanel();
		controlSliders.setLayout(new BoxLayout(controlSliders, BoxLayout.Y_AXIS));
		add(controlSliders, BorderLayout.CENTER);

		controlValues = new JPanel();
		controlValues.setLayout(new BoxLayout(controlValues, BoxLayout.Y_AXIS));
		add(controlValues, BorderLayout.EAST);
	}
	
	public void addSlider(String name, double maximum) {
		JLabel label = new JLabel();
		label.setText(name + ": ");
		controlLabels.add(label);

		JSlider slider = new JSlider();
		slider.setMaximum((int) (maximum * 100));
		slider.setName(name);
		controlSliders.add(slider);

		JLabel valueLabel = new JLabel();
		valueLabel.setPreferredSize(new Dimension(40, 10));
		controlValues.add(valueLabel);

		slider.addChangeListener((ChangeEvent event) -> {
			double newValue = slider.getValue() * SLIDER_RESOLUTION;
			String valueString = String.valueOf(newValue);
			client.publishPorperty(name, newValue);			
			valueLabel.setText(String.format(LABEL_FORMAT, newValue));
			propertiesFile.put(name, valueString);
		});
		
		// Call this after we register the change listener to update its dependencies on load 
		slider.setValue((int) (getProperty(name) / SLIDER_RESOLUTION));
	}

	private double getProperty(String name) {
		return Double.valueOf(propertiesFile.getProperty(name, "0.0"));
	}

	public void save() {
		save(DEFAULT_PATH);
	}
	
	public void save(Path destination) {
		try {
			propertiesFile.store(Files.newOutputStream(destination), "Robot properties FRC 5495");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error saving properties");
		}
	}

	public void load(Path path) {
		propertiesFile = new Properties();
		
		try {
			if (!Files.exists(path.getParent()))
				Files.createDirectories(path.getParent());
			if (!Files.exists(path))
				Files.createFile(path);
			
			propertiesFile.load(Files.newBufferedReader(path));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading properties file");
		}
		
		for (Component component : controlSliders.getComponents()){
			JSlider slider = (JSlider) component;
			int value = (int) (getProperty(slider.getName()) / SLIDER_RESOLUTION);
			slider.setValue(value);
		}
	}
}
