package iot.unipi.it;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;


public class CoapNetworkHandler {

	private CoapClient clientCo2Sensor = null;
    private CoapClient clientVentilationSystem = null;
    private CoapObserveRelation observeSensor1 = null;
    private CoapClient clientHumidityRedSensor = null;
    private CoapClient clientDehumidifierRedSystem = null;
    private CoapObserveRelation observeSensor2 = null;
	private CoapClient clientHumidityWhiteSensor = null;
    private CoapClient clientDehumidifierWhiteSystem = null;
    private CoapObserveRelation observeSensor3 = null;
    private boolean ventilation_on = false;
    private boolean dehumidifier_red_on = false;
    private boolean dehumidifier_white_on = false;
    private boolean alarm = false;
    private int max_Co2 = 500;
    private int Co2_level;
    private int humidity_red_value;
    private int humidity_white_value;
    private int humidity_red_up_value = 70;
    private int humidity_white_up_value = 80;
    private int humidity_red_low_value = 60;
    private int humidity_white_low_value = 70;
    private String responseString1 = " ";
    private String responseString2 = " ";
    private String responseString3 = " ";



	private static CoapNetworkHandler instance = null;

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();
        return instance;
    }
	
    
    
    public void addCo2Sensor(String ipAddress) {

        System.out.println("The Co2 sensor: [" + ipAddress + "] + is now registered");
        clientCo2Sensor = new CoapClient("coap://[" + ipAddress + "]/air_quality/Co2");
        clientVentilationSystem = new CoapClient("coap://[" + ipAddress + "]/air_quality/ventilation");
        System.out.println("clientCo2Sensor://[" + clientCo2Sensor + "] clientVentilationSystem://[" + clientVentilationSystem);
        observeSensor1 = clientCo2Sensor.observe(
                new CoapHandler() {
                	public void onLoad(CoapResponse response) {
                        handleCo2Response(response);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }
    
    
    public void handleCo2Response(CoapResponse response){
        String responseString1 = new String(response.getPayload());
        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(responseString1);
            if (sensorMessage.containsKey("concentration")) {
                this.Co2_level = Integer.parseInt(sensorMessage.get("concentration").toString());
                System.out.println("Co2_level: " + this.Co2_level);
                SmartCellarDB.insertCo2(Co2_level);
                String msg;
                if (Co2_level >= 480) {
                    msg = "mode=ON1";
                    ventilation_on = true;
                    alarm = false;
                    clientVentilationSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Air Quality System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Air Quality System " + clientVentilationSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
                
                }else if (Co2_level >= max_Co2) {
                	msg = "mode=ON2";
                    ventilation_on = true;
                    alarm = true;
                    clientVentilationSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Air Quality System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Air Quality System " + clientVentilationSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
            }else if(Co2_level <= 400) {
            	msg = "mode=OFF";
                ventilation_on = false;
                alarm = false;
                clientVentilationSystem.put(new CoapHandler() {
                    public void onLoad(CoapResponse coapResponse) {
                        if(coapResponse != null) {
                            if(!coapResponse.isSuccess())
                                System.out.print("\n[ERROR] Air Quality System: PUT request unsuccessful\n>");
                        }
                    }
                    public void onError() {
                        System.err.print("\n[ERROR] Air Quality System " + clientVentilationSystem.getURI() + "]\n>");
                    }
                }, msg, MediaTypeRegistry.TEXT_PLAIN);
                
            } 
                
            }
        } catch (Exception e){
            System.err.println("The message received was not valid");
        }
        System.out.println("");
    }
    
    
    public void addHumidityRedSensor(String ipAddress) {

        System.out.println("The humidity redwine sensor: [" + ipAddress + "] + is now registered");
        clientHumidityRedSensor = new CoapClient("coap://[" + ipAddress + "]/humidity_redwine/humidity");
        clientDehumidifierRedSystem = new CoapClient("coap://[" + ipAddress + "]/humidity_redwine/dehumidifier");
        System.out.println("clientHumidityRedSensor://[" + clientHumidityRedSensor + "] clientDehumidifierRedSystem://[" + clientDehumidifierRedSystem);
        observeSensor2 = clientHumidityRedSensor.observe(
                new CoapHandler() {
                	public void onLoad(CoapResponse response) {
                         System.out.println("Sta osservando");
                        handleHumidityRedResponse(response);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }
    
    
    public void addHumidityWhiteSensor(String ipAddress) {

        System.out.println("The humidity whitewine sensor: [" + ipAddress + "] + is now registered");
        clientHumidityWhiteSensor = new CoapClient("coap://[" + ipAddress + "]/humidity_whitewine/humidity");
        clientDehumidifierWhiteSystem = new CoapClient("coap://[" + ipAddress + "]/humidity_whitewine/dehumidifier");
        System.out.println("clientHumidityWhiteSensor://[" + clientHumidityWhiteSensor + "] clientDehumidifierWhiteSystem://[" + clientDehumidifierWhiteSystem);
        observeSensor3 = clientHumidityWhiteSensor.observe(
                new CoapHandler() {
                	public void onLoad(CoapResponse response) {
                        handleHumidityWhiteResponse(response);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }
    
    public void handleHumidityRedResponse(CoapResponse response){
        String responseString2 = new String(response.getPayload());
        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(responseString2);
            if (sensorMessage.containsKey("concentration")) {
                this.humidity_red_value = Integer.parseInt(sensorMessage.get("concentration").toString());
                System.out.println("humidity_red_value: " + this.humidity_red_value);
                SmartCellarDB.insertHumidity(humidity_red_value, "red");
                String msg;
                if (humidity_red_value >= humidity_red_up_value) {
                    msg = "mode=ON";
                    dehumidifier_red_on = true;
                    clientDehumidifierRedSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Dehumidifier Red System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Dehumidifier Red System " + clientDehumidifierRedSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
                
                }else if (humidity_red_value <= humidity_red_low_value) {
                	msg = "mode=OFF";
                	dehumidifier_red_on = false;
                    clientDehumidifierRedSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Dehumidifier Red System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Dehumidifier Red System " + clientDehumidifierRedSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
            }
                
            }
        } catch (Exception e){
            System.err.println("The message received was not valid");
        }
        System.out.println("");
    }
    
    public void handleHumidityWhiteResponse(CoapResponse response){
       String responseString3 = new String(response.getPayload());
        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(responseString3);
            if (sensorMessage.containsKey("concentration")) {
                this.humidity_white_value = Integer.parseInt(sensorMessage.get("concentration").toString());
                System.out.println("humidity_white_value: " + this.humidity_white_value);
                SmartCellarDB.insertHumidity(humidity_white_value, "white");
                String msg;
                if (humidity_white_value >= humidity_white_up_value) {
                    msg = "mode=ON";
                    dehumidifier_white_on = true;
                    clientDehumidifierWhiteSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Dehumidifier White System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Dehumidifier White System " + clientDehumidifierWhiteSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
                
                }else if (humidity_white_value <= humidity_white_low_value) {
                	msg = "mode=OFF";
                	dehumidifier_white_on = false;
                    clientDehumidifierWhiteSystem.put(new CoapHandler() {
                        public void onLoad(CoapResponse coapResponse) {
                            if(coapResponse != null) {
                                if(!coapResponse.isSuccess())
                                    System.out.print("\n[ERROR] Dehumidifier White System: PUT request unsuccessful\n>");
                            }
                        }
                        public void onError() {
                            System.err.print("\n[ERROR] Dehumidifier White System " + clientDehumidifierWhiteSystem.getURI() + "]\n>");
                        }
                    }, msg, MediaTypeRegistry.TEXT_PLAIN);
            }
                
            }
        } catch (Exception e){
            System.err.println("The message received was not valid");
        }
        System.out.println("");
    }
    
    
    
    
    public void stopAlarm() {
    	if(Co2_level < max_Co2){
    		alarm = false;
            System.out.println("You stop the alarm");
        }else{
    		System.out.println("You have to wait that the Co2 level go under the alarm threshold");
        }
    } 
    
    public int getCurrentCo2() {
        return this.Co2_level;
    }
    
    public int getCurrentHum(String type) {
        if(type.equals("red")) {
            System.out.println("type red");
        	return this.humidity_red_value;
        }else if (type.equals("white")) {
            System.out.println("type white");
        	return this.humidity_white_value;
        }else {
			System.out.println("you insert a wrong wine type");
			return 0;
		}
    }
    
    public void changeRangeHum(int low, int up, String type) {
    	if(type.equals("red")) {
    		humidity_red_low_value = low;
    		humidity_red_up_value = up;
            System.out.println("humidity_red_low_value:" + humidity_red_low_value + "  humidity_red_up_value:" + humidity_red_up_value );

		}
		else if(type.equals("white")) {
			humidity_white_low_value = low;
			humidity_white_up_value = up;
            System.out.println("humidity_white_low_value:" + humidity_white_low_value + "  humidity_white_up_value:" + humidity_white_up_value );
		}
		else {
			System.out.println("you insert a wrong wine type");
		}
    }
    
    public void changeMaxCo2(int value) {
        if(value <= 480){
            System.out.println("Il valore inserito non è corretto. Inserire un valore superiore a 480");
        }else{
            this.max_Co2 = value;
            System.out.println("max_Co2:" + this.max_Co2);
        }
    }
    
    
    
    
    
    
	
}
