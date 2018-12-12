package com.energyict.protocolimplv2.dlms.idis.am132;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
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

import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol driver for AM130-v2 communication module.
 * - this is the next generation of AM130 with LTE modem supporting IDIS pk1 + EVN secure modes
 * - the origianal AM130 protocol was never used in the field: com.energyict.protocolimplv2.dlms.idis.am130.AM130
 * <p/>
 * Class inheritance history:
 * - com.energyict.protocolimplv2.dlms.idis.am500.AM500 = Old IDIS pk 1 protocol
 * - com.energyict.protocolimplv2.dlms.idis.am130.AM130 = Extension with basic IDIS pk2 support (never actually used)
 * - com.energyict.protocolimplv2.dlms.idis.am540.AM540 = Huge extensions of IDIS pk2 features + secure EVN support
 */

public class AM132 extends AM540 {

    public AM132(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM130-v2 DLMS (IDIS P2)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-12-23$";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> supportedProtocolDialects = new ArrayList<>();

        // support of direct serial connection for optical probe
        supportedProtocolDialects.add(new SerialDeviceProtocolDialect(getPropertySpecService(), getNlsService()));

        // support of TcpIp connection for normal GPRS connection
        supportedProtocolDialects.add(new TcpDeviceProtocolDialect(getPropertySpecService(), getNlsService()));

        return supportedProtocolDialects;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> supportedConnectionTypes = new ArrayList<>();

        supportedConnectionTypes.add(new OutboundTcpIpConnectionType(getPropertySpecService()));
        supportedConnectionTypes.add(new InboundIpConnectionType());

        return supportedConnectionTypes;
    }


    @Override
    public AM132Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (AM132Properties) dlmsProperties;
    }

    @Override
    protected AM132Properties getNewInstanceOfProperties() {
        return new AM132Properties(getPropertySpecService(), getNlsService());
    }


    @Override
    protected HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new AM132ConfigurationSupport(getPropertySpecService());
    }
}
