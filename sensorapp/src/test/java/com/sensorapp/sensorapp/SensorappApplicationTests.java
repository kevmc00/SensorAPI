package com.sensorapp.sensorapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SensorappApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void TestQueryBuilder(){
		Controller controller = new Controller();
		String query = controller.buildRequestQuery("1000", "2007-01-01", "2008-01-01");
		String correctQuery = "select * from sensorvalues where sensorID in (1000) and date between '2007-01-01' and '2008-01-01'";
		assertEquals(query, correctQuery);

		query = controller.buildRequestQuery("1000, 2000", "2007-01-01", "");
		correctQuery = "select * from sensorvalues where sensorID in (1000, 2000) and date > '2007-01-01'";
		assertEquals(query, correctQuery);
	
		query = controller.buildRequestQuery("", "", "2007-01-01");
		correctQuery = "select * from sensorvalues where date = '2007-01-01'";
		assertEquals(query, correctQuery);

		query = controller.buildRequestQuery("", "", "");
		correctQuery = "select * from sensorvalues";
		assertEquals(query, correctQuery);
	}

}
