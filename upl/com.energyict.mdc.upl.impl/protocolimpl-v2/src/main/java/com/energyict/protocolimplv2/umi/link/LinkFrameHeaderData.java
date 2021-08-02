package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

/**
 * Represents link layer header data.
 */

public class LinkFrameHeaderData extends LittleEndianData {
    public static final byte SIZE = 0x4;

    private final short  sequenceVersion;        // 1 byte [s s s s v v v v]
    private final short  busyReservedFrametype;  // 1 byte [b r r r f f f f]
    private final short framePayloadLength;     // 1 byte
    private final short destinationSource;       // 1 byte [d d d d s s s s]

    public static class Builder {
        private byte sequence = 0;
        private byte version = LinkLayerFrame.UMI_LINK_PROTOCOL_VERSION;
        private byte busy = 0;
        private byte reserved = 0;
        private LinkFrameType frameType = LinkFrameType.SIMPLE;
        private short framePayloadLength = 0;
        private byte destination = 0;
        private byte source = 0;

        public Builder setSequence(byte sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder setVersion(byte version) {
            this.version = version;
            return this;
        }

        public Builder setBusy(byte busy) {
            this.busy = busy;
            return this;
        }

        public Builder setReserved(byte reserved) {
            this.reserved = reserved;
            return this;
        }

        public Builder setFrameType(LinkFrameType frameType) {
            this.frameType = frameType;
            return this;
        }

        public Builder setFramePayloadLength(int payloadLength) {
            this.framePayloadLength = (short)payloadLength;
            return this;
        }

        public Builder setDestination(byte destination) {
            this.destination = destination;
            return this;
        }

        public Builder setSource(byte source) {
            this.source = source;
            return this;
        }

        public LinkFrameHeaderData build() {
            return new LinkFrameHeaderData(this);
        }


    }

    public LinkFrameHeaderData(byte[] rawData) {
        super(rawData.clone());
        if (rawData.length != SIZE)
            throw new java.security.InvalidParameterException(
                    "Invalid raw link frame header size = " + rawData.length + ", required size = " + SIZE
            );
        this.sequenceVersion = getRawBuffer().get();
        this.busyReservedFrametype = getRawBuffer().get();
        this.framePayloadLength = getRawBuffer().get();
        this.destinationSource = getRawBuffer().get();
    }

    private LinkFrameHeaderData(Builder builder) {
        super(SIZE);
        this.sequenceVersion = (short) ((builder.sequence << 4) | (LinkLayerFrame.UMI_LINK_PROTOCOL_VERSION & 0xF));
        this.busyReservedFrametype = (short) ((builder.busy << 7) | ((byte)(builder.frameType.getId()) & 0xF));
        this.framePayloadLength = builder.framePayloadLength;
        this.destinationSource = (short) ((builder.destination << 4) | (builder.source & 0xF));

        getRawBuffer()
                .put((byte)this.sequenceVersion)
                .put((byte)this.busyReservedFrametype)
                .put((byte)framePayloadLength)
                .put((byte)destinationSource);

    }

    public short getSequenceVersion() {
        return this.sequenceVersion;
    }

    public short getBusyReservedFrametype() {
        return this.busyReservedFrametype;
    }

    public short getFramePayloadLength() {
        return (short)(this.framePayloadLength & 0xFF);
    }

    public short getDestinationSource() {
        return this.destinationSource;
    }

    public byte getSequence() {
        return (byte)(this.sequenceVersion >> 4 & 0x0F);
    }

    public byte getVersion() {
        return (byte) (this.sequenceVersion & 0x0F);
    }

    public boolean isBusy() {
        return ((byte)(this.busyReservedFrametype >> 7) & 0x01) == 0x01;
    }

    public LinkFrameType getLinkFrameType() {
        return LinkFrameType.fromId(this.busyReservedFrametype & 0x0F);
    }

    public byte getDestination() {
        return (byte) (this.destinationSource >> 4 & 0x0F);
    }

    public byte getSource() {
        return (byte) (this.destinationSource & 0x0F);
    }
}
