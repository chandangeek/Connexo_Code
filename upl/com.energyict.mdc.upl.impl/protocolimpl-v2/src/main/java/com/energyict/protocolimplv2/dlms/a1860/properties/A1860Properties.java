package com.energyict.protocolimplv2.dlms.a1860.properties;

import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;
import static com.energyict.dlms.common.DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS;
import static com.energyict.protocolimplv2.dlms.a1860.properties.A1860ConfigurationSupport.READCACHE_PROPERTY;

public class A1860Properties extends DlmsProperties {

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler = null;

    @Override
    public int getServerLowerMacAddress() {
        return parseBigDecimalProperty(SERVER_LOWER_MAC_ADDRESS, A1860ConfigurationSupport.DEFAULT_SERVER_LOWER_MAC_ADDRESS);
    }

    @Override
    public int getAddressingMode() {
        return parseBigDecimalProperty(ADDRESSING_MODE, new BigDecimal(4));
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        if (invokeIdAndPriorityHandler == null) {
            invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        }
        return invokeIdAndPriorityHandler;
    }

    @Override
    public int getInformationFieldSize() {
        return 0x7EE;
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new LocalSecurityProvider(getProperties());
    }

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

}