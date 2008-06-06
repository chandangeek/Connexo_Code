/**
 * 
 */
package com.energyict.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.coreimpl.CommunicationProtocolImpl;
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.mdw.coreimpl.RtuTypeImpl;

/**
 * @author gna
 *
 */
public class UtilitiesTest {
	
	private String javaClassName = "com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P";
	private String testRtu = "testRtu";
	
	@BeforeClass
	public static void setUpOnce() throws Exception {
		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
		// first delete the device
		List result = Utilities.mw().getRtuFactory().findByName(testRtu);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((Rtu)result.get(0)).delete();
		
		// then the deviceType
		result = Utilities.mw().getRtuTypeFactory().findByName(testRtu);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((RtuTypeImpl)result.get(0)).delete();
		
		// then the communication profile
		result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((CommunicationProtocolImpl)result.get(0)).delete();
	}
	
	@Test
	public void createCommunicationProtocolTest(){
		try {
			Utilities.createCommunicationProtocol(javaClassName);
			List result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);
			
			if(result.size() == 1){
				assertEquals(javaClassName, ((CommunicationProtocolImpl)result.get(0)).getShadow().getJavaClassName());
			}
			else
				fail();
			
		} catch (BusinessException e) {
			fail();
			e.printStackTrace();
		} catch (SQLException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void createRtuTypeTest(){
		try {
			CommunicationProtocol commProtocol = Utilities.createCommunicationProtocol(javaClassName);
			
			Utilities.createRtuType(commProtocol, testRtu, 6);
			List result = Utilities.mw().getRtuTypeFactory().findByName(testRtu);
			
			if(result.size() == 1){
				assertEquals(testRtu, ((RtuTypeImpl)result.get(0)).getShadow().getName());
				assertEquals(javaClassName, ((RtuTypeImpl)result.get(0)).getShadow().getCommunicationProtocolShadow().getName());
				assertEquals(6, ((RtuTypeImpl)result.get(0)).getShadow().getChannelCount());
			}
			else
				fail();
			
		} catch (BusinessException e) {
			fail();
			e.printStackTrace();
		} catch (SQLException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	@Test
	public void createRtuTest(){
		CommunicationProtocol commProtocol;
		RtuType rtuType;
		try {
			commProtocol = Utilities.createCommunicationProtocol(javaClassName);
			rtuType = Utilities.createRtuType(commProtocol, testRtu, 6);
			
			Utilities.createRtu(rtuType);
			List result = Utilities.mw().getRtuFactory().findBySerialNumber("99999999");
			
			if(result.size() == 1){
				assertEquals(testRtu, ((RtuImpl)result.get(0)).getShadow().getName());
				assertEquals(6, ((RtuImpl)result.get(0)).getShadow().getChannelShadows().size());
			}
			else
				fail();
			
		} catch (BusinessException e) {
			fail();
			e.printStackTrace();
		} catch (SQLException e) {
			fail();
			e.printStackTrace();
		}
	}

}
