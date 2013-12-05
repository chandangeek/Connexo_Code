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
public class TestModeParam {

    private int testModeDemandInterval; // : UINT8;
    private int testModeTimeOut; // : UINT8;

    /** Creates a new instance of RecordTemplate */
    public TestModeParam(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setTestModeDemandInterval((int)data[offset++]&0xff);
        setTestModeTimeOut((int)data[offset++]&0xff);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TestModeParam:\n");
        strBuff.append("   testModeDemandInterval="+getTestModeDemandInterval()+"\n");
        strBuff.append("   testModeTimeOut="+getTestModeTimeOut()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 2;
    }

    public int getTestModeDemandInterval() {
        return testModeDemandInterval;
    }

    private void setTestModeDemandInterval(int testModeDemandInterval) {
        this.testModeDemandInterval = testModeDemandInterval;
    }

    public int getTestModeTimeOut() {
        return testModeTimeOut;
    }

    private void setTestModeTimeOut(int testModeTimeOut) {
        this.testModeTimeOut = testModeTimeOut;
    }

}
