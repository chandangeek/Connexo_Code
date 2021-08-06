package com.energyict.protocolimplv2.umi.security.scheme2;

import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;
import com.energyict.protocolimplv2.umi.packet.AppLayerPacket;
import com.energyict.protocolimplv2.umi.properties.UmiSessionPropertiesS2;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.security.scheme0.AppPacketSecurityS0;
import com.energyict.protocolimplv2.umi.signature.scheme2.AppPacketSignatureS2;
import com.energyict.protocolimplv2.umi.signature.scheme2.CmdSignatureS2;
import com.energyict.protocolimplv2.umi.signature.scheme2.RspSignatureS2;
import com.energyict.protocolimplv2.umi.types.Role;
import com.energyict.protocolimplv2.umi.types.UmiInitialisationVector;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AppPacketSecurityS2 extends AppPacketSecurityS0 {
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    public static final int UMI_GCM_TAG_LENGTH = 128;
    private UmiSessionPropertiesS2 sessionProperties;

    private static final Logger LOGGER = Logger.getLogger(AppPacketSecurityS2.class.getName());

    public AppPacketSecurityS2(UmiSessionPropertiesS2 sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    @Override
    public AppLayerEncryptedPacket encrypt(AppLayerPacket packet) {
        AppLayerEncryptedPacket encryptedPacket = null;
        packet.getAdditionalAuthData().setEncryptionScheme(sessionProperties.getEncryptionScheme());
        if (sessionProperties.getEncryptionScheme() == SecurityScheme.NO_SECURITY) {
            encryptedPacket = super.encrypt(packet);
        } else {
            UmiInitialisationVector ivReference = packet.getHeaderPayloadData().getType().isCmd() ?
                    sessionProperties.getOutboundIV() : sessionProperties.getInboundIV();

            long counter = Integer.toUnsignedLong(ivReference.getCounter()[0]);
            packet.getAdditionalAuthData().setEncryptionOptions((int) counter);
            LOGGER.finest("UmiInitialisationVector counter: " + ivReference.getCounterAsNumber() + ", byte: " + (byte) counter
                    + ", bitmask: " + Arrays.stream(ivReference.getCounter()).mapToObj(i -> Integer.toBinaryString(i)).collect(Collectors.toList()));

            // perform encryption
            SecretKey keySpec = new SecretKeySpec(sessionProperties.getSessionKey(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(UMI_GCM_TAG_LENGTH, ivReference.getRaw());
            try {
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
                cipher.updateAAD(packet.getAdditionalAuthData().getRaw());
                byte[] cipherText = cipher.doFinal(packet.getToBeEncrypted());
                encryptedPacket = new AppLayerEncryptedPacket(packet.getAdditionalAuthData(), cipherText);
                sessionProperties.setEstablished(ivReference.increment());
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Error encrypting : [" + e.getMessage() + "]", e);
            }
        }
        return encryptedPacket;
    }

    @Override
    public AppLayerPacket decrypt(AppLayerEncryptedPacket encryptedPacket) throws GeneralSecurityException {
        AppLayerPacket packet = null;
        if (encryptedPacket.getAdditionalAuthData().getEncryptionScheme() == SecurityScheme.NO_SECURITY) {
            packet = super.decrypt(encryptedPacket);
        } else {
            int encryptedOptions = encryptedPacket.getAdditionalAuthData().getEncryptionOptions();
            UmiInitialisationVector ivReference = null;
            if (sessionProperties.getOutboundIV().getCounterAsNumber() == sessionProperties.getInboundIV().getCounterAsNumber()
                    || (byte) Integer.toUnsignedLong(sessionProperties.getInboundIV().getCounter()[0]) == (byte) encryptedOptions) {
                ivReference = sessionProperties.getInboundIV(); // seems to be inbound
            } else if ((byte) Integer.toUnsignedLong(sessionProperties.getOutboundIV().getCounter()[0]) == (byte) encryptedOptions) {
                ivReference = sessionProperties.getOutboundIV();
            } else {
                throw new GeneralSecurityException("Incorrect encryption options in packet!");
            }
            SecretKeySpec keySpec = new SecretKeySpec(sessionProperties.getSessionKey(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(UMI_GCM_TAG_LENGTH, ivReference.getRaw());
            try {
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
                cipher.updateAAD(encryptedPacket.getAdditionalAuthData().getRaw());
                byte[] decryptedText = cipher.doFinal(encryptedPacket.getCipherText());
                packet = new AppLayerPacket(encryptedPacket.getAdditionalAuthData(), decryptedText);
                ivReference.increment();
            } catch (Exception e) {
                throw new GeneralSecurityException(e.getMessage());
            }
        }
        return packet;
    }

    @Override
    public void sign(AppLayerPacket packet) throws GeneralSecurityException {
        if (packet.getHeaderPayloadData().getSignatureScheme() == SecurityScheme.NO_SECURITY) {
            super.sign(packet);
        } else {
            AppPacketSignatureS2 signature = null;
            if (packet.getHeaderPayloadData().getType().isCmd()) {
                Role role = sessionProperties.getOwnCertificate().getRole();
                Date from = Calendar.getInstance().getTime();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, 30);
                Date until = calendar.getTime();

                signature = new CmdSignatureS2(role, from, until);
            } else {
                signature = new RspSignatureS2();
            }
            byte[] toBeSigned = getToBeSigned(packet, signature);

            try {
                Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
                signer.initSign(sessionProperties.getOwnPrivateKey());
                signer.update(toBeSigned);
                signature.setDigitalSignature(signer.sign());
                packet.setSignature(signature);
            } catch (Exception e) {
                throw new GeneralSecurityException(e);
            }
        }
    }

    @Override
    public boolean verifySignature(AppLayerPacket packet) {
        if (packet.getHeaderPayloadData().getSignatureScheme() == SecurityScheme.NO_SECURITY) {
            return super.verifySignature(packet);
        } else {
            AppPacketSignatureS2 signature = (AppPacketSignatureS2) packet.getSignature();
            byte[] toBeSigned = getToBeSigned(packet, signature);

            try {
                Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
                verifier.initVerify(sessionProperties.getRemoteCertificate().getPublicKey());
                verifier.update(toBeSigned);
                verifier.verify(signature.getDigitalSignature());
            } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
                return false;
            }
            return true;
        }
    }

    protected static byte[] getToBeSigned(AppLayerPacket packet, AppPacketSignatureS2 signature) {
        byte[] signatureToBeSigned = signature.getToBeSigned();
        byte[] packetToBeSigned = packet.getToBeSigned();

        byte[] toBeSigned = new byte[signatureToBeSigned.length + packetToBeSigned.length];
        System.arraycopy(packetToBeSigned, 0, toBeSigned, 0, packetToBeSigned.length);
        System.arraycopy(signatureToBeSigned, 0, toBeSigned, packetToBeSigned.length,
                signatureToBeSigned.length);
        return toBeSigned;
    }
}
