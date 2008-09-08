package com.energyict.protocolimpl.metcom;


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetcomTest {
	
	private Metcom metcom;

	@Before
	public void setUp() throws Exception {
		metcom = new Metcom3();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void doSetTimeTest(){
		metcom.setTesting(true);
		
		metcom.timeSetMethod = 0;
		try {
			metcom.setTime();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(0, JUnitTestCode.checkMethod());
		
		metcom.timeSetMethod = 1;
		try {
			metcom.setTime();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(1, JUnitTestCode.checkMethod());
		JUnitTestCode.setDelay(1);
		try {
			metcom.setTime();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(0, JUnitTestCode.checkMethod());
		
		JUnitTestCode.setDelay(0);
		metcom.timeSetMethod = 2;
		try {
			metcom.setTime();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(1, JUnitTestCode.checkMethod());
		JUnitTestCode.setDelay(1);
		metcom.timeSetMethod = 2;
		try {
			metcom.setTime();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertEquals(1, JUnitTestCode.checkMethod());
	}

}
