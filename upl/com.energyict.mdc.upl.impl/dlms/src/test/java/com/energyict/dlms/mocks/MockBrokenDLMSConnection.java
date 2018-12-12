/**
 *
 */
package com.energyict.dlms.mocks;

import java.io.IOException;

/**
 * @author jme
 */
public class MockBrokenDLMSConnection extends MockDLMSConnection {

    @Override
    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        throw new IOException("Dummy IOException");
    }

    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        throw new IOException("Dummy IOException");
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        throw new IOException("Dummy IOException during readResponseWithRetries method");
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        throw new IOException("Dummy IOException during readResponseWithRetries method");
    }
}
