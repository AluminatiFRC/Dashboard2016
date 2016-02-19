package org.usfirst.frc.team5495;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Action;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * If there is no connection, messages are ignored. Does not work with wildcard topics yet.
 * @author shsrobotics
 *
 */
public class MessageClient implements MqttCallback{
	private String brokerAddress;
	private MqttClient client;
	private MemoryPersistence persistance; // needed for mqtt
	private ConnectionState state = ConnectionState.DISCONNECTED;
	private HashMap<String, Consumer<String>> listeners = new HashMap<>();

	enum ConnectionState{
		DISCONNECTED, CONNECTING, CONNECTED
	}
	
	public MessageClient(String brokerAddress) {
		persistance = new MemoryPersistence();
		this.brokerAddress = brokerAddress;
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
					
					String[] subscriptions = listeners.keySet().stream().toArray(size -> new String[size]);
					client.subscribe(subscriptions);
				} catch (MqttException e) {
					System.err.println("[MQTT] MqttException, error connecting. Trying again");
					
					try {
						Thread.sleep(1000);
					} catch (Exception e1) {
					}
				}
			}
		}).start();
	}

	public void publish (String topic, String message) {
		if (client != null) {
			try {
				client.publish(topic, new MqttMessage(message.getBytes()));
			} catch (MqttException e) {
				System.err.println("[MQTT] Failed to publish message: "+ topic + " "+ message);
			}
		} else {
			System.err.println("[MQTT] Client not found, unable to publish message.");
		}
		
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
		Consumer<String> listener = listeners.get(topic);
		if (listener == null){
			System.out.println("Unhandled message. Topic: " + topic + " Message: " + message);
		} else {
			try{
				listener.accept(new String(message.getPayload()));
			} catch (Exception e){
				//We must catch any errors here, or the client will disconnect
				e.printStackTrace();
			}
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
}
