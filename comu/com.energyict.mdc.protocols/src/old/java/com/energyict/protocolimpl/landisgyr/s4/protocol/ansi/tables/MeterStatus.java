/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TableTemplate.java
 *
 * Created July 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class MeterStatus extends AbstractTable {

    private MeterMethodStatus meterMethodStatus;
    // RELAY_STATUS : RELAY_STATUS_BFLD;
    // SERVICE_STATUS : SERVICE_STATUS_BFLD;
    // ADDRESSABILITY_STATUS : ADDRESSABILITY_STATUS_BFLD;
    private DSPFirmwareRevision dspFirmwareRevision; //DSP_FIRMWARE_REVISION : DSP_FIRMWARE_REVISION_RCD;
    private int voltageCode; //VOLTAGE_CODE : UINT8;
    private int lineFrequency; //LINE_FREQUENCY : ARRAY[3] OF UINT8;



    /** Creates a new instance of TableTemplate */
    public MeterStatus(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(12,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterStatus:\n");
        strBuff.append("   dspFirmwareRevision="+getDspFirmwareRevision()+"\n");
        strBuff.append("   lineFrequency="+getLineFrequency()+"\n");
        strBuff.append("   meterMethodStatus="+getMeterMethodStatus()+"\n");
        strBuff.append("   voltageCode="+getVoltageCode()+"\n");
        return strBuff.toString();
    }

    public BigDecimal getVoltageMultiplier() throws IOException {
        return VoltageCode.findVoltageCode(getVoltageCode()).getMultiplier().multiply(BigDecimal.valueOf(3600/getTableFactory().getC12ProtocolLink().getProfileInterval()));
    }

    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int offset=0;
        setMeterMethodStatus(new MeterMethodStatus(tableData, offset, getManufacturerTableFactory()));
        offset+=MeterMethodStatus.getSize(getManufacturerTableFactory());

        offset+=3; // skip ov er 3 bytes;

        setDspFirmwareRevision(new DSPFirmwareRevision(tableData, offset, getManufacturerTableFactory()));
        offset+=DSPFirmwareRevision.getSize(getManufacturerTableFactory());
        setVoltageCode((int)tableData[offset++]&0xFF);
        setLineFrequency(C12ParseUtils.getInt(tableData, offset, 3, cfgt.getDataOrder()));
        offset+=3;

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public MeterMethodStatus getMeterMethodStatus() {
        return meterMethodStatus;
    }

    public void setMeterMethodStatus(MeterMethodStatus meterMethodStatus) {
        this.meterMethodStatus = meterMethodStatus;
    }

    public DSPFirmwareRevision getDspFirmwareRevision() {
        return dspFirmwareRevision;
    }

    public void setDspFirmwareRevision(DSPFirmwareRevision dspFirmwareRevision) {
        this.dspFirmwareRevision = dspFirmwareRevision;
    }

    public int getVoltageCode() {
        return voltageCode;
    }

    public void setVoltageCode(int voltageCode) {
        this.voltageCode = voltageCode;
    }

    public int getLineFrequency() {
        return lineFrequency;
    }

    public void setLineFrequency(int lineFrequency) {
        this.lineFrequency = lineFrequency;
    }


}
