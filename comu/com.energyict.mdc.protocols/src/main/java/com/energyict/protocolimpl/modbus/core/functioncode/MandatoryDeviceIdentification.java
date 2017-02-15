/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MandatoryDeviceIdentification.java
 *
 * Created on 2 april 2007, 15:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

import java.util.List;

/**
 *
 * @author Koen
 */
public class MandatoryDeviceIdentification {

    private String vendorName;
    private String productCode;
    private String majorMinorRevision;

    /** Creates a new instance of MandatoryDeviceIdentification */
    public MandatoryDeviceIdentification(List deviceObjects) {
        setVendorName(((DeviceObject)deviceObjects.get(0)).getStr());
        setProductCode(((DeviceObject)deviceObjects.get(1)).getStr());
        setMajorMinorRevision(((DeviceObject)deviceObjects.get(2)).getStr());

    }

    public String toString() {
        return "vendorName="+getVendorName()+", productCode="+getProductCode()+", majorMinorRevision="+getMajorMinorRevision()+"\n";
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

    public String getMajorMinorRevision() {
        return majorMinorRevision;
    }

    public void setMajorMinorRevision(String majorMinorRevision) {
        this.majorMinorRevision = majorMinorRevision;
    }
}
