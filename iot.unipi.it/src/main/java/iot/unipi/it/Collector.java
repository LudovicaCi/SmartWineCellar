package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Collector extends CoapServer {

	public static void main(String[] args) throws SocketException, InterruptedException {
			MqttClientCollector mc = new MqttClientCollector();
			RegistrationServer rs = new RegistrationServer();
			
			rs.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			String command = "";
			String[] tokens;
			
			System.out.println("\nCommand list:");
			System.out.println("!exit: exit the program");
			System.out.println("!commands: list possible commands");					
			System.out.println("!checkTemp: get current temperature");
			System.out.println("!checkHumidity: get current humidity level");
			System.out.println("!checkCo2: get current Co2 level");
			System.out.println("!changeRangeTemp [low] [up]: set new acceptable range for the temperature");
			System.out.println("!changeRangeHum [low] [up]: set new acceptable range for the humidity");
			System.out.println("!changeMaxCo2 [new_value]: set new max value for the Co2 level");
			System.out.println("!stopAlarm: stop Co2 alarm");
			System.out.println("\n");
			
			while(true) {
				try {
					command = reader.readLine();
					tokens = command.split(" ");
					
					if (tokens[0].equals("!exit")) 
					{
						System.exit(1);
					} else if (tokens[0].equals("!commands")) 
					{
						System.out.println("\nCommand list:");
						System.out.println("!exit: exit the program");
						System.out.println("!commands: list possible commands");					
						System.out.println("!checkTemp: get current temperature");
						System.out.println("!checkHumidity [type]: get current humidity level");
						System.out.println("!checkCo2: get current Co2 level");
						System.out.println("!changeRangeTemp [low] [up]: set new acceptable range for the temperature");
						System.out.println("!changeRangeHum [low] [up]: set new acceptable range for the humidity");
						System.out.println("!changeMaxCo2 [new_value]: set new max value for the Co2 level");
						System.out.println("!stopAlarm: stop Co2 alarm");
						System.out.println("\n");
						
					}else if (tokens[0].equals("!checkTemp")) 
					{
						System.out.format("The temperature in the cellar is %d °C", mc.getCurrentTemp(tokens[1]));
						
					} else if (tokens[0].equals("!checkHumidity")) 
					{
						System.out.format("The humidity level in the cellar is %d g/m³", rs.getCurrentHum(tokens[1]));
						
					} else if (tokens[0].equals("!checkCo2"))
					{
						System.out.format("The co2 level in the cellar is of %d ppt", rs.getCurrentCo2());						
					}
					else if (tokens[0].equals("!changeRangeTemp"))
					{
						mc.changeRangeTemp(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),tokens[3]);
					} else if (tokens[0].equals("!changeRangeHum"))
					{
						rs.changeRangeHum(Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),tokens[3]);
					}
					else if (tokens[0].equals("!changeMaxCo2"))
					{
						rs.changeMaxCo2(Integer.parseInt(tokens[1]));
						
					} else if (tokens[0].equals("!stopAlarm"))
					{
						rs.stopAlarm();
					} 
					else {
						throw new IOException();
					}

					System.out.println("\n");
					
				}
				catch (IOException e) {
					System.out.println("Command not found, please retry!");
				}
			}
		}
					
}

