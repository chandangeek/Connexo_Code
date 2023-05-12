package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.packet.AppLayerEncryptedPacket;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.io.IOException;

public interface IUmiLinkSession {

    boolean isSessionEstablished();

    void sendUmiResync() throws IOException;

    byte[] sendGenericLink(AppLayerEncryptedPacket appLayerEncryptedPacketRaw) throws IOException;

    void setDestinationId(UmiId destinationUmiId);

    UmiId getDestinationId();
}
