/**
 *
 */
package com.energyict.dlms.cosem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.mocks.MockBrokenDLMSConnection;
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
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getDefaultObisCode()}.
	 */
	@Test
	public final void testGetObisCode() {
		assertNotNull(SFSKMacCounters.getDefaultObisCode());
		assertEquals(ObisCode.fromString("0.0.26.3.0.255"), SFSKMacCounters.getDefaultObisCode());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getSynchronizationRegister()}.
	 */
	@Test
	public final void testGetSynchronizationRegister() {
		assertNull(getEmptySFSKMacCounter().getSynchronizationRegister());
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
		assertNull(getEmptySFSKMacCounter().getDesynchronizationListing());
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
		assertNull(getEmptySFSKMacCounter().getBroadcastFramesCounter());
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
	@Test
	public final void testGetRepetitionsCounter() {
		assertNull(getEmptySFSKMacCounter().getRepetitionsCounter());
		assertNotNull(getSFSKMacCounter().getRepetitionsCounter());
		assertTrue(getSFSKMacCounter().getRepetitionsCounter().isUnsigned32());
		assertEquals(305419776, getSFSKMacCounter().getRepetitionsCounter().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getTransmissionsCounter()}.
	 */
	@Test
	public final void testGetTransmissionsCounter() {
		assertNull(getEmptySFSKMacCounter().getTransmissionsCounter());
		assertNotNull(getSFSKMacCounter().getTransmissionsCounter());
		assertTrue(getSFSKMacCounter().getTransmissionsCounter().isUnsigned32());
		assertEquals(3430008, getSFSKMacCounter().getTransmissionsCounter().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getCrcOkFramesCounter()}.
	 */
	@Test
	public final void testGetCrcOkFramesCounter() {
		assertNull(getEmptySFSKMacCounter().getCrcOkFramesCounter());
		assertNotNull(getSFSKMacCounter().getCrcOkFramesCounter());
		assertTrue(getSFSKMacCounter().getCrcOkFramesCounter().isUnsigned32());
		assertEquals(302012024, getSFSKMacCounter().getCrcOkFramesCounter().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#getCrcNOkFramesCounter()}.
	 */
	@Test
	public final void testGetCrcNOkFramesCounter() {
		assertNull(getEmptySFSKMacCounter().getCrcNOkFramesCounter());
		assertNotNull(getSFSKMacCounter().getCrcNOkFramesCounter());
		assertTrue(getSFSKMacCounter().getCrcNOkFramesCounter().isUnsigned32());
		assertEquals(305397880, getSFSKMacCounter().getCrcNOkFramesCounter().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#invokeResetData()}.
	 */
	@Test
	public final void testInvokeResetData() {
		try {
			getSFSKMacCounter().invokeResetData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#toString()}.
	 */
	@Test
	public final void testToString() {
		assertNotNull(getSFSKMacCounter().toString());
		assertNotNull(getEmptySFSKMacCounter().toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.SFSKMacCounters#asRegisterValue()}.
	 */
	@Test
	public final void testAsRegisterValue() {
		assertNotNull(getEmptySFSKMacCounter().asRegisterValue());
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
	private static SFSKMacCounters getEmptySFSKMacCounter() {
		MockProtocolLink protocolLink = new MockProtocolLink(new MockBrokenDLMSConnection());
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
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$20", "$90$02$01$0C$01$00$06$12$34$56$00");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$28", "$90$02$01$0C$01$00$06$00$34$56$78");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$30", "$90$02$01$0C$01$00$06$12$00$56$78");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$05$01$02$00$38", "$90$02$01$0C$01$00$06$12$34$00$78");
		dlmsConnection.addRequestResponsePair("$E6$E6$00$06$01$02$00$50$01$12$00$00", "$90$02$01$0D$01$00");
		return dlmsConnection;
	}

}
