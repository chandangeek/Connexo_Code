package com.energyict.protocolimplv2.dlms.idis.as3000g;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
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
import com.energyict.protocolimplv2.dlms.idis.as3000g.events.AS3000GLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.as3000g.messages.AS3000GMessaging;
import com.energyict.protocolimplv2.dlms.idis.as3000g.properties.AS3000GConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.as3000g.properties.AS3000GProperties;
import com.energyict.protocolimplv2.dlms.idis.as3000g.registers.AS3000GRegisterFactory;
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
        return Arrays.asList(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
    }

    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AS3000GConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    public AS3000GProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AS3000GProperties(this.getPropertySpecService(), getNlsService());
        }
        return (AS3000GProperties) dlmsProperties;
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell AS3000G DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-08-17 08:02:00 +0300 (Wed, 17 Aug 2022)$";
    }
}