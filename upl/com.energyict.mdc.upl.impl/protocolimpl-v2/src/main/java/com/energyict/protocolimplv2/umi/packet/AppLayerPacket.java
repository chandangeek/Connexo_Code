package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.IData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class AppLayerPacket implements IData {
    public static final byte UMI_APP_PROTOCOL_VERSION = 1;

    private AdditionalAuthenticatedData additionalAuthData;
    private HeaderPayloadData           headerPayloadData;
    private IData                       payload;            // variable length
    private IData                       signature;          // variable length

    /**
     * Constructor
     * @param additionalAuthData
     * @param headerPayloadData
     * @param payload
     */
    public AppLayerPacket(AdditionalAuthenticatedData additionalAuthData, HeaderPayloadData headerPayloadData, IData payload) {
        this.additionalAuthData = additionalAuthData;
        this.headerPayloadData = headerPayloadData;
        this.payload = payload;
        this.signature = new LittleEndianData(0);
    }

    /**
     * Constructor used for AppLayerPacket instance construction after raw packet decryption
     * @param additionalAuthData non-encrypted header part.
     * @param rawData raw data which includes header payload, payload, signature.
     */
    public AppLayerPacket(AdditionalAuthenticatedData additionalAuthData, byte[] rawData) {
        this.additionalAuthData = additionalAuthData;

        try {
            int fromIndex = 0;
            int toIndex = HeaderPayloadData.SIZE;

            byte[] rawHeaderPayloadData = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.headerPayloadData = new HeaderPayloadData(rawHeaderPayloadData);

            fromIndex = toIndex;
            toIndex = fromIndex + this.headerPayloadData.getPayloadLength();
            byte[] rawPayload = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.payload = new LittleEndianData(rawPayload);

            fromIndex = toIndex;
            toIndex = rawData.length;
            byte[] rawSignature = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.signature = new LittleEndianData(rawSignature);
        } catch (Exception e) {
            throw new java.security.InvalidParameterException(
                    "Invalid raw data size=" + rawData.length + ", unable to deserialize application layer packet from raw data. " +
                            "The original error message is: " + e.getMessage()
            );
        }
    }

    public AdditionalAuthenticatedData getAdditionalAuthData() {
        return additionalAuthData;
    }

    public HeaderPayloadData getHeaderPayloadData() {
        return headerPayloadData;
    }

    public IData getPayload() {
        return payload;
    }

    public IData getSignature() {
        return signature;
    }

    public void setSignature(IData signature) {
        this.signature = signature;
    }

    public void setPayload(IData payload) {
        this.payload = payload;
    }

    @Override
    public byte[] getRaw() {
        int length = getLength();
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(additionalAuthData.getRaw()).put(headerPayloadData.getRaw()).put(payload.getRaw());
        buffer.put(signature.getRaw());
        return buffer.array();
    }

    @Override
    public int getLength() {
        int length = additionalAuthData.getLength() + headerPayloadData.getLength() + payload.getLength();
        length += signature.getLength();
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppLayerPacket)) return false;
        AppLayerPacket that = (AppLayerPacket) o;
        return additionalAuthData.equals(that.additionalAuthData)
                && headerPayloadData.equals(that.headerPayloadData)
                && payload.equals(that.payload)
                && signature.equals(that.signature);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(additionalAuthData, headerPayloadData, payload, signature);
        return result;
    }

    public byte[] getToBeSigned() {
        byte[] aadTBS = additionalAuthData.getToBeSigned();
        byte[] toBeSigned = new byte[aadTBS.length + headerPayloadData.getLength() + payload.getLength()];
        headerPayloadData.getRaw();
        payload.getRaw();
        System.arraycopy(aadTBS, 0, toBeSigned, 0, aadTBS.length);
        System.arraycopy(headerPayloadData.getRaw(), 0, toBeSigned, aadTBS.length, headerPayloadData.getLength());
        System.arraycopy(payload.getRaw(), 0, toBeSigned,
                aadTBS.length + headerPayloadData.getLength(),
                payload.getLength());
        return toBeSigned;
    }

    public byte[] getToBeEncrypted() {
        int length = getLength() - AdditionalAuthenticatedData.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(headerPayloadData.getRaw()).put(payload.getRaw()).put(signature.getRaw());
        return buffer.array();
    }
}
