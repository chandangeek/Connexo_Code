/**
 *
 */
package com.energyict.dlms.cosem;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author jme
 */
public class DLMSClassIdTest {

	/**
	 * Test method for {@link com.energyict.dlms.cosem.DLMSClassId#getClassId()}
	 * .
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

}
