package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class LinkLayerFrame implements IData {
    public static final byte UMI_LINK_PROTOCOL_VERSION = 1;

    private LinkFrameHeaderData linkFrameHeaderData;
    private IData payload;      // variable length
    private IData crc;          // 2

    public static class Builder {
        private LinkFrameHeaderData linkFrameHeaderData;
        private IData payload;      // variable length

        public Builder setLinkFrameHeaderData(LinkFrameHeaderData linkFrameHeaderData) {
            this.linkFrameHeaderData = linkFrameHeaderData;
            return this;
        }

        public Builder setPayload(IData payload) {
            this.payload = payload;
            return this;
        }

        public LinkLayerFrame build() {
            return new LinkLayerFrame(linkFrameHeaderData, payload);
        }

    }

    private LinkLayerFrame(LinkFrameHeaderData linkFrameHeaderData, IData payload) {
        this.linkFrameHeaderData = linkFrameHeaderData;
        this.payload = payload;
        this.crc = new LittleEndianData(calculateCrc());
    }

    public LinkLayerFrame(byte[] rawData) {
        try {
            int fromIndex = 0;
            int toIndex = LinkFrameHeaderData.SIZE;

            byte[] rawHeaderPayloadData = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.linkFrameHeaderData = new LinkFrameHeaderData(rawHeaderPayloadData);

            fromIndex = toIndex;
            toIndex = fromIndex + this.linkFrameHeaderData.getFramePayloadLength();
            byte[] rawPayload = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.payload = new LittleEndianData(rawPayload);

            fromIndex = toIndex;
            toIndex = rawData.length;
            byte[] rawCrc = Arrays.copyOfRange(rawData, fromIndex, toIndex);
            this.crc = new LittleEndianData(rawCrc);
        } catch (Exception e) {
            throw new java.security.InvalidParameterException(
                    "Invalid raw data size=" + rawData.length + ", unable to deserialize link layer frame from raw data."
            );
        }
    }

    public LinkFrameHeaderData getLinkFrameHeaderData() {
        return linkFrameHeaderData;
    }

    public void setLinkFrameHeaderData(LinkFrameHeaderData linkFrameHeaderData) {
        this.linkFrameHeaderData = linkFrameHeaderData;
    }

    public IData getPayload() {
        return payload;
    }

    public void setPayload(IData payload) {
        this.payload = payload;
    }

    public IData getCrc() {
        return crc;
    }

    public void setCrc(IData crc) {
        this.crc = crc;
    }


    @Override
    public byte[] getRaw() {
        int length = getLength();
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(linkFrameHeaderData.getRaw()).put(payload.getRaw()).put(crc.getRaw());
        return buffer.array();
    }

    @Override
    public int getLength() {
        int length = linkFrameHeaderData.getLength() + payload.getLength() + crc.getLength();
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkLayerFrame)) return false;
        LinkLayerFrame that = (LinkLayerFrame) o;
        return linkFrameHeaderData.equals(that.linkFrameHeaderData)
                && payload.equals(that.payload)
                && crc.equals(that.crc);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(linkFrameHeaderData, payload, crc);
        return result;
    }

    /**
     * Calculates a 16-bit CRC on an array of bytes.
     *
     *     The CRC uses the CCITT standard polynomial X^16 + X^12 + X^5 + 1
     *     Data bit order is ls-bit first.
     *     Data byte order is ls-byte first.
     *     CRC is not bit reversed.
     * @return byte[] crc raw value in little-endian format
     */
    public byte[] calculateCrc() {
        int size = this.linkFrameHeaderData.getLength() + this.payload.getLength();
        short poly = 0x1021;
        int current = 0x00;
        int crc = 0;
        int index = 0;

        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(linkFrameHeaderData.getRaw()).put(payload.getRaw());

        while (size-- > 0) {
            current = buffer.get(index++);
            for (int i = 0; i < 8; i++) {
                if ((current & 0x01) == 0x01) {
                    crc ^= 0x8000;
                }
                current = (current >> 1);
                crc = ((crc << 1) ^ (((crc & 0x8000) != 0x0000) ? poly : 0));
            }
        }
        ByteBuffer bufferCrc = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
        bufferCrc.putShort((short)crc);
        return bufferCrc.array();
    }
}
