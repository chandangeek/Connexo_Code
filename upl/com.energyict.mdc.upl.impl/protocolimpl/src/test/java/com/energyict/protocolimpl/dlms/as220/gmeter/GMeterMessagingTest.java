/**
 * 
 */
package com.energyict.protocolimpl.dlms.as220.gmeter;


import static org.junit.Assert.assertEquals;

import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gna
 * @since 2-mrt-2010
 *
 */
public class GMeterMessagingTest {

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
	}
	
	/**
	 * Test to get the proper result from the message
	 */
	@Test
	public final void getMessageValueTest(){
		String content = "<EnableEncryption Open_Key_Value=\"00000000000000000000000000000000\" Transfer_Key_Value=\"bf7f729d78ccf8dde69a22fcd19bebd4\"> </EnableEncryption>";
		GMeterMessaging messaging = new GMeterMessaging(null);
		assertEquals("00000000000000000000000000000000", messaging.getMessageValue(content, RtuMessageConstant.MBUS_OPEN_KEY));
		assertEquals("bf7f729d78ccf8dde69a22fcd19bebd4", messaging.getMessageValue(content, RtuMessageConstant.MBUS_TRANSFER_KEY));
	}

}
