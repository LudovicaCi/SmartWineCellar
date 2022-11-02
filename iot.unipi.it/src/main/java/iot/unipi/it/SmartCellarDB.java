package iot.unipi.it;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SmartCellarDB {
	 @SuppressWarnings("finally")
		private static Connection makeJDBCConnection() {
		Connection databaseConnection = null;


     String databaseIP = "localhost";
     String databasePort = "3306";
     String databaseUsername = "root";
     String databasePassword = "root";
     String databaseName = "smart_cellar";
     
     
     try {
         Class.forName("com.mysql.cj.jdbc.Driver");//checks if the Driver class exists (correctly available)
     } catch (ClassNotFoundException e) {
         e.printStackTrace();
         return databaseConnection;
     }
     try {
         // DriverManager: The basic service for managing a set of JDBC drivers.
         databaseConnection = DriverManager.getConnection(
                 "jdbc:mysql://" + databaseIP + ":" + databasePort +
                         "/" + databaseName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                 databaseUsername,
                 databasePassword);
         //The Driver Manager provides the connection specified in the parameter string
         if (databaseConnection == null) {
             System.err.println("Connection to Db failed");
         }
     } catch (SQLException e) {
     	System.err.println("MySQL Connection Failed!\n");
         e.printStackTrace();
     }finally {
         return databaseConnection;
     }
 }
	 
	 public static void insertTemperature(final int temperature, final String type) {
	    	String insertQueryStatement = "INSERT INTO temperature (timestamp, temperature, type) VALUES (CURRENT_TIMESTAMP, ?, ?)";
	    	try (Connection smartPoolConnection = makeJDBCConnection();
	        		PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(insertQueryStatement);
	           ) {
	    		smartPoolPrepareStat.setInt(1, temperature); 
	    		smartPoolPrepareStat.setString(2, type);
	        	smartPoolPrepareStat.executeUpdate();
	 
	    	 } catch (SQLException sqlex) {
	             sqlex.printStackTrace();
	         }
		}
	 
	 public static void insertHumidity(final int humidity, final String type) {
	    	String insertQueryStatement = "INSERT INTO humidity (timestamp, humidity, type) VALUES (CURRENT_TIMESTAMP, ?, ?)";
	    	try (Connection smartPoolConnection = makeJDBCConnection();
	        		PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(insertQueryStatement);
	           ) {
	    		smartPoolPrepareStat.setInt(1, humidity); 
	    		smartPoolPrepareStat.setString(2, type);
	        	smartPoolPrepareStat.executeUpdate();
	 
	    	 } catch (SQLException sqlex) {
	             sqlex.printStackTrace();
	         }
		}
	    	
	 public static void insertCo2(final int Co2) {
	    	String insertQueryStatement = "INSERT INTO co2 (timestamp, Co2) VALUES (CURRENT_TIMESTAMP, ?)";
	    	try (Connection smartPoolConnection = makeJDBCConnection();
	        		PreparedStatement smartPoolPrepareStat = smartPoolConnection.prepareStatement(insertQueryStatement);
	           ) {
	    		smartPoolPrepareStat.setInt(1, Co2); 
	        	smartPoolPrepareStat.executeUpdate();
	 
	    	 } catch (SQLException sqlex) {
	             sqlex.printStackTrace();
	         }
		}
}