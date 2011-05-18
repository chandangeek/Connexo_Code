/**
 *
 */
package com.energyict.protocolimpl.utils;


import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.*;
import org.junit.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

		// first delete all the device
		List result = Utilities.mw().getRtuFactory().findByName("99999999");
		if (result.size() > 0) {
			for(int i = 0; i < result.size(); i++) {
				((Rtu)result.get(0)).delete();
			}
		}

		// then all the deviceType
		result = Utilities.mw().getRtuTypeFactory().findByName(testRtu);
		if (result.size() > 0) {
			for(int i = 0; i < result.size(); i++) {
				((RtuTypeImpl)result.get(0)).delete();
			}
		}

		// then all the communication profile
		result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);
		if (result.size() > 0) {
			for(int i = 0; i < result.size(); i++) {
				((CommunicationProtocolImpl)result.get(0)).delete();
			}
		}
	}

	@Test
	public void createCommunicationProtocolTest(){
		try {
			Utilities.createCommunicationProtocol(javaClassName);
			List result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);

			if(result.size() == 1){
				assertEquals(javaClassName, ((CommunicationProtocolImpl)result.get(0)).getShadow().getJavaClassName());
			} else {
				fail();
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
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
			} else {
				fail();
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void createRtuTest(){
		CommunicationProtocol commProtocol = null;
		RtuType rtuType;
		try {

			List result = Utilities.mw().getCommunicationProtocolFactory().findAll();
			for(int i = 0; i < result.size(); i++){
				if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(javaClassName)){
					commProtocol = (CommunicationProtocol)result.get(i);
					break;
				}
			}
			if(commProtocol == null) {
				commProtocol = Utilities.createCommunicationProtocol(javaClassName);
			}
			rtuType = Utilities.createRtuType(commProtocol, testRtu, 6);

			Utilities.createRtu(rtuType);
			result = Utilities.mw().getRtuFactory().findBySerialNumber("99999999");

			if(result.size() == 1){
				assertEquals("99999999", ((RtuImpl)result.get(0)).getShadow().getName());
				assertEquals(6, ((RtuImpl)result.get(0)).getShadow().getChannelShadows().size());
			} else {
				fail();
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void addChannelTest(){
		CommunicationProtocol commProtocol;
		RtuType rtuType;
		Rtu rtu = null;
		try {
			commProtocol = Utilities.createCommunicationProtocol(javaClassName);
			rtuType = Utilities.createRtuType(commProtocol, testRtu, 6);

			Utilities.createRtu(rtuType);
			List result = Utilities.mw().getRtuFactory().findBySerialNumber("99999999");

			if(result.size() == 1){
				rtu = (Rtu)result.get(0);
			} else {
				fail();
			}

			rtu = Utilities.addChannel(rtu, TimeDuration.SECONDS, 7);
			rtu = Utilities.addChannel(rtu, TimeDuration.DAYS, 8);
			rtu = Utilities.addChannel(rtu, TimeDuration.MONTHS, 9);
			rtu = Utilities.addChannel(rtu, TimeDuration.MINUTES, 10);

			assertEquals(10, rtu.getShadow().getChannelShadows().size());
			if(!Utilities.getChannelWithProfileIndex(rtu, 7).getInterval().equals(new TimeDuration(1, TimeDuration.SECONDS))) {
				fail();
			}
			if(!Utilities.getChannelWithProfileIndex(rtu, 8).getInterval().equals(new TimeDuration(1, TimeDuration.DAYS))) {
				fail();
			}
			if(!Utilities.getChannelWithProfileIndex(rtu, 9).getInterval().equals(new TimeDuration(1, TimeDuration.MONTHS))) {
				fail();
			}
			if(!Utilities.getChannelWithProfileIndex(rtu, 10).getInterval().equals(new TimeDuration(1, TimeDuration.MINUTES))) {
				fail();
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
}
