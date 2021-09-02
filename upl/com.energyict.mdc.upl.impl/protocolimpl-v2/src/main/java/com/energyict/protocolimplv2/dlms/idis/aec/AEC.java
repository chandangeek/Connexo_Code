package com.energyict.protocolimplv2.dlms.idis.aec;

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
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.aec.messages.AECMessaging;
import com.energyict.protocolimplv2.dlms.idis.aec.registers.AECRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;

import java.util.Arrays;
import java.util.List;

public class AEC extends AM540 {

    protected AECRegisterFactory registerFactory;

    public AEC(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AECMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.getNlsService(), this.getConverter(), this.getCalendarExtractor(), this.getMessageFileExtractor(), this.getKeyAccessorTypeExtractor());
        }
        return idisMessaging;
    }

    @Override
    protected AECRegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AECRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
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
    //TODO
    public String getProtocolDescription() {
        return "AEC DLMS (IDIS P2)";
    }

    @Override
    // TODO
    public String getVersion() {
        return "$Date: 2021-09-31$";
    }
}
