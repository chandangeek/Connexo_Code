package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;

import java.io.IOException;

public interface IUmiLinkSession {
    boolean isSessionEstablished();
    void sendUmiResync() throws IOException;
    byte[] sendGenericLink(AppLayerEncryptedPacket appLayerEncryptedPacketRaw) throws IOException;
}
