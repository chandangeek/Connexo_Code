package com.energyict.protocolimpl.base;

import com.energyict.protocol.CustomerVersion;

/**
 * @author Karel
 */
public class ProtocolVersionImpl implements CustomerVersion {

    /**
     * Creates a new instance of CustomerVersion
     */
    public ProtocolVersionImpl() {
    }

    public static void main(String[] args) {
        CustomerVersion version = new ProtocolVersionImpl();
        System.out.println(
                "Protocol version: " +
                        version.getCustomer() +
                        " , version: " +
                        version.getVersion()
        );
    }

    public String getVersion() {
        return getClass().getPackage().getSpecificationVersion();
    }

    public String getCustomer() {
        return getClass().getPackage().getSpecificationVendor();
    }
}