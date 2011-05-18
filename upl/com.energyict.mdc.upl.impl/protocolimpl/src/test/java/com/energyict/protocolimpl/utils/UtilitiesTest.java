/**
 *
 */
package com.energyict.protocolimpl.utils;


import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.coreimpl.CommunicationProtocolImpl;
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.mdw.testutils.CommunicationProtocolCRUD;
import com.energyict.mdw.testutils.RtuCRUD;
import com.energyict.mdw.testutils.RtuTypeCRUD;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {

		// first delete all the device
        RtuCRUD.deleteRtu("99999999");
		// then all the deviceType
        RtuTypeCRUD.deleteRtuType(testRtu);
        CommunicationProtocolCRUD.deleteCommunicationProtocol(javaClassName);
	}

	@Test
	public void createCommunicationProtocolTest() throws BusinessException, SQLException {
        Utilities.createCommunicationProtocol(javaClassName);
        List result = Utilities.mw().getCommunicationProtocolFactory().findByName(javaClassName);

		assertEquals(1, result.size());
	    assertEquals(javaClassName, ((CommunicationProtocolImpl)result.get(0)).getShadow().getJavaClassName());
	}

	@Test
	public void createRtuTypeTest() throws BusinessException, SQLException {
        CommunicationProtocol commProtocol = Utilities.createCommunicationProtocol(javaClassName);

        Utilities.createRtuType(commProtocol, testRtu, 6);
        List<RtuType> result = Utilities.mw().getRtuTypeFactory().findByName(testRtu);

        assertEquals(1, result.size());
        assertEquals(testRtu, result.get(0).getShadow().getName());
        assertEquals(javaClassName, result.get(0).getShadow().getCommunicationProtocolShadow().getName());
        assertEquals(6, result.get(0).getShadow().getChannelCount());
	}

	@Test
	public void createRtuTest() throws BusinessException, SQLException {
		CommunicationProtocol commProtocol = null;
		RtuType rtuType;
        List result = Utilities.mw().getCommunicationProtocolFactory().findAll();
        for (Object aResult : result) {
            if (((CommunicationProtocol) aResult).getJavaClassName().equalsIgnoreCase(javaClassName)) {
                commProtocol = (CommunicationProtocol) aResult;
                break;
            }
        }
        if(commProtocol == null) {
            commProtocol = Utilities.createCommunicationProtocol(javaClassName);
        }
        rtuType = Utilities.createRtuType(commProtocol, testRtu, 6);

        Utilities.createRtu(rtuType);
        result = Utilities.mw().getRtuFactory().findBySerialNumber("99999999");

        assertEquals(1, result.size());
        assertEquals("99999999", ((RtuImpl)result.get(0)).getShadow().getName());
        assertEquals(6, ((RtuImpl)result.get(0)).getShadow().getChannelShadows().size());
	}

	@Test
	public void addChannelTest() throws BusinessException, SQLException {
		CommunicationProtocol commProtocol;
		RtuType rtuType;
		Rtu rtu = null;
        commProtocol = Utilities.createCommunicationProtocol(javaClassName);
        rtuType = Utilities.createRtuType(commProtocol, testRtu, 6);

        Utilities.createRtu(rtuType);
        List result = Utilities.mw().getRtuFactory().findBySerialNumber("99999999");

        assertEquals(1, result.size());
        rtu = (Rtu)result.get(0);

        rtu = Utilities.addChannel(rtu, TimeDuration.SECONDS, 7);
        rtu = Utilities.addChannel(rtu, TimeDuration.DAYS, 8);
        rtu = Utilities.addChannel(rtu, TimeDuration.MONTHS, 9);
        rtu = Utilities.addChannel(rtu, TimeDuration.MINUTES, 10);

        assertEquals(10, rtu.getShadow().getChannelShadows().size());
        assertEquals(new TimeDuration(1, TimeDuration.SECONDS), Utilities.getChannelWithProfileIndex(rtu, 7).getInterval());
        assertEquals(new TimeDuration(1, TimeDuration.DAYS), Utilities.getChannelWithProfileIndex(rtu, 8).getInterval());
        assertEquals(new TimeDuration(1, TimeDuration.MONTHS), Utilities.getChannelWithProfileIndex(rtu, 9).getInterval());
        assertEquals(new TimeDuration(1, TimeDuration.MINUTES), Utilities.getChannelWithProfileIndex(rtu, 10).getInterval());
	}
}
