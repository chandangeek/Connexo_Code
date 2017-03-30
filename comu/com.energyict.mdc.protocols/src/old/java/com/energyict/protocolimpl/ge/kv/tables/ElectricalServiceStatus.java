/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ElectricalServiceStatus.java
 *
 * Created on 14 november 2005, 12:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ElectricalServiceStatus extends AbstractTable {

    private int meterBase;
    private int maxClassAmps;
    private int elementVolts;
    private boolean autoDetectServiceInProgress;
    private int serviceInUse;
    private int dspCaseInUse;
    private int elementVoltsInUse;


    /** Creates a new instance of ElectricalServiceStatus */
    public ElectricalServiceStatus(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(87,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ElectricalServiceStatus: meterBase="+getMeterBase()+
                       ", maxClassAmps="+getMaxClassAmps()+
                       ", elementVolts="+getElementVolts()+
                       ", autoDetectServiceInProgress="+isAutoDetectServiceInProgress()+
                       ", serviceInUse="+getServiceInUse()+
                       ", dspCaseInUse="+getDspCaseInUse()+
                       ", elementVoltsInUse="+getElementVoltsInUse()+"\n");


        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setMeterBase(C12ParseUtils.getInt(tableData,offset++));
        setMaxClassAmps(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setElementVolts(C12ParseUtils.getInt(tableData,offset++));
        setAutoDetectServiceInProgress(C12ParseUtils.getInt(tableData,offset++) == 1);
        setServiceInUse(C12ParseUtils.getInt(tableData,offset++));
        setDspCaseInUse(C12ParseUtils.getInt(tableData,offset++));
        setElementVoltsInUse(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
    }

    public int getMeterBase() {
        return meterBase;
    }

    public void setMeterBase(int meterBase) {
        this.meterBase = meterBase;
    }

    public int getMaxClassAmps() {
        return maxClassAmps;
    }

    public void setMaxClassAmps(int maxClassAmps) {
        this.maxClassAmps = maxClassAmps;
    }

    public int getElementVolts() {
        return elementVolts;
    }

    public void setElementVolts(int elementVolts) {
        this.elementVolts = elementVolts;
    }

    public boolean isAutoDetectServiceInProgress() {
        return autoDetectServiceInProgress;
    }

    public void setAutoDetectServiceInProgress(boolean autoDetectServiceInProgress) {
        this.autoDetectServiceInProgress = autoDetectServiceInProgress;
    }

    public int getServiceInUse() {
        return serviceInUse;
    }

    public void setServiceInUse(int serviceInUse) {
        this.serviceInUse = serviceInUse;
    }

    public int getDspCaseInUse() {
        return dspCaseInUse;
    }

    public void setDspCaseInUse(int dspCaseInUse) {
        this.dspCaseInUse = dspCaseInUse;
    }

    public int getElementVoltsInUse() {
        return elementVoltsInUse;
    }

    public void setElementVoltsInUse(int elementVoltsInUse) {
        this.elementVoltsInUse = elementVoltsInUse;
    }

}
