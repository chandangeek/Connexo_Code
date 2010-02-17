/**
 *
 */
package com.energyict.dlms.cosem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class SFSKMacCountersTest {

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getClassId()}.
	 */
	@Test
	public final void testGetClassId() {
		assertEquals(DLMSClassId.S_FSK_MAC_COUNTERS.getClassId(), getSFSKMacCounter().getClassId());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getObisCode()}.
	 */
	@Test
	public final void testGetObisCode() {
		assertNotNull(SFSKMacCounters.getObisCode());
		assertEquals(ObisCode.fromString("0.0.26.3.0.255"), SFSKMacCounters.getObisCode());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getSynchronizationRegister()}.
	 */
	@Test
	public final void testGetSynchronizationRegister() {
		assertNotNull(getSFSKMacCounter().getSynchronizationRegister());
		assertTrue(getSFSKMacCounter().getSynchronizationRegister().isArray());
		assertEquals(1, getSFSKMacCounter().getSynchronizationRegister().nrOfDataTypes());

		assertTrue(getSFSKMacCounter().getSynchronizationRegister().getDataType(0).isStructure());
		assertEquals(2, getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().nrOfDataTypes());

		assertNotNull(getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(0));
		assertTrue(getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(0).isUnsigned16());
		assertEquals(4660, getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(0).getUnsigned16().getValue());

		assertNotNull(getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(1));
		assertTrue(getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(1).isUnsigned32());
		assertEquals(305419896, getSFSKMacCounter().getSynchronizationRegister().getDataType(0).getStructure().getDataType(1).getUnsigned32().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getDesynchronizationListing()}.
	 */
	@Test
	public final void testGetDesynchronizationListing() {
		assertNotNull(getSFSKMacCounter().getDesynchronizationListing());
		assertTrue(getSFSKMacCounter().getDesynchronizationListing().isStructure());
		assertEquals(5, getSFSKMacCounter().getDesynchronizationListing().nrOfDataTypes());

		for (int i = 0; i < 5; i++) {
			assertNotNull(getSFSKMacCounter().getDesynchronizationListing().getDataType(i));
			assertTrue(getSFSKMacCounter().getDesynchronizationListing().getDataType(i).isUnsigned32());
			assertNotNull(getSFSKMacCounter().getDesynchronizationListing().getDataType(i).getUnsigned32());
		}

		assertEquals(18, getSFSKMacCounter().getDesynchronizationListing().getDataType(0).getUnsigned32().getValue());
		assertEquals(52, getSFSKMacCounter().getDesynchronizationListing().getDataType(1).getUnsigned32().getValue());
		assertEquals(86, getSFSKMacCounter().getDesynchronizationListing().getDataType(2).getUnsigned32().getValue());
		assertEquals(120, getSFSKMacCounter().getDesynchronizationListing().getDataType(3).getUnsigned32().getValue());
		assertEquals(144, getSFSKMacCounter().getDesynchronizationListing().getDataType(4).getUnsigned32().getValue());

	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getBroadcastFramesCounter()}.
	 */
	@Test
	public final void testGetBroadcastFramesCounter() {
		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter());
		assertTrue(getSFSKMacCounter().getBroadcastFramesCounter().isArray());
		assertEquals(1, getSFSKMacCounter().getBroadcastFramesCounter().nrOfDataTypes());

		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0));
		assertTrue(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).isStructure());
		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure());
		assertEquals(2, getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().nrOfDataTypes());

		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(0));
		assertTrue(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(0).isUnsigned16());
		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(0).getUnsigned16());
		assertEquals(4660, getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(0).getUnsigned16().getValue());

		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(1));
		assertTrue(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(1).isUnsigned32());
		assertNotNull(getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(1).getUnsigned32());
		assertEquals(1450741778, getSFSKMacCounter().getBroadcastFramesCounter().getDataType(0).getStructure().getDataType(1).getUnsigned32().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getRepetitionsCounter()}.
	 */
	@Test @Ignore
	public final void testGetRepetitionsCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getTransmissionsCounter()}.
	 */
	@Test @Ignore
	public final void testGetTransmissionsCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getCrcOkFramesCounter()}.
	 */
	@Test @Ignore
	public final void testGetCrcOkFramesCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getCrcNOkFramesCounter()}.
	 */
	@Test @Ignore
	public final void testGetCrcNOkFramesCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#invokeResetData()}.
	 */
	@Test @Ignore
	public final void testInvokeResetData() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#toString()}.
	 */
	@Test
	public final void testToString() {
		assertNotNull(getSFSKMacCounter().toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#asRegisterValue()}.
	 */
	@Test
	public final void testAsRegisterValue() {
		assertNotNull(getSFSKMacCounter().asRegisterValue());
	}

	/**
	 * @return
	 */
	private static SFSKMacCounters getSFSKMacCounter() {
		MockProtocolLink protocolLink = new MockProtocolLink(getDlmsConnection());
		ObjectReference objectReference = new ObjectReference(0);
		SFSKMacCounters sfskMacCounters = new SFSKMacCounters(protocolLink, objectReference);
		return sfskMacCounters;
	}

	/**
	 * @return
	 */
	private static MockDLMSConnection getDlmsConnection() {
		MockDLMSConnection dlmsConnection = new MockDLMSConnection();
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$08", "$E6$E6$00$0C$01$00$01$01$02$02$12$12$34$06$12$34$56$78");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$10", "$90$02$01$0C$01$00$02$05$06$00$00$00$12$06$00$00$00$34$06$00$00$00$56$06$00$00$00$78$06$00$00$00$90");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$18", "$90$02$01$0C$01$00$01$01$02$02$12$12$34$06$56$78$90$12");
		return dlmsConnection;
	}

}
