package com.energyict.protocols.mdc.protocoltasks;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.elster.garnet.SerialDeviceProtocolDialect;

import java.math.BigDecimal;

/**
 * Models a {@link DeviceProtocolDialect} for a serial HDLC connection type (optical/RS485/... interface)
 * <p/>
 * Only difference is the default value for the server lower mac address: 16 instead of 0
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class EDPSerialDeviceProtocolDialect extends SerialDeviceProtocolDialect {

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, BigDecimal.valueOf(16));
    }
}