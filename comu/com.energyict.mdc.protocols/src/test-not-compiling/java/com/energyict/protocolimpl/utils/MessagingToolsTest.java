package com.energyict.protocolimpl.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

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
		MessageEntry validEntry = new MessageEntry("<mainTag testTag=\"value\"></mainTag>", "123");
		MessageEntry invalidEntry = new MessageEntry("<mainTag testTag=\"value></mainTag>", "123");

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
