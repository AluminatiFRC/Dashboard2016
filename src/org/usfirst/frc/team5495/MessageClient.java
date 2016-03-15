package org.usfirst.frc.team5495;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageClient implements MqttCallback {
	private static final int CONNECT_RETRY_TIME_MS = 1000;
	private String brokerAddress;
	private MqttClient client;
	private MemoryPersistence persistance; // needed for mqtt
	private ConnectionState state = ConnectionState.DISCONNECTED;
	
	private static final String PROPERTIES_TOPIC = "robot/properties/";
	
	private HashMap<String, Consumer<String>> listeners = new HashMap<>();
	private HashMap<String, Double> properties = new HashMap<>();
	private HashMap<String, String> messageBuffer = new HashMap<>();
	private List<String> polledTopics;
	private JSONParser parser = new JSONParser();

	enum ConnectionState{
		DISCONNECTED, CONNECTING, CONNECTED
	}
	
	/**
	 * Creates a new MessageClient. Give this constructor a list of topics you would like to poll 
	 * at a later time.
	 * @param brokerAddress
	 * @param polledTopics
	 */
	public MessageClient(String brokerAddress, String... polledTopics) {
		persistance = new MemoryPersistence();
		this.brokerAddress = brokerAddress;
		this.polledTopics = Arrays.asList(polledTopics);
	}

	public void connect() {
		if (state != ConnectionState.DISCONNECTED) {
			return;
		}
		state = ConnectionState.CONNECTING;
		new Thread(() -> {
			while (client == null || !client.isConnected()) {
				try {
					client = new MqttClient(brokerAddress, UUID.randomUUID().toString().substring(0, 20), persistance);
					client.setCallback(this);
					MqttConnectOptions connOpts = new MqttConnectOptions();
					connOpts.setCleanSession(true);
					connOpts.setConnectionTimeout(1);
					
					System.out.println("[MQTT] Connecting to broker: " + brokerAddress);
					client.connect(connOpts);
					state = ConnectionState.CONNECTED;
					System.out.println("[MQTT] Connected to client sucsessfully");

					// Subscribe to the topics we want
					String[] polledSubscriptions = polledTopics.stream().toArray(size -> new String[size]);
					client.subscribe(polledSubscriptions);
					
					String[] listenerSubscriptions = listeners.keySet().stream().toArray(size -> new String[size]);
					client.subscribe(listenerSubscriptions);
					
//					client.subscribe(PROPERTIES_TOPIC + "#");

				} catch (MqttException e) {
					System.err.println("[MQTT] MqttException, error connecting. Trying again");
					
					try {
						Thread.sleep(CONNECT_RETRY_TIME_MS);
					} catch (Exception e1) {
					}
				}
			}
		}).start();
	}

	public void publish(String topic, MqttMessage message) {
		if (client != null) {
			try {
				client.publish(topic, message);
			} catch (MqttException e) {
				System.err.println("[MQTT] Failed to publish message: "+ topic + " "+ message);
			}
		} else {
			System.err.println("[MQTT] Client not found, unable to publish message.");
		}
	}
	
	public void publish(String topic, String message){
		publish(topic, new MqttMessage(message.getBytes()));
	}

	public void publishPorperty(String name, double value) {
		MqttMessage message = new MqttMessage(String.valueOf(value).getBytes());
		message.setRetained(true);
		publish(PROPERTIES_TOPIC + name, message);  
	}
	
	@Override
	public void connectionLost(Throwable ex) {
		System.err.println("[MQTT] Connection lost");
		state = ConnectionState.DISCONNECTED;
		connect();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {	
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("topic: " + topic);
		String messageString = new String(message.getPayload());
		
		Consumer<String> listener = listeners.get(topic);
		if (listener != null){
			try{
				listener.accept(messageString);
			} catch (Exception e){
				//We must catch any errors from the listener here, or the mqtt client will disconnect
				e.printStackTrace();
			}
		}
		
		if (topic.startsWith(PROPERTIES_TOPIC)){
			properties.put(topic.substring(PROPERTIES_TOPIC.length()), Double.valueOf(messageString));
		}
		
		if (polledTopics.contains(topic)){
			messageBuffer.put(topic, messageString);
		}
	}
	
	public void addMessageListener(String topic, Consumer<String> listener) {
		listeners.put(topic, listener);
		if (state == ConnectionState.CONNECTED){
			try {
				client.subscribe(topic);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getMessage(String topic){
		return messageBuffer.get(topic);
	}
	
	public double getProperty(String name){
		return properties.get(name);
	}
	
	public JSONObject getJsonObject(String topic){
    	JSONObject obj;
    	try {
			obj = (JSONObject) parser.parse(getMessage(topic));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
    	
    	return obj;
	}
}