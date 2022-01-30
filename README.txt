Instructions for weather sensor app
-----------------------------------
- This app was built in VSCode using Spring Boot to create an API and MySQL for database

BEFORE RUNNING
- A MySQL database called "sensors" must be set up on port 3306
- In the application, the username is hardcoded to "root" and the password can be entered

SETTING UP THE API
- The main class is called "SensorappApplication"
- Upon running, you will be asked for the database password
- If the password is entered incorrectly, restart the app
- The API operates on the local host (localhost:8080)

USING THE API
- The API has 3 usable commands: \add, \record and \request
- Postman was used to test the API but any program that generates GET and POST requests will suffice
- ADD
  - The add command is used to add sensors to the system
  - The JSON format of the body of the POST request can be seen below:
	{
	    "id":"0001",
	    "country":"Ireland",
	    "city":"Dublin"
	}
  - Sensors with duplicate IDs will be rejected
- RECORD
  - The record command is used to record sensor values
  - The JSON format of the body of the POST request can be seen below:
	{
	    "sensorID":"0001",
	    "date":"2006-01-01",
	    "dataType":"Temperature",
	    "value":20
	}
  - A sensor's value cannot be recorded until it has been registered
- REQUEST
  - The request command is used to request averaged data from the specific sensors in specific date ranges
  - The following parameters can be included in the request, although none is needed
     - sensorID (separated with underscore)
     - startdate (formatted YYYY-MM-DD)
     - enddate (formatted YYYY-MM-DD)
     - datatype (Temperature, Humidity, WindSpeed - Case Sensitive, separate with underscore)
  - An example of this GET request format can be seen below:
     http://localhost:8080/request?sensorID=0001_0002&enddate=2007-01-01&startdate=2005-01-01&dataType=Temperature_WindSpeed
  - Omitting the sensor ID will result in averages of the requested values from all sensors
  - If both startdate and enddate are included, the sensor data from that range of dates will be returned
  - Omitting both startdate and enddate will include all data from all requested sensors
  - Including startdate without enddate will return all data from the startdate to present
  - Including enddate without startdate will return all data from the given date
  - The returned values will be averages of the values of the specified datatypes in the order they were requested
  - If datatype is omitted, an average of each of the three values will be returned in the following order - Temperature, Humidity, WindSpeed