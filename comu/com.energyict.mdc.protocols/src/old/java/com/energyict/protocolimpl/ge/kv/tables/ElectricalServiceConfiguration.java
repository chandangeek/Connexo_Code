/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ElectricalServiceConfiguration.java
 *
 * Created on 14 november 2005, 12:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ElectricalServiceConfiguration extends AbstractTable {

    private boolean autoDetectServiceOverrideFlag;
    private int serviceOverride;
    private int defaultDSPCase;
    private String ansiForm;

    /** Creates a new instance of ElectricalServiceConfiguration */
    public ElectricalServiceConfiguration(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(86,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ElectricalServiceConfiguration: autoDetectServiceOverrideFlag="+isAutoDetectServiceOverrideFlag()+", serviceOverride="+getServiceOverride()+", defaultDSPCase="+getDefaultDSPCase()+", ansiForm="+getAnsiForm()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        setAutoDetectServiceOverrideFlag(C12ParseUtils.getInt(tableData,offset++)==1);
        setServiceOverride(C12ParseUtils.getInt(tableData,offset++));
        setDefaultDSPCase(C12ParseUtils.getInt(tableData,offset++));
        setAnsiForm(new String(ProtocolUtils.getSubArray2(tableData,offset,5)));
        offset+=5;
    }

    public boolean isAutoDetectServiceOverrideFlag() {
        return autoDetectServiceOverrideFlag;
    }

    public void setAutoDetectServiceOverrideFlag(boolean autoDetectServiceOverrideFlag) {
        this.autoDetectServiceOverrideFlag = autoDetectServiceOverrideFlag;
    }

    public int getServiceOverride() {
        return serviceOverride;
    }

    public void setServiceOverride(int serviceOverride) {
        this.serviceOverride = serviceOverride;
    }

    public int getDefaultDSPCase() {
        return defaultDSPCase;
    }

    public void setDefaultDSPCase(int defaultDSPCase) {
        this.defaultDSPCase = defaultDSPCase;
    }

    public String getAnsiForm() {
        return ansiForm;
    }

    public void setAnsiForm(String ansiForm) {
        this.ansiForm = ansiForm;
    }

}
