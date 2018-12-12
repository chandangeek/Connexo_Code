/*
 * Device.java
 *
 * Created on 2 april 2007, 15:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

/**
 *
 * @author Koen
 */
public class Device {
    
    private String vendorName;
    private String productCode;
    
    /** Creates a new instance of Device */
    public Device(String vendorName,String productCode) {
        this.setVendorName(vendorName);
        this.setProductCode(productCode);
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
    
}
