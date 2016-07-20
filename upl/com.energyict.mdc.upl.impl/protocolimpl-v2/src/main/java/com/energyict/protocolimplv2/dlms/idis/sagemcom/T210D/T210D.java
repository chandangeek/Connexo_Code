package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D;

import com.energyict.dlms.UniversalObject;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540Cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cisac on 6/27/2016.
 */
public class T210D extends AM130 {

    @Override
    public String getVersion() {
        return "$Date: 2016-07-20 10:41:02 +0300 (Wed, 20 Jul 2016)$";
    }
}
