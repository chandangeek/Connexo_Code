package com.energyict.protocolimplv2.dlms.idis.AS3000G;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.AS3000G.events.AS3000GLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.AS3000G.messages.AS3000GMessaging;
import com.energyict.protocolimplv2.dlms.idis.AS3000G.properties.AS3000GConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.AS3000G.properties.AS3000GProperties;
import com.energyict.protocolimplv2.dlms.idis.AS3000G.registers.AS3000GRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;

import java.util.Arrays;
import java.util.List;

public class AS3000G extends AM540 {
    protected AS3000GRegisterFactory registerFactory;

    public AS3000G(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AS3000GMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AS3000GLogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return idisLogBookFactory;
    }

    @Override
    protected AS3000GRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AS3000GRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(new SioOpticalConnectionType(this.getPropertySpecService()),
                new RxTxOpticalConnectionType(this.getPropertySpecService()),
                new OutboundTcpIpConnectionType(this.getPropertySpecService()),
                new InboundIpConnectionType() );
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AS3000GConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    public AS3000GProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AS3000GProperties) dlmsProperties;
    }

    @Override
    protected AS3000GProperties getNewInstanceOfProperties() {
        return new AS3000GProperties(this.getPropertySpecService(), getNlsService());
    }


    @Override
    public String getProtocolDescription() {
        return "Honeywell AS3000G DLMS";
    }

    @Override
    public String getVersion() {
        return "11.10.2021";
    }

}