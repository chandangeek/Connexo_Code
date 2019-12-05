package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.math.BigDecimal;

public class EI7 extends A2 {

    public EI7(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor);
    }

    protected void setupSession(ComChannel comChannel, ObisCode frameCounterObiscode) {
        DlmsProperties publicClientProperties = getDlmsProperties();
        DlmsSession publicDlmsSession = getPublicDlmsSession(comChannel, publicClientProperties);
        long frameCounter;
        String logicalDeviceName;
        try {
            frameCounter = getFrameCounter(publicDlmsSession, frameCounterObiscode);
            logicalDeviceName = getLogicalDeviceName(publicDlmsSession);
            if (getHhuSignOnV2() != null) {
                checkDeviceName(logicalDeviceName);
            }
        } catch (DataAccessResultException | ProtocolException e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } finally {
            getLogger().info("Disconnecting public client");
            publicDlmsSession.disconnect();
        }
        getDlmsSessionProperties().setSerialNumber(logicalDeviceName);
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
        getDlmsSessionProperties().getProperties().setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(MANAGEMENT_CLIENT));
        if (getHhuSignOnV2() != null) {
            getHhuSignOnV2().setClientMacAddress(MANAGEMENT_CLIENT);
        }
        setDlmsSession(new EI7DlmsSession(comChannel, getDlmsSessionProperties(), getHhuSignOnV2(), offlineDevice.getSerialNumber()));
    }

    protected DlmsSession getPublicDlmsSession(ComChannel comChannel, DlmsProperties publicClientProperties) {
        DlmsSession publicDlmsSession = new EI7DlmsSession(comChannel, publicClientProperties, getHhuSignOnV2(), offlineDevice.getSerialNumber());
        getLogger().info("Connecting to public client:" + PUBLIC_CLIENT);
        connectWithRetries(publicDlmsSession);
        return publicDlmsSession;
    }
}
