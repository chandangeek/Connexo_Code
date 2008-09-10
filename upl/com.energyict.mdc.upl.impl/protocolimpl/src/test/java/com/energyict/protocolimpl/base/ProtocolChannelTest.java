package com.energyict.protocolimpl.base;

import static org.junit.Assert.*;

import org.junit.Test;

import com.energyict.protocol.InvalidPropertyException;


public class ProtocolChannelTest {
	
	@Test
	public void protocolChannelTest(){
		String channel1 = "1.8.1";
		String channel2 = "1.8.1+9d";
		String channel3 = "1.8.1d";
		String channel4 = "1.8.1+9m";
		String channel5 = "1.8.1m";
		String channel6 = "1.8.1m+9";
		String channel7 = "d1.8.2";
		
		try {
			ProtocolChannel ch1 = new ProtocolChannel(channel1);
			assertFalse(ch1.containsDailyValues());
			assertFalse(ch1.containsMonthlyValues());
			
			ProtocolChannel ch2 = new ProtocolChannel(channel2);
			assertTrue(ch2.containsDailyValues());
			assertFalse(ch2.containsMonthlyValues());
			
			ProtocolChannel ch3 = new ProtocolChannel(channel3);
			assertTrue(ch3.containsDailyValues());
			assertFalse(ch3.containsMonthlyValues());
			
			ProtocolChannel ch4 = new ProtocolChannel(channel4);
			assertFalse(ch4.containsDailyValues());
			assertTrue(ch4.containsMonthlyValues());
			
			ProtocolChannel ch5 = new ProtocolChannel(channel5);
			assertFalse(ch5.containsDailyValues());
			assertTrue(ch5.containsMonthlyValues());
			
			ProtocolChannel ch6 = new ProtocolChannel(channel6);
			assertFalse(ch6.containsDailyValues());
			assertTrue(ch6.containsMonthlyValues());
			
			ProtocolChannel ch7 = new ProtocolChannel(channel7);
			assertTrue(ch7.containsDailyValues());
			assertFalse(ch7.containsMonthlyValues());
			
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		}
	}

}
