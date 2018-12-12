/**
 *
 */
package com.energyict.protocolimpl.iec1107.ppmi1.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.energyict.protocol.IntervalStateBits;

/**
 * @author jme
 *
 */
public class LoadProfileStatusTest {

	/**
	 * Test method for {@link com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus#LoadProfileStatus(byte)}.
	 * Test method for {@link com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus#LoadProfileStatus(int)}.
	 */
	@Test
	public final void testLoadProfileStatus() {
		for (int i = 0; i < 0x0FF; i++) {
			assertEquals(new LoadProfileStatus(i).getEIStatus(), new LoadProfileStatus((byte) i).getEIStatus());
			assertEquals(new LoadProfileStatus(i).toString(), new LoadProfileStatus((byte) i).toString());
		}
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus#getEIStatus()}.
	 */
	@Test
	public final void testGetEIStatus() {
		assertEquals(IntervalStateBits.OTHER, new LoadProfileStatus(LoadProfileStatus.SS_DIAGNOSTIC_FLAG).getEIStatus());
		assertEquals(IntervalStateBits.CONFIGURATIONCHANGE | IntervalStateBits.SHORTLONG, new LoadProfileStatus(LoadProfileStatus.SS_WRITE_ACCESS).getEIStatus());
		assertEquals(IntervalStateBits.PHASEFAILURE, new LoadProfileStatus(LoadProfileStatus.SS_PARTIAL_DEMAND).getEIStatus());
		assertEquals(IntervalStateBits.REVERSERUN, new LoadProfileStatus(LoadProfileStatus.SS_REVERSE_RUN).getEIStatus());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileStatus#toString()}.
	 */
	@Test
	public final void testToString() {
		assertTrue(new LoadProfileStatus(LoadProfileStatus.SS_DIAGNOSTIC_FLAG).toString().contains("Diagnostic"));
		assertTrue(new LoadProfileStatus(LoadProfileStatus.SS_WRITE_ACCESS).toString().contains("Write"));
		assertTrue(new LoadProfileStatus(LoadProfileStatus.SS_PARTIAL_DEMAND).toString().contains("Partial"));
		assertTrue(new LoadProfileStatus(LoadProfileStatus.SS_REVERSE_RUN).toString().contains("Reverse"));
	}

}
