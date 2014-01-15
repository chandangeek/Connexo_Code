package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.BusinessObject;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:17
 */
public interface MessageAdapterMapping extends BusinessObject {

    public String getDeviceProtocolJavaClassName();

    public String getMessageAdapterJavaClassName();
}
