package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Keeps a reference from a DeviceProtocol to a SecuritySupport object.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:28
 */
public interface SecuritySupportAdapterMapping {

    public String getSecuritySupportJavaClassName();

    public String getDeviceProtocolJavaClassName();

}