package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.ProtocolType;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.util.Arrays;

/**
 * Represents non-encrypted Application Layer packet header data.
 */
public class AdditionalAuthenticatedData extends LittleEndianData {
    public static final byte SIZE = 0x15;
    private SecurityScheme encryptionScheme;   // 1 byte set during encryption
    private final byte     protocolVersion;    // 1 byte
    private final short    sourceOptions;      // 1 byte
    private final short    destinationOptions; // 1 byte
    private final UmiId    sourceUmiId;        // 8 bytes
    private final UmiId    destinationUmiId;   // 8 bytes
    private int           encryptionOptions;  // 1 byte set during encryption

    public static class Builder {
        private SecurityScheme encryptionScheme    // 1 byte set during encryption
                = SecurityScheme.NO_SECURITY;

        private short          sourceOptions;      // 1 byte
        private short          destinationOptions; // 1 byte
        private UmiId          sourceUmiId;        // 8 bytes
        private UmiId          destinationUmiId;   // 8 bytes
        private int           encryptionOptions;  // 1 byte set during encryption

        public Builder encryptionScheme(SecurityScheme scheme) {
            encryptionScheme = scheme;
            return this;
        }

        public Builder sourceOptions(short options) {
            sourceOptions = options;
            return this;
        }

        public Builder destinationOptions(short options) {
            destinationOptions = options;
            return this;
        }

        public Builder sourceUmiId(UmiId umiId) {
            sourceUmiId = umiId;
            return this;
        }

        public Builder destinationUmiId(UmiId umiId) {
            destinationUmiId = umiId;
            return this;
        }

        public Builder encryptionOptions(int options) {
            encryptionOptions = options;
            return this;
        }

        public AdditionalAuthenticatedData build() {
            return new AdditionalAuthenticatedData(this);
        }
    }

    private AdditionalAuthenticatedData(Builder builder) {
        super(SIZE);
        protocolVersion = (byte) ((ProtocolType.UMI_COMMAND_RESPONSE.getId() << 4) |
                (AppLayerPacket.UMI_APP_PROTOCOL_VERSION & 0xF));

        encryptionScheme = builder.encryptionScheme;
        sourceOptions = builder.sourceOptions;
        destinationOptions = builder.destinationOptions;
        encryptionOptions = builder.encryptionOptions;
        sourceUmiId = builder.sourceUmiId;
        destinationUmiId = builder.destinationUmiId;

        getRawBuffer()
                .put((byte) encryptionScheme.getId())
                .put(protocolVersion)
                .put((byte) sourceOptions)
                .put((byte) destinationOptions)
                .put(sourceUmiId.getRaw())
                .put(destinationUmiId.getRaw())
                .put((byte)encryptionOptions);
    }

    public AdditionalAuthenticatedData(byte[] rawData) {
        super(rawData.clone());
        if (rawData.length != SIZE)
            throw new java.security.InvalidParameterException(
                "Invalid raw AAD size=" + rawData.length + ", required size=" + SIZE
            );
        encryptionScheme = SecurityScheme.fromId(getRawBuffer().get());
        protocolVersion = getRawBuffer().get();
        sourceOptions = getRawBuffer().get();
        destinationOptions = getRawBuffer().get();

        byte[] sourceUmiIdRaw = new byte[UmiId.SIZE];
        getRawBuffer().get(sourceUmiIdRaw);
        sourceUmiId = new UmiId(sourceUmiIdRaw, true);

        byte[] destUmiIdRaw = new byte[UmiId.SIZE];
        getRawBuffer().get(destUmiIdRaw);
        destinationUmiId = new UmiId(destUmiIdRaw, true);

        encryptionOptions = Byte.toUnsignedInt(getRawBuffer().get());
    }

    public SecurityScheme getEncryptionScheme() {
        return encryptionScheme;
    }

    public AdditionalAuthenticatedData setEncryptionScheme(SecurityScheme encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        getRawBuffer().position(0);
        getRawBuffer().put((byte) this.encryptionScheme.getId());
        getRawBuffer().position(getRawBuffer().limit());
        return this;
    }

    public byte getProtocolVersion() {
        return protocolVersion;
    }

    public short getSourceOptions() {
        return sourceOptions;
    }

    public short getDestinationOptions() {
        return destinationOptions;
    }

    public UmiId getSourceUmiId() {
        return sourceUmiId;
    }

    public UmiId getDestinationUmiId() {
        return destinationUmiId;
    }

    public int getEncryptionOptions() {
        return encryptionOptions;
    }

    public AdditionalAuthenticatedData setEncryptionOptions(int encryptionOptions) {
        this.encryptionOptions = encryptionOptions;
        getRawBuffer().position(getRawBuffer().limit() - 1);
        getRawBuffer().put((byte)encryptionOptions);
        getRawBuffer().position(getRawBuffer().limit());
        return this;
    }

    public byte[] getToBeSigned() {
        return Arrays.copyOfRange(getRaw(), SecurityScheme.SIZE, getLength());
    }
}
