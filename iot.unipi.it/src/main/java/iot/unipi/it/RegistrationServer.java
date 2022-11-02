package iot.unipi.it;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;




public class RegistrationServer extends CoapServer {

	 private final static CoapNetworkHandler coapHandler = CoapNetworkHandler.getInstance();

	    public RegistrationServer() throws SocketException {
	        this.add(new RegistrationResource());
	    }
	    
	    class RegistrationResource extends CoapResource{

	        public RegistrationResource() {
	            super("registration");
	        }
	        
	        @Override
	        public void handlePOST(CoapExchange exchange) {
	            String deviceType = exchange.getRequestText();
	            String ipAddress = exchange.getSourceAddress().getHostAddress();
	
	            
	            
	            if(deviceType.equals("humidity_redwine"))
	                coapHandler.addHumidityRedSensor(ipAddress);
	            else if(deviceType.equals("humidity_whitewine"))
	                coapHandler.addHumidityWhiteSensor(ipAddress);
	            else if(deviceType.equals("air_quality"))
	                coapHandler.addCo2Sensor(ipAddress);

	            exchange.respond(ResponseCode.CREATED, "Success".getBytes(StandardCharsets.UTF_8));
			}
		}

	    public int getCurrentHum(String type){
			 return coapHandler.getCurrentHum(type);
		}

	    public int getCurrentCo2(){
			return coapHandler.getCurrentCo2();
		}

		public void changeRangeHum(int low, int up, String type){
			coapHandler.changeRangeHum(low, up, type);
		}

		public void changeMaxCo2(int value){
			coapHandler.changeMaxCo2(value);
		}

		public void stopAlarm(){
			coapHandler.stopAlarm();
		}
	
}