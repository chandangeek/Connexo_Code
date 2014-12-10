package com.energyict.protocols.mdc.protocoltasks;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocolimplv2.edp.EDPProperties;
import com.energyict.protocolimplv2.elster.garnet.SerialDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a DeviceProtocolDialect for a serial HDLC connection type (optical/RS485/... interface)
 * <p/>
 * Only difference is the default value for the server lower mac address: 16 instead of 0
 *
 * @author: khe
 * @since: 16/10/12 (113:25)
 */
public class EDPSerialDeviceProtocolDialect extends SerialDeviceProtocolDialect {

    public EDPSerialDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected PropertySpec serverLowerMacAddressPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(EDPProperties.SERVER_LOWER_MAC_ADDRESS, false, BigDecimal.valueOf(16));
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(serverLowerMacAddressPropertySpec());
        return propertySpecs;
    }
}