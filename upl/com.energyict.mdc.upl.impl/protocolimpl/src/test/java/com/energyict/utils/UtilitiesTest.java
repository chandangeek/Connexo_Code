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
import com.energyict.mdw.coreimpl.CommunicationProtocolImpl;

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
		
		// then the deviceType
		
		// then the communication profile
		
		List result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((CommunicationProtocolImpl)result.get(0)).delete();
	}
	
	@Ignore
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
	
	@Ignore
	@Test
	public void createRtuTypeTest(){
		try {
			CommunicationProtocol commProtocol = Utilities.createCommunicationProtocol(javaClassName);
			
			Utilities.createRtuType(commProtocol, testRtu, 6);
			
		} catch (BusinessException e) {
			fail();
			e.printStackTrace();
		} catch (SQLException e) {
			fail();
			e.printStackTrace();
		}
	}
	

}
