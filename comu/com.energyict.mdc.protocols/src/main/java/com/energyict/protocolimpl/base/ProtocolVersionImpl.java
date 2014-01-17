package com.energyict.protocolimpl.base;

/**
 *
 * @author  Karel
 */
public class ProtocolVersionImpl {

    public String getVersion() {
        return getClass().getPackage().getSpecificationVersion();
    }

    public String getCustomer() {
        return getClass().getPackage().getSpecificationVendor();
    }

}