package com.sensorapp.sensorapp;

import java.io.Console;
import java.sql.*;
import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST Controller class
@RestController
public class Controller {
    private Connection con;
    private Statement stmt;
    private static int recordCount;

    public Controller(){
        try{
            recordCount = 0;
			// Get database password
			Console console = System.console();
			String pw = new String(console.readPassword("Enter Database Password:"));

			// Dummy SQL query
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sensors", "root", pw);

            // Check if tables have been created and create if not
			stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("show tables");
            if(!rs.next()){
                stmt.executeUpdate("create table RegisteredSensors (id varchar(255), country varchar(255), city varchar(255), PRIMARY KEY(id))");
                stmt.executeUpdate("create table SensorValues (id varchar(255), sensorID varchar(255), date date, dataType varchar(255), value int, PRIMARY KEY(id))");
            }

            // Set ID no. for sensor readings to current max
            rs = stmt.executeQuery("select max(id) as start from sensorvalues");
            if(rs.next()){
                recordCount = rs.getInt("start");
            }

		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
		}
    }

    // Querys sensor reading database
    @GetMapping("/request")
    public double[] querySensors(@RequestParam(name = "sensorID", defaultValue = "") String sensorID,
    @RequestParam(name = "startdate", defaultValue = "") String startDate,
    @RequestParam(name = "enddate", defaultValue = "") String enddate,
    @RequestParam(name = "dataType", defaultValue = "Temperature_Humidity_WindSpeed") String type){
        try{
            // Get formatted query
            String query = buildRequestQuery(sensorID, startDate, enddate);

            // Print query to console for transparency
            System.out.println(query.toString());

            // Send query to database
            ResultSet rs = stmt.executeQuery(query.toString());

            // Parse the data that the user wants returned (default is all 3)
            // HashMap is used to get the index of the array for each data type
            String[] toReturn = type.split("_");
            HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
            for(int i = 0; i < toReturn.length; i++){
                indexMap.put(toReturn[i], i);
            }

            // Create arrays for results and counts of each type
            double[] results = new double[toReturn.length];
            int[] counts = new int[toReturn.length];
            int index = 0;

            // Iterate through SQL response
            while(rs.next()){
                // Skip over responses with values with unwanted types
                if(indexMap.containsKey(rs.getString("dataType"))){
                    index = indexMap.get(rs.getString("dataType"));
                    // Increment count and add to sum
                    counts[index]++;
                    results[index] += rs.getInt("value");
                }
            }

            // Use counts to convert sum to average
            for(int i = 0; i < results.length; i++){
                results[i] /= counts[i];
            }

            return results;

        }catch(Exception e){
            System.out.println("Error performing request: " + e.getMessage());
            e.printStackTrace();
        }
        return new double[]{}; 
    }

    // Returns formatted query based on sensor ID, start date and end date
    public String buildRequestQuery(String sensorID, String startDate, String enddate){
        StringBuilder query = new StringBuilder();

        // If no sensor ID specified, average of all sensor values returned
        if(!sensorID.equals("")){

            query.append("select * from sensorvalues where ");
            String[] sensors = sensorID.split("_");
            query.append("sensorID in (");
            for(int i = 0; i < sensors.length; i++){
                query.append(sensors[i]);
                if(i < sensors.length - 1){
                    query.append(",");
                }
            }
            query.append(")");
        }

        // If no start or end dates specified, all values from specified sensors returned
        if(!startDate.equals("") || !enddate.equals("")){
            // Query appended differently if sensors already specified
            if(query.length() == 0){
                query.append("select * from sensorvalues where ");
            }else{
                query.append(" and ");
            }

            // If both start and end date are specified, return interval
            if(!startDate.equals("") && !enddate.equals("")){
                query.append("date between '" + startDate + "' and '" + enddate + "'");
            // If only start date is specified, return all data up to start date
            }else if(!startDate.equals("")){
                query.append("date > '" + startDate + "'");
            // If only end date is specified, return data from the specified date
            }else{
                query.append("date = '" + enddate + "'");
            }
        }
        
        // If nothing has been specified to this point, return avg of all data
        if(query.length() == 0){
            query.append("select * from sensorvalues");
        }

        return query.toString();
    }

    // Method used to add a new sensor to the system
    @RequestMapping(method = RequestMethod.POST, value = "/add")
    public void addSensor(@RequestBody Sensor sensor){

        // Build SQL Query
        StringBuilder query = new StringBuilder();
        query.append("insert into registeredsensors (id, country, city) values (");
        query.append("'" + sensor.getID() + "'" + ",");
        query.append("'" + sensor.getCountry() + "'" + ",");
        query.append("'" + sensor.getCity() + "'" + ")");
        try{
            // Print query for transparency
            System.out.println(query.toString());
            // Add sensor to database
            stmt.executeUpdate(query.toString());
            System.out.println("Successfully added sensor (id: " + sensor.getID() + ")");
        // Error is thrown if the sensor's primary key (id) is already in the database so no duplicates exist
        }catch(SQLIntegrityConstraintViolationException e){
            System.out.println("Error: Sensor has already been added");
        }catch(Exception e){
            System.out.println("Error adding sensor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method records incoming sensor values
    @RequestMapping(method = RequestMethod.POST, value = "/record")
    public void recordValue(@RequestBody SensorValue value){
        // Check if sensor has been registered
        try{
            String query = "select count(*) as total from registeredsensors where id='" + value.getSensorID() + "'";
            // Print query for transparency and send
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            // If response is less than 1, sensor has not been registered - ignore value
            if(rs.getInt("total") < 1){
                System.out.println("Sensor no." + value.getSensorID() + " not registered - cannot record value");
                return;
            }
        }catch(Exception e){
            System.out.println("Error checking for sensor: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Build query to add value
        StringBuilder query = new StringBuilder();
        recordCount++;
        query.append("insert into sensorvalues (id, sensorID, date, dataType, value) values (");
        query.append("'" + recordCount + "'" + ",");
        query.append("'" + value.getSensorID() + "'" + ",");
        query.append("'" + value.getDate() + "'" + ",");
        query.append("'" + value.getDataType() + "'" + ",");
        query.append("'" + value.getValue() + "'" + ")");

        // Send query to database
        try{
            System.out.println(query.toString());
            stmt.executeUpdate(query.toString());
            System.out.println("Successfully added " + value.getDataType() + " value (" + value.getValue() + ") for sensor no." + value.getID());
        }catch(Exception e){
            System.out.println("Error adding sensor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
