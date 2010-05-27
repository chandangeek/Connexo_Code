package com.energyict.protocolimpl.base;

import com.energyict.mdw.core.CustomerVersion;

/**
 *
 * @author  Karel
 */
public class ProtocolVersionImpl implements CustomerVersion {
    
    /** Creates a new instance of CustomerVersion */
    public ProtocolVersionImpl()  {
    }
    
    public String getVersion() {
        return getClass().getPackage().getSpecificationVersion();
    }
    
    public String getCustomer() {
        return getClass().getPackage().getSpecificationVendor();
    }
    
    public static void main(String[] args) {
        CustomerVersion version = new ProtocolVersionImpl();
        System.out.println(
            "Protocol version: "  + 
            version.getCustomer() +
            " , version: " +
            version.getVersion()
            );
    }
    
}
