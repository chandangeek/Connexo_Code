package com.energyict.protocolimplv2.umi.session;

import com.energyict.dlms.DLMSUtils;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.connection.UmiConnection;
import com.energyict.protocolimplv2.umi.connection.UmiTcpIpConnection;
import com.energyict.protocolimplv2.umi.link.UmiLinkSession;
import com.energyict.protocolimplv2.umi.packet.*;
import com.energyict.protocolimplv2.umi.packet.payload.*;
import com.energyict.protocolimplv2.umi.properties.UmiSessionProperties;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.*;
import com.energyict.protocolimplv2.umi.util.IData;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UmiSession implements IUmiSession {
    public static final UmiCode UMI_ID_CODE = new UmiCode("umi.0.0.0.0");
    public static final UmiId thisUmiId = new UmiId("9991253571665924");
    public static final UmiId destinationUmiId = new UmiId("9991253571665921");
    private static final Logger LOGGER = Logger.getLogger(UmiSession.class.getName());

    private UmiConnection connection;
    private UmiSessionProperties properties;
    private UmiLinkSession linkSession;

    public UmiSession(ComChannel comChannel, UmiSessionProperties properties) {
        this.connection = new UmiTcpIpConnection(comChannel, properties);
        this.properties = properties;
        this.linkSession = new UmiLinkSession(this.connection);
    }

    protected AppLayerPacket sendGeneric(IData payload, AppPacketType packetType, AppPacketType expectedResponseType)
            throws IOException, GeneralSecurityException {
        return sendGeneric(payload, packetType, expectedResponseType, true);
    }

    protected AppLayerPacket sendGeneric(IData payload, AppPacketType packetType, AppPacketType expectedResponseType, boolean txIncrementExpected)
            throws IOException, GeneralSecurityException {
        AdditionalAuthenticatedData authenticatedData = new AdditionalAuthenticatedData.Builder()
                .sourceOptions(properties.getSourceOptions())
                .destinationOptions(properties.getSourceOptions())
                .sourceUmiId(properties.getSourceUmiId())
                .destinationUmiId(properties.getDestinationUmiId())
                .build();
        HeaderPayloadData headerPayloadData = new HeaderPayloadData.Builder()
                .packetType(packetType)
                .payloadLength(payload)
                .respSignatureSchemeRequest(properties.getRespSignatureSchemeRequest())
                .signatureScheme(properties.getCmdSignatureScheme())
                .transactionNumber(properties.getTransactionNumber())
                .build();
        AppLayerPacket request = new AppLayerPacket(authenticatedData, headerPayloadData, payload);
        properties.getSecurity().sign(request);

        byte[] r = request.getRaw();
        StringBuilder toLogR = new StringBuilder("Request packet: \n");
        for (int i = 0; i < r.length; i++) {
            toLogR.append(String.format("0x%02X", r[i])).append(" ");
        }
        LOGGER.log(Level.FINEST, toLogR.toString());

        AppLayerEncryptedPacket encryptedPacket = properties.getSecurity().encrypt(request);

        byte[] response = linkSession.sendGenericLink(encryptedPacket);
        AppLayerEncryptedPacket encryptedResponsePacket = new AppLayerEncryptedPacket(response);

        if (!encryptedResponsePacket.getAdditionalAuthData().getDestinationUmiId().equals(properties.getSourceUmiId())) {
            throw new ProtocolException("Invalid response packet destination UMI ID=" +
                    encryptedResponsePacket.getAdditionalAuthData().getDestinationUmiId() +
                    " should be=" + properties.getSourceUmiId());
        }
        if (!encryptedResponsePacket.getAdditionalAuthData().getSourceUmiId().equals(properties.getDestinationUmiId())) {
            throw new ProtocolException("Invalid response packet source UMI ID=" +
                    encryptedResponsePacket.getAdditionalAuthData().getSourceUmiId() +
                    " should be=" + properties.getDestinationUmiId());
        }

        AppLayerPacket responsePacket = properties.getSecurity().decrypt(encryptedResponsePacket);
        byte[] dp = responsePacket.getRaw();
        StringBuilder toLogDp = new StringBuilder("Decrypted packet: \n");
        for (int i = 0; i < dp.length; i++) {
            toLogDp.append(String.format("0x%02X", dp[i])).append(" ");
        }
        LOGGER.log(Level.FINEST, toLogDp.toString());

        if (responsePacket.getHeaderPayloadData().getType() == AppPacketType.ERROR) {
            throw new ProtocolException(responsePacket.getHeaderPayloadData().getResultCode().isPresent()
                    ? responsePacket.getHeaderPayloadData().getResultCode().get().getDescription() : "Application layer error occurred");
        }

        // Deferred behaviour implementation
        if (responsePacket.getHeaderPayloadData().getType() == AppPacketType.WAIT) {
            // Deferred response case
            verifyAppPacketOrThrowException(request, responsePacket);
            ByteBuffer delayBuffer = ByteBuffer.wrap(responsePacket.getPayload().getRaw()).order(ByteOrder.LITTLE_ENDIAN);
            int delay = delayBuffer.getShort();
            DLMSUtils.delay(delay);
            properties.setEncryptionScheme(SecurityScheme.NO_SECURITY);
            responsePacket = sendGeneric(new LittleEndianData(0), AppPacketType.COMPLETE, expectedResponseType, false);
        }

        if (responsePacket.getHeaderPayloadData().getType() != expectedResponseType) {
            throw new ProtocolException("Incorrect packet type=(" +
                    responsePacket.getHeaderPayloadData().getType() + ") in response. Expected=" + expectedResponseType);
        }
        verifyAppPacketOrThrowException(request, responsePacket);
        if (txIncrementExpected) {
            properties.incrementTransactionNumber();
        }
        return responsePacket;
    }

    private void verifyAppPacketOrThrowException(AppLayerPacket request, AppLayerPacket responsePacket) throws IOException, GeneralSecurityException {
        if (responsePacket.getHeaderPayloadData().getTransactionNumber() != request.getHeaderPayloadData().getTransactionNumber()) {
            throw new ProtocolException("Incorrect transaction number=(" +
                    responsePacket.getHeaderPayloadData().getTransactionNumber() + ") in response packet.");
        }
        if (responsePacket.getHeaderPayloadData().getSignatureScheme() != request.getHeaderPayloadData().getRespSignatureSchemeRequest().get()) {
            throw new ProtocolException("Incorrect signature scheme=(" +
                    responsePacket.getHeaderPayloadData().getSignatureScheme() + ") in response packet");
        }
        if (!properties.getSecurity().verifySignature(responsePacket)) {
            throw new ProtocolException("Incorrect response packet signature");
        }
    }

    @Override
    public ResultCode noOperation() {
        return ResultCode.NOT_IMPLEMENTED;
    }

    @Override
    public ResultCode eventPublish(int events) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new EvtPublishCmdPayload(events),
                AppPacketType.EVENT_PUBLISH,
                AppPacketType.EVENT_PUBLISH_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode eventSubscribe(int events, SecurityScheme publishSecScheme) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new EvtSubscribeCmdPayload(events, publishSecScheme),
                AppPacketType.EVENT_SUBSCRIBE,
                AppPacketType.EVENT_SUBSCRIBE_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode imageStart(int totalLength) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new ImageStartCmdPayload(totalLength),
                AppPacketType.IMAGE_START,
                AppPacketType.IMAGE_START_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode imageData(byte[] dataBlock) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new ImageDataCmdPayload(dataBlock),
                AppPacketType.IMAGE_DATA,
                AppPacketType.IMAGE_DATA_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode imageEnd(boolean valid) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new ImageEndCmdPayload(valid),
                AppPacketType.IMAGE_END,
                AppPacketType.IMAGE_END_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public Pair<ResultCode, ReadObjRspPayload> readObject(UmiCode code) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return new Pair(ResultCode.NO_SESSION, null);
        }
        AppLayerPacket response = sendGeneric(new ReadObjCmdPayload(code),
                AppPacketType.READ_OBJECT,
                AppPacketType.READ_OBJECT_RESPONSE);
        ResultCode resultCode = response.getHeaderPayloadData().getResultCode().get();
        return new Pair(resultCode, resultCode.equals(ResultCode.OK) ? new ReadObjRspPayload(response.getPayload()) : null);
    }

    @Override
    public Pair<ResultCode, ReadObjPartRspPayload> readObjectPart(UmiObjectPart objectPart) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return new Pair(ResultCode.NO_SESSION, null);
        }
        AppLayerPacket response = sendGeneric(new ReadObjPartCmdPayload(objectPart),
                AppPacketType.READ_OBJECT_PART,
                AppPacketType.READ_OBJECT_PART_RESPONSE);
        ResultCode resultCode = response.getHeaderPayloadData().getResultCode().get();
        return new Pair(resultCode, resultCode.equals(ResultCode.OK) ? new ReadObjPartRspPayload(response.getPayload()) : null);
    }

    @Override
    public ResultCode writeObject(UmiCode code, byte[] value) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new WriteObjCmdPayload(code, value),
                AppPacketType.WRITE_OBJECT,
                AppPacketType.WRITE_OBJECT_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode writeObjectPart(UmiObjectPart objectPart, byte[] value) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new WriteObjPartCmdPayload(objectPart, value),
                AppPacketType.WRITE_OBJECT_RESPONSE,
                AppPacketType.WRITE_OBJECT_PART_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public Pair<ResultCode, IData> tunnelData(IData data) {
        return new Pair<>(ResultCode.NOT_IMPLEMENTED, null);
    }

    @Override
    public Pair<ResultCode, IData> tunnelUmi(IData packet) {
        return new Pair<>(ResultCode.NOT_IMPLEMENTED, null);
    }

    @Override
    public Pair<ResultCode, GetObjAccessRspPayload> getObjectAccess(UmiCode code) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return new Pair(ResultCode.NO_SESSION, null);
        }
        AppLayerPacket response = sendGeneric(new GetObjAccessCmdPayload(code),
                AppPacketType.GET_OBJECT_ACCESS,
                AppPacketType.GET_OBJECT_ACCESS_RESPONSE);
        ResultCode resultCode = response.getHeaderPayloadData().getResultCode().get();
        return new Pair(resultCode, resultCode.equals(ResultCode.OK) ? new GetObjAccessRspPayload(response.getPayload()) : null);
    }

    @Override
    public ResultCode setObjectReadAccess(UmiCode code, List<Role> roles) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new SetObjAccessCmdPayload(code, roles),
                AppPacketType.SET_OBJECT_READ_ACCESS,
                AppPacketType.SET_OBJECT_READ_ACCESS_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode setObjectWriteAccess(UmiCode code, List<Role> roles) throws IOException, GeneralSecurityException {
        if (!isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        AppLayerPacket response = sendGeneric(new SetObjAccessCmdPayload(code, roles),
                AppPacketType.SET_OBJECT_WRITE_ACCESS,
                AppPacketType.SET_OBJECT_WRITE_ACCESS_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    @Override
    public ResultCode startSession() throws IOException, GeneralSecurityException {
        linkSession.sendUmiResync();
        if (!linkSession.isSessionEstablished()) {
            return ResultCode.NO_SESSION;
        }
        return ResultCode.OK;
    }

    @Override
    public ResultCode endSession() throws IOException, GeneralSecurityException {
        return ResultCode.OK;
    }

    @Override
    public boolean isSessionEstablished() {
        return linkSession.isSessionEstablished();
    }
}
