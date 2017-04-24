/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataIdentityTemplate.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MeterIDS extends AbstractDataDefinition {

    private String fullSerialNumber; // OctetString(24),
    private String fullMeterID; // OctetString(32),
    private String fullLoadResearchID; // OctetString(32),

    /** Creates a new instance of DataIdentityTemplate */
    public MeterIDS(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterIDS:\n");
        strBuff.append("   fullLoadResearchID="+getFullLoadResearchID()+"\n");
        strBuff.append("   fullMeterID="+getFullMeterID()+"\n");
        strBuff.append("   fullSerialNumber="+getFullSerialNumber()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0061; // 97 READ DLMS_METER_IDS
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setFullSerialNumber(new String(ProtocolUtils.getSubArray2(data, offset, 24)));
        offset+=24;
        setFullMeterID(new String(ProtocolUtils.getSubArray2(data, offset, 32)));
        offset+=32;
        setFullLoadResearchID(new String(ProtocolUtils.getSubArray2(data, offset, 32)));
        offset+=32;

    }

    public String getFullSerialNumber() {
        return fullSerialNumber;
    }

    public void setFullSerialNumber(String fullSerialNumber) {
        this.fullSerialNumber = fullSerialNumber;
    }

    public String getFullMeterID() {
        return fullMeterID;
    }

    public void setFullMeterID(String fullMeterID) {
        this.fullMeterID = fullMeterID;
    }

    public String getFullLoadResearchID() {
        return fullLoadResearchID;
    }

    public void setFullLoadResearchID(String fullLoadResearchID) {
        this.fullLoadResearchID = fullLoadResearchID;
    }
}
