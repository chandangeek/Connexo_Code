/**
 *
 */
package com.energyict.dlms.cosem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

/**
 * @author jme
 */
public class DLMSClassIdTest {

	private static final int	NON_EXISTING_CLASSID	= -1;

	/**
	 * Test method for {@link com.energyict.dlms.cosem.DLMSClassId#getClassId()}.
	 */
	@Test
	public final void testGetClassId() {
		DLMSClassId[] values = DLMSClassId.values();
		for (DLMSClassId dlmsClassIdToCheck : values) {
			assertTrue(dlmsClassIdToCheck.getClassId() > 0);
			for (DLMSClassId dlmsClassId : values) {
				if ((dlmsClassId.getClassId() == dlmsClassIdToCheck.getClassId()) && (!dlmsClassId.equals(dlmsClassIdToCheck))) {
					String message = "The class ID number should be unique for each class type, but ";
					message += dlmsClassId + " and " + dlmsClassIdToCheck;
					message += " both have a value of " + dlmsClassId.getClassId();
					fail(message);
				}
			}
		}
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.DLMSClassId#findById(int)}.
	 */
	@Test
	public final void testfindById() {
		DLMSClassId[] values = DLMSClassId.values();
		for (DLMSClassId dlmsClassId : values) {
			try {
				assertEquals(dlmsClassId, DLMSClassId.findById(dlmsClassId.getClassId()));
			} catch (IOException e) {
				fail("This should not generate an IOException, because [" + dlmsClassId + "] with id=" + dlmsClassId.getClassId() + " exists for sure.");
			}
		}

		try {
			DLMSClassId.findById(NON_EXISTING_CLASSID);
			fail("Requested nonexisting DLMSClassId [" + NON_EXISTING_CLASSID + "], but no exception was catched!");
		} catch (IOException e) {}

	}


}
