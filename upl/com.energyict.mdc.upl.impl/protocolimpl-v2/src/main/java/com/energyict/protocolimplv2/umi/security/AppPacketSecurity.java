package com.energyict.protocolimplv2.umi.security;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;
import com.energyict.protocolimplv2.umi.packet.AppLayerPacket;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface AppPacketSecurity {
    AppLayerEncryptedPacket encrypt(AppLayerPacket packet);

    AppLayerPacket decrypt(AppLayerEncryptedPacket encryptedPacket) throws GeneralSecurityException;

    void sign(AppLayerPacket packet) throws ProtocolException, GeneralSecurityException;

    boolean verifySignature(AppLayerPacket packet) throws ProtocolException;
}
