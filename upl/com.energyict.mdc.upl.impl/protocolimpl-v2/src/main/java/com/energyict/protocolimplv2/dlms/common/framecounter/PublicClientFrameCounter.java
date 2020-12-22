package com.energyict.protocolimplv2.dlms.common.framecounter;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;

import java.io.IOException;
import java.util.Optional;

public class PublicClientFrameCounter implements FrameCounter {

    private final ObisCode obisCode;
    private final PublicClientDlmsSessionProvider publicClientDlmsSessionProvider;

    public PublicClientFrameCounter(ObisCode obisCode, PublicClientDlmsSessionProvider publicClientDlmsSessionProvider) {
        this.obisCode = obisCode;
        this.publicClientDlmsSessionProvider = publicClientDlmsSessionProvider;
    }

    @Override
    public Optional<Long> get() {
        DlmsSession publicDlmsSession = publicClientDlmsSessionProvider.provide();
        publicDlmsSession.connect();
        try {
            long frameCounter = publicDlmsSession.getCosemObjectFactory().getData(obisCode).getValueAttr().longValue();
            return Optional.of(frameCounter);
        } catch (IOException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } finally {
            publicDlmsSession.disconnect();
        }
    }
}
