/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 4 juli 2006, 9:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PhaseBitfield {

    private int phaseVoltagePhasorCode; // : UINT(0..3);
    private int phaseVoltageCode; // : UINT(4..7);

    /** Creates a new instance of RecordTemplate */
    public PhaseBitfield(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setPhaseVoltagePhasorCode(data[offset] & 0x0F);
        setPhaseVoltageCode((data[offset]>>4) & 0x0F);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PhaseBitfield:\n");
        strBuff.append("   phaseVoltageCode="+getPhaseVoltageCode()+"\n");
        strBuff.append("   phaseVoltagePhasorCode="+getPhaseVoltagePhasorCode()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public int getPhaseVoltagePhasorCode() {
        return phaseVoltagePhasorCode;
    }

    public void setPhaseVoltagePhasorCode(int phaseVoltagePhasorCode) {
        this.phaseVoltagePhasorCode = phaseVoltagePhasorCode;
    }

    public int getPhaseVoltageCode() {
        return phaseVoltageCode;
    }

    public void setPhaseVoltageCode(int phaseVoltageCode) {
        this.phaseVoltageCode = phaseVoltageCode;
    }

}
