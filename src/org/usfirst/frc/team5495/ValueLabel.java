package org.usfirst.frc.team5495;
import javax.swing.JLabel;

public class ValueLabel extends JLabel{
	private String parameterName;
	private float value;
	
	public ValueLabel(String parameter) {
		parameterName = parameter;
	}	
	public void setValue(float newvalue) {
		setText(parameterName + ":" + newvalue);
		value = newvalue;
	}
	public float getValue() {
		return value;
	}
}