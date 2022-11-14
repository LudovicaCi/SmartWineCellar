package iot.unipi.it;

import java.util.Map;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;


public class MqttClientCollector implements MqttCallback {
	
	private final String brokerAddr = "tcp://127.0.0.1:1883";
	private final String clientId = "Java_MqttClient";
	
	//topic to be subscribed to
		private final String subTopicTempRed = "current_temperature_red";
		private final String subTopicTempWhite = "current_temperature_white";

		//topic to publish in
		private final String pubTopicTempRed = "temperature_regulator_red"; 
		private final String pubTopicTempWhite = "temperature_regulator_white"; 


		private MqttClient mqttClient = null;
		
		
		private int currentTempRed;
		private boolean regulatorTempRed = false;
		private int currentTempWhite;
		private boolean regulatorTempWhite = false;
		
		private int lowTempRed = 12;
		private int upTempRed = 16;
		private int lowTempWhite = 10;
		private int upTempWhite = 12;
		
		
		public MqttClientCollector(){

			
			do{
				try{
					mqttClient = new MqttClient(brokerAddr, clientId);
					System.out.println("Connecting to the broker " + brokerAddr);
					mqttClient.setCallback(this);
					mqttClient.connect();
					System.out.println("Connection to the broker successful");
					mqttClient.subscribe(subTopicTempRed);
					mqttClient.subscribe(subTopicTempWhite);
					System.out.println("Application correctly subscribed to the topics " + subTopicTempRed + "," + subTopicTempWhite );
				} catch(MqttException e){
					System.out.println("Not able to connect. Retrying...");
				}
			} while (!mqttClient.isConnected());
		}

		public void connectionLost(Throwable cause) {
			System.out.println("Connection is broken: " + cause);
			int timeWindow = 3000;
			while (!mqttClient.isConnected()) {
				try {
					System.out.println("Trying to reconnect in " + timeWindow/1000 + " seconds.");
					Thread.sleep(timeWindow);
					System.out.println("Reconnecting ...");
					timeWindow *= 2;
					mqttClient.connect();
					
					mqttClient.subscribe(subTopicTempRed);
					mqttClient.subscribe(subTopicTempWhite);
					System.out.println("Connection is restored");
				}catch(MqttException me) {
					System.out.println("I could not connect");
				} catch (InterruptedException e) {
					System.out.println("I could not connect");
				}
			}
		}
		
		
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			byte[] payload = message.getPayload();
		try {
			JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));
				
				if(topic.equals(this.subTopicTempRed)) 
				{
					if (sensorMessage.containsKey("temperature")) {
							// Parsing
							this.currentTempRed = Integer.parseInt(sensorMessage.get("temperature").toString());
							System.out.println("Ho letto la temperature_red ed è "+ this.currentTempRed);
							
							// Put data in DB
							SmartCellarDB.insertTemperature(currentTempRed, "red");
							
							if(currentTempRed <  lowTempRed){
								System.out.println("Temperature is too low! Conditioner in heat mode");
								publish(this.pubTopicTempRed, "OFF");
								regulatorTempRed = false;
								
							} else if(currentTempRed > upTempRed){
								System.out.println("Temperature is too high! Conditioner in cool mode");
								publish(this.pubTopicTempRed, "ON");
								regulatorTempRed = true;
							} 
							else {
								System.out.println("Nessuna azione necessaria");
							}
					}
					
				}else if(topic.equals(this.subTopicTempWhite)) 
							{
								if (sensorMessage.containsKey("temperature")) {
										// Parsing
										this.currentTempWhite = Integer.parseInt(sensorMessage.get("temperature").toString());
										System.out.println("Ho letto la temperature_white ed è "+ this.currentTempWhite);
										
										// Put data in DB
										SmartCellarDB.insertTemperature(currentTempWhite, "white");
										
										if(currentTempWhite < lowTempWhite){
											System.out.println("Temperature is too low! Conditioner in heat mode");
											publish(this.pubTopicTempWhite, "OFF");
											regulatorTempWhite = false;
											
										} else if(currentTempWhite > upTempWhite){
											System.out.println("Temperature is too high! Conditioner in cool mode");
											publish(this.pubTopicTempWhite, "ON");
											regulatorTempWhite = true;										} 
										else {
											System.out.println("Nessuna azione necessaria");
										}
								} 
							}else {
									System.out.println(String.format("Unknown topic: [%s] %s", topic, new String(payload)));
								}
							 
				} catch (ParseException e) {
								System.out.println(String.format("Received badly formatted message: [%s] %s", topic, new String(payload)));
							} catch (Exception e) {
								e.printStackTrace();
							}
	}
				
		
		
		public void deliveryComplete(IMqttDeliveryToken token) {
			System.out.println("Delivery of the message correctly completed");		
		}
		
		public void publish(final String topic, final String content){
			try {
				MqttMessage message = new MqttMessage(content.getBytes());
				mqttClient.publish(topic, message);
				System.out.println("New message published succesfully to the topic " + topic + ". Message = " + content);
			} catch(MqttException me) {
				me.printStackTrace();
			}
		}
		
		
		public void changeRangeTemp(int low, int up, String type){
			if(type.equals("red")) {
				lowTempRed = low;
				upTempRed = up;
				System.out.println("lowTempRed:" + lowTempRed + "  upTempRed:" + upTempRed );
			}
			else if(type.equals("white")) {
				lowTempWhite = low;
				upTempWhite = up;
				System.out.println("lowTempWhite:" + lowTempWhite + "  upTempWhite:" + upTempWhite );
			}
			else {
				System.out.println("you insert a wrong wine type");
			}
		}

		public int getCurrentTemp(String type) {
			if(type.equals("red")) {
				System.out.println("type red");
				return this.currentTempRed;
			}
			else if(type.equals("white")) {
				System.out.println("type white");
				return this.currentTempWhite;
			}
			else {
				System.out.println("you insert a wrong wine type");
				return 0;
			}
		}
		
}
