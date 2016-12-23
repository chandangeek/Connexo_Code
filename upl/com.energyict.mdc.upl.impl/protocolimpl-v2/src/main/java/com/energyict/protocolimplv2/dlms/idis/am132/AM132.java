package com.energyict.protocolimplv2.dlms.idis.am132;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;


import java.util.ArrayList;
import java.util.List;

/**
 * Protocol driver for AM130-v2 communication module.
 *   - this is the next generation of AM130 with LTE modem supporting IDIS pk1 + EVN secure modes
 *   - the origianal AM130 protocol was never used in the field: com.energyict.protocolimplv2.dlms.idis.am130.AM130
 *
 *   Class inheritance history:
 *    - com.energyict.protocolimplv2.dlms.idis.am500.AM500 = Old IDIS pk 1 protocol
 *    - com.energyict.protocolimplv2.dlms.idis.am130.AM130 = Extension with basic IDIS pk2 support (never actually used)
 *    - com.energyict.protocolimplv2.dlms.idis.am540.AM540 = Huge extensions of IDIS pk2 features + secure EVN support
 */

public class AM132 extends AM540 {

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
        supportedProtocolDialects.add(new SerialDeviceProtocolDialect());

        // support of TcpIp connection for normal GPRS connection
        supportedProtocolDialects.add(new TcpDeviceProtocolDialect());

        return supportedProtocolDialects;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> supportedConnectionTypes = new ArrayList<>();

        supportedConnectionTypes.add(new OutboundTcpIpConnectionType());
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
        return new AM132Properties();
    }


    @Override
    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new AM132ConfigurationSupport();
    }
}
