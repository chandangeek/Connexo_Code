/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;


class InformationObjectC0 extends InformationObject {

    private Quantity activeImport;
    private Quantity activeExport;
    private Quantity q1;
    private Quantity q2;
    private Quantity q3;
    private Quantity q4;
    private Date timeStamp;

    public InformationObjectC0(TimeZone timeZone, ByteArray byteArray){
        activeImport = new Quantity(new BigDecimal(byteArray.bitValue( 0, 30 )), Unit.get("kWh") );
        activeExport = new Quantity(new BigDecimal(byteArray.bitValue( 32, 62 )), Unit.get("kWh") );
        q1 = new Quantity(new BigDecimal(byteArray.bitValue( 64, 94 )), Unit.get("varh") );
        q2 = new Quantity(new BigDecimal(byteArray.bitValue( 96, 126 )), Unit.get("varh") );
        q3 = new Quantity(new BigDecimal(byteArray.bitValue( 128, 158 )), Unit.get("varh") );
        q4 = new Quantity(new BigDecimal(byteArray.bitValue( 160, 190 )), Unit.get("varh") );
        timeStamp = new CP40Time(TimeZone.getDefault(), byteArray.sub( 24,5 )).getDate();
    }

    public Quantity getActiveImport() {
        return activeImport;
    }

    public Quantity getActiveExport() {
        return activeExport;
    }

    public Quantity getQ1() {
        return q1;
    }

    public Quantity getQ2() {
        return q2;
    }

    public Quantity getQ3() {
        return q3;
    }

    public Quantity getQ4() {
        return q4;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public String toString(){
        return "InformationObject0xC0 ["
                + activeImport + ", "
                + activeExport + ", "
                + q1 + ", "
                + q2 + ", "
                + q3 + ", "
                + q4 + ", "
                + timeStamp
                + "]";
    }

}