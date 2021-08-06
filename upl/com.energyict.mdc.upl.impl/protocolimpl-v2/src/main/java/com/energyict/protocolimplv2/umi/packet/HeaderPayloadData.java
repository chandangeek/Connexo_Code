package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.ResultCode;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Represents usually encrypted Application Layer header data.
 */
public class HeaderPayloadData extends LittleEndianData {
    public static final byte SIZE = 0xB;
    private final SecurityScheme           signatureScheme;            // 1 byte
    private final int                      transactionNumber;          // 2 bytes
    private final long                     createdDatetime;            // 4 byte
    private final AppPacketType            type;                       // 1 byte
    private final Optional<SecurityScheme> respSignatureSchemeRequest; // 1 byte
    private final Optional<ResultCode>     resultCode;                 // 1 byte
    private final int                      payloadLength;              // 2 bytes

    public static class Builder {
        private SecurityScheme              signatureScheme;
        private int                         transactionNumber;
        private AppPacketType               packetType;
        private Optional<SecurityScheme>    respSignatureSchemeRequest = Optional.empty();
        private Optional<ResultCode>        resultCode = Optional.empty();
        private int                         payloadLength;

        public Builder signatureScheme(SecurityScheme scheme) {
            signatureScheme = scheme;
            return this;
        }

        public Builder respSignatureSchemeRequest(SecurityScheme scheme) {
            respSignatureSchemeRequest = Optional.of(scheme);
            return this;
        }

        public Builder resultCode(ResultCode code) {
            resultCode = Optional.of(code);
            return this;
        }

        public Builder transactionNumber(int transaction) {
            transactionNumber = transaction;
            return this;
        }

        public Builder packetType(AppPacketType type) {
            packetType = type;
            return this;
        }

        public Builder payloadLength(IData payload) {
            payloadLength = payload.getLength();
            return this;
        }

        public HeaderPayloadData build() {
            return new HeaderPayloadData(this);
        }
    }

    private HeaderPayloadData(Builder builder) {
        super(SIZE);
        createdDatetime            = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime());
        signatureScheme            = builder.signatureScheme;
        transactionNumber          = builder.transactionNumber;
        type                       = builder.packetType;
        resultCode                 = builder.resultCode;
        payloadLength              = builder.payloadLength;
        respSignatureSchemeRequest = builder.respSignatureSchemeRequest;

        getRawBuffer().put((byte)signatureScheme.getId())
                .putShort((short) transactionNumber)
                .putInt((int)createdDatetime)
                .put((byte) type.getId());
        if (respSignatureSchemeRequest.isPresent()) {
            getRawBuffer().put((byte) respSignatureSchemeRequest.get().getId());
        } else {
            getRawBuffer().put((byte) resultCode.get().getId());
        }
        getRawBuffer().putShort((short) payloadLength);
    }

    public HeaderPayloadData(byte[] rawHeaderPayloadData) {
        super(rawHeaderPayloadData.clone());
        if (rawHeaderPayloadData.length != SIZE)
            throw new java.security.InvalidParameterException(
                    "Invalid raw header payload size=" + rawHeaderPayloadData.length + ", required size=" + SIZE
            );
        signatureScheme = SecurityScheme.fromId(getRawBuffer().get());
        transactionNumber = getRawBuffer().getShort();
        createdDatetime = getRawBuffer().getInt();
        type = AppPacketType.fromId(getRawBuffer().get());

        if (type.isCmd()) {
            respSignatureSchemeRequest = Optional.of(SecurityScheme.fromId(getRawBuffer().get()));
            resultCode = Optional.empty();
        } else {
            respSignatureSchemeRequest = Optional.empty();
            resultCode = Optional.of(ResultCode.fromId(getRawBuffer().get()));
        }
        payloadLength = getRawBuffer().getShort();
    }

    public SecurityScheme getSignatureScheme() {
        return signatureScheme;
    }

    public int getTransactionNumber() {
        return transactionNumber;
    }

    public long getCreatedDatetime() {
        return createdDatetime;
    }

    public AppPacketType getType() {
        return type;
    }

    public Optional<SecurityScheme> getRespSignatureSchemeRequest() {
        return respSignatureSchemeRequest;
    }

    public Optional<ResultCode> getResultCode() {
        return resultCode;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeaderPayloadData that = (HeaderPayloadData) o;
        return transactionNumber == that.transactionNumber &&
                createdDatetime == that.createdDatetime &&
                payloadLength == that.payloadLength &&
                signatureScheme == that.signatureScheme &&
                type == that.type
                && respSignatureSchemeRequest.equals(that.respSignatureSchemeRequest) &&
                resultCode.equals(that.resultCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signatureScheme, transactionNumber,
                createdDatetime, type,
                respSignatureSchemeRequest, resultCode,
                payloadLength);
    }
}
