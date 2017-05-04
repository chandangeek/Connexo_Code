/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject71.java
 *
 * Created on 12 april 2006, 14:57
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;


/** @author fbo */

public class InformationObject47 extends InformationObject {
    
    private int month;
    private int year;
    private int manufacturerCode;
    private String productCode;
    
    /** Creates a new instance of InformationObject71 */
    public InformationObject47() { }


    String getProductCode() {
        return productCode;
    }

    void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    int getMonth() {
        return month;
    }

    void setMonth(int month) {
        this.month = month;
    }

    int getYear() {
        return year;
    }

    void setYear(int year) {
        this.year = year;
    }

    int getManufacturerCode() {
        return manufacturerCode;
    }

    void setManufacturerCode(int manufacturerCode) {
        this.manufacturerCode = manufacturerCode;
    }
    
}
