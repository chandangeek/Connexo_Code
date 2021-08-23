package com.energyict.protocolimplv2.umi.security.scheme0;

import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;
import com.energyict.protocolimplv2.umi.packet.AppLayerPacket;
import com.energyict.protocolimplv2.umi.security.AppPacketSecurity;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.signature.AppPacketSignatureS0;

import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class AppPacketSecurityS0 implements AppPacketSecurity {
    @Override
    public AppLayerEncryptedPacket encrypt(AppLayerPacket packet) {
        byte encryptionOptions = 0;
        packet.getAdditionalAuthData()
                .setEncryptionScheme(SecurityScheme.NO_SECURITY)
                .setEncryptionOptions(encryptionOptions);

        byte[] rawPacket = packet.getRaw();
        byte[] ciphertext = Arrays.copyOfRange(rawPacket, packet.getAdditionalAuthData().getLength(), rawPacket.length);

        return new AppLayerEncryptedPacket(packet.getAdditionalAuthData(), ciphertext);
    }

    @Override
    public AppLayerPacket decrypt(AppLayerEncryptedPacket encryptedPacket) throws GeneralSecurityException {
        if (encryptedPacket.getAdditionalAuthData().getEncryptionScheme() != SecurityScheme.NO_SECURITY)
            throw new InvalidParameterException("authenticatedData.encryptionScheme != SecurityScheme.NO_SECURITY");
        return new AppLayerPacket(encryptedPacket.getAdditionalAuthData(), encryptedPacket.getCipherText());
    }

    @Override
    public void sign(AppLayerPacket packet) throws GeneralSecurityException {
        if (packet.getHeaderPayloadData().getSignatureScheme() != SecurityScheme.NO_SECURITY)
            throw new InvalidParameterException("signatureScheme != SecurityScheme.NO_SECURITY");
        packet.setSignature(new AppPacketSignatureS0());
    }

    @Override
    public boolean verifySignature(AppLayerPacket packet) {
        return packet.getHeaderPayloadData().getSignatureScheme() == SecurityScheme.NO_SECURITY &&
                packet.getSignature().equals(new AppPacketSignatureS0());
    }
}
