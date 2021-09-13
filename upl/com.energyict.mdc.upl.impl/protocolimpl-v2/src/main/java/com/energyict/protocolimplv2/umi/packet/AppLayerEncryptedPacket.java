package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Arrays;

public class AppLayerEncryptedPacket extends LittleEndianData {
    private AdditionalAuthenticatedData additionalAuthData; /**< part of the header */
    private byte[]                      cipherText;         /**< contains ciphertext and encryption info */

    public AppLayerEncryptedPacket(AdditionalAuthenticatedData additionalAuthData, byte[] cipherText) {
        super(AdditionalAuthenticatedData.SIZE + cipherText.length);
        this.additionalAuthData = additionalAuthData;
        this.cipherText = cipherText.clone();
        getRawBuffer().put(this.additionalAuthData.getRaw()).put(cipherText);
    }

    public AppLayerEncryptedPacket(byte[] rawPacket) {
        super(rawPacket.clone());
        if (rawPacket.length < AdditionalAuthenticatedData.SIZE)
            throw new java.security.InvalidParameterException(
                    "Invalid raw encrypted packet size="
                            + rawPacket.length + ", must be >"
                            + AdditionalAuthenticatedData.SIZE
            );
        byte[] rawAAD = Arrays.copyOfRange(rawPacket, 0, AdditionalAuthenticatedData.SIZE);
        this.additionalAuthData = new AdditionalAuthenticatedData(rawAAD);
        cipherText = Arrays.copyOfRange(rawPacket, AdditionalAuthenticatedData.SIZE, rawPacket.length);
    }

    public AdditionalAuthenticatedData getAdditionalAuthData() {
        return additionalAuthData;
    }

    public byte[] getCipherText() {
        return cipherText;
    }
}
