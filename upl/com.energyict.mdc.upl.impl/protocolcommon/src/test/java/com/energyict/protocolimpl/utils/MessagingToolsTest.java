package com.energyict.protocolimpl.utils;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author jme
 *
 */
public class MessagingToolsTest {

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.MessagingTools#getContentOfAttribute(MessageEntry, java.lang.String)}.
	 */
	@Test
	public final void testGetContentOfAttribute() {
		MessageEntry validEntry = MessageEntry.fromContent("<mainTag testTag=\"value\"></mainTag>").trackingId("123").finish();
		MessageEntry invalidEntry = MessageEntry.fromContent("<mainTag testTag=\"value></mainTag>").trackingId("123").finish();

		assertNotNull(MessagingTools.getContentOfAttribute(validEntry, "testTag"));
		assertNull(MessagingTools.getContentOfAttribute(validEntry, "testTag123"));
		assertEquals("value", MessagingTools.getContentOfAttribute(validEntry, "testTag"));

		assertNull(MessagingTools.getContentOfAttribute(invalidEntry, "testTag"));
		assertNull(MessagingTools.getContentOfAttribute(invalidEntry, "testTag123"));

		assertNull(MessagingTools.getContentOfAttribute(validEntry, "mainTag"));
		assertNull(MessagingTools.getContentOfAttribute(invalidEntry, "mainTag"));

		assertNull(MessagingTools.getContentOfAttribute(validEntry, " "));
		assertNull(MessagingTools.getContentOfAttribute(invalidEntry, " "));

		assertNull(MessagingTools.getContentOfAttribute(validEntry, ""));
		assertNull(MessagingTools.getContentOfAttribute(invalidEntry, ""));

	}

}
