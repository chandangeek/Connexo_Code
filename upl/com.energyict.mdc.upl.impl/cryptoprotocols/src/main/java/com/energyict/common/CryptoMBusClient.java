package com.energyict.common;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ObjectReference;

import java.io.IOException;

/**
 * Specific implementation for Cryptoserver usage only.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/03/13
 * Time: 13:38
 * Author: khe
 */
public class CryptoMBusClient extends MBusClient {

    public CryptoMBusClient(ProtocolLink protocolLink, ObjectReference objectReference, int version) {
        super(protocolLink, objectReference, version);
    }

    public CryptoMBusClient(MBusClient mBusClient, int version) {
        super(mBusClient.getProtocolLink(), mBusClient.getObjectReference(), version);
    }

    /**
     * Send the request to execute method 8 (set encryption key). It is already encrypted by the Cryptoserver.
     */
    public void sendSetEncryptionKeyRequest(byte[] encryptedRequest) throws IOException {
        byte[] responseData = this.protocolLink.getDLMSConnection().sendRequest(encryptedRequest, true);
        checkCosemPDUResponseHeader(responseData);
    }
}
