/**
 *
 */
package com.energyict.dlms.mocks;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class MockBrokenDLMSConnection extends MockDLMSConnection {

	@Override
	public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
		throw new IOException("Dummy IOException");
	}

}
