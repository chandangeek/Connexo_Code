package com.energyict.protocolimplv2.dlms.idis.am122;

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
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am122.messages.AM122Messaging;
import com.energyict.protocolimplv2.dlms.idis.am122.registers.AM122RegisterFactory;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;

import java.util.Arrays;
import java.util.List;

public class AM122 extends AM540 {

    protected AM122RegisterFactory registerFactory;

    public AM122(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM122Messaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected AM122RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM122RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList((ConnectionType) new SioOpticalConnectionType(this.getPropertySpecService()),
                new RxTxOpticalConnectionType(this.getPropertySpecService()),
                new OutboundTcpIpConnectionType(this.getPropertySpecService()),
                new InboundIpConnectionType() );
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM122 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2019-02-28 16:22:14 +0300 (Thu, 28 Feb 2019)$";
    }
}
