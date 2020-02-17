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
import com.energyict.protocolimplv2.dlms.a2.A2DlmsSession;
import com.energyict.protocolimplv2.dlms.ei7.messages.EI7Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.io.IOException;
import java.math.BigDecimal;

public class EI7 extends A2 {

    private EI7Messaging messaging = null;

    public EI7(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor);
    }

    public EI7DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsSessionProperties) {
        return new EI7DlmsSession(comChannel, dlmsSessionProperties, getHhuSignOnV2(), offlineDevice.getSerialNumber());
    }

    protected EI7Messaging getProtocolMessaging() {
        if (messaging == null) {
            messaging = new EI7Messaging(this, getPropertySpecService(), getNlsService(), getConverter(), getMessageFileExtractor());
        }
        return messaging;
    }

    @Override
    public String getProtocolDescription() {
        return "EI7 ThemisUno DLMS Protocol";
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-01-15 12:00:00 +0200 (Wen, 15 Ian 2020) $";
    }
}
