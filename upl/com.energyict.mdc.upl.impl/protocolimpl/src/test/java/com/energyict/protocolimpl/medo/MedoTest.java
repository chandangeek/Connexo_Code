package com.energyict.protocolimpl.medo;


import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.protocol.UnsupportedException;

public class MedoTest {

	Medo medo;
	
	@Before
	public void setUp() throws Exception {
		medo = new Medo();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void getFirmwareVersionTest(){
		try {
			String firm = medo.getFirmwareVersion();
			assertEquals("MEDO Metering Equipment Digital Outstation - V1.3", firm);
		} catch (UnsupportedException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
	}

}
