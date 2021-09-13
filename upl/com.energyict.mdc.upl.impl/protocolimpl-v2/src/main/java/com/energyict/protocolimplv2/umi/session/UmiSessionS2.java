package com.energyict.protocolimplv2.umi.session;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.packet.AppLayerPacket;
import com.energyict.protocolimplv2.umi.packet.AppPacketType;
import com.energyict.protocolimplv2.umi.packet.payload.EmptyPacketPayload;
import com.energyict.protocolimplv2.umi.properties.UmiSessionPropertiesS2;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class UmiSessionS2 extends UmiSession {
    private UmiSessionPropertiesS2 propertiesS2;

    public UmiSessionS2(ComChannel comChannel, UmiSessionPropertiesS2 properties) {
        super(comChannel, properties);
        propertiesS2 = properties;
    }

    private Pair<ResultCode, LittleEndianData> getCertificate() throws IOException, GeneralSecurityException {
        AppLayerPacket response = sendGeneric(propertiesS2.getDestinationUmiId(),
                AppPacketType.S2_GET_CERTIFICATE,
                AppPacketType.S2_GET_CERTIFICATE_RESPONSE);
        return new Pair(response.getHeaderPayloadData().getResultCode().get(), response.getPayload());
    }

    private ResultCode addCertificate() throws IOException, GeneralSecurityException {
        AppLayerPacket response = sendGeneric(new LittleEndianData(propertiesS2.getOwnCertificate().getEncoded()),
                AppPacketType.S2_ADD_CERTIFICATE,
                AppPacketType.S2_ADD_CERTIFICATE_RESPONSE);
        return response.getHeaderPayloadData().getResultCode().get();
    }

    public ResultCode startSession() throws IOException, GeneralSecurityException {
        if (isSessionEstablished()) {
            return ResultCode.OK;
        }

        ResultCode resultCode = super.startSession();
        if (propertiesS2.getPerformCertificateExchange()) {
            resultCode = exchangeCertificates();
        }

        if (resultCode == ResultCode.OK) {
            /** generate salt */
            byte[] saltA = UmiSessionS2Utils.generateSalt();

            /** send start session */
            AppLayerPacket response = sendGeneric(new LittleEndianData(saltA),
                    AppPacketType.S2_START_SESSION,
                    AppPacketType.S2_START_SESSION_RESPONSE);
            resultCode = response.getHeaderPayloadData().getResultCode().get();

            if (resultCode == ResultCode.OK) {
                /** get salt from remote device */
                byte[] saltB = response.getPayload().getRaw();

                /** create session key */
                byte[] sessionKey = UmiSessionS2Utils.createSessionKey(propertiesS2.getOwnPrivateKey(),
                        propertiesS2.getRemoteCertificate(), saltA, saltB);

                this.propertiesS2.setSessionKey(sessionKey);
                this.propertiesS2.setEstablished(true);
                this.propertiesS2.setEncryptionScheme(SecurityScheme.ASYMMETRIC);
            }
        }
        return resultCode;
    }

    public ResultCode endSession() throws IOException, GeneralSecurityException {
        ResultCode resultCode = ResultCode.NO_SESSION;
        if (isSessionEstablished()) {
            sendGeneric(new EmptyPacketPayload(), AppPacketType.S2_END_SESSION, AppPacketType.S2_END_SESSION_RESPONSE);
            resultCode = ResultCode.OK;
            this.propertiesS2.setEstablished(false);
        }
        return resultCode;
    }

    @Override
    public boolean isSessionEstablished() {
        return propertiesS2.isEstablished();
    }

    private ResultCode exchangeCertificates() throws IOException, GeneralSecurityException {
        /** first we need to get the certificate from remote device */
        Pair<ResultCode, LittleEndianData> pair = getCertificate();
        ResultCode resultCode = pair.getFirst();
        if (resultCode == ResultCode.OK) {
            UmiCVCCertificate cvcCertificate = new UmiCVCCertificate(pair.getLast().getRaw());
            propertiesS2.setRemoteCertificate(cvcCertificate);

            /** then we need to add our own certificate to the device */
            resultCode = addCertificate();
        }
        return resultCode;
    }
}
