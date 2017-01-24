/*
 * EndDeviceMode1ndStatusTable.java
 *
 * Created on 18 oktober 2005, 17:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class EndDeviceModeAndStatusTable extends AbstractTable {

    // end device mode bitfield 1 byte
    private int edMode;
    private boolean meteringFlag;
    private boolean testModeFlag;
    private boolean meterShopModeFlag;
    private EndDeviceStdStatus1Bitfield endDeviceStdStatus1Bitfield;
    private EndDeviceStdStatus2Bitfield endDeviceStdStatus2Bitfield;

    private byte[] endDeviceManufacturerStatus;


    /** Creates a new instance of EndDeviceMode1ndStatusTable */
    public EndDeviceModeAndStatusTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(3));
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EndDeviceModeAndStatusTable:\n");
        strBuff.append("   edMode="+getEdMode()+"\n");
        strBuff.append("   endDeviceStdStatus1Bitfield="+getEndDeviceStdStatus1Bitfield()+"\n");
        strBuff.append("   endDeviceStdStatus2Bitfield="+getEndDeviceStdStatus2Bitfield()+"\n");
        strBuff.append("   endDeviceManufacturerStatus="+getEndDeviceManufacturerStatus()+"\n");
        strBuff.append("   meterShopModeFlag="+isMeterShopModeFlag()+"\n");
        strBuff.append("   meteringFlag="+isMeteringFlag()+"\n");
        strBuff.append("   testModeFlag="+isTestModeFlag()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] tableData) throws IOException {
        // end device mode bitfield 1 byte
        int offset=0;
        setEdMode(C12ParseUtils.getInt(tableData,offset++));
        setMeteringFlag((getEdMode() & 0x01) == 0x01);
        setTestModeFlag((getEdMode() & 0x02) == 0x02);
        setMeterShopModeFlag((getEdMode() & 0x04) == 0x04);

        endDeviceStdStatus1Bitfield = new EndDeviceStdStatus1Bitfield(tableData,offset,getTableFactory());
        offset+=EndDeviceStdStatus1Bitfield.getSize(getTableFactory());
        endDeviceStdStatus2Bitfield = new EndDeviceStdStatus2Bitfield(tableData,offset,getTableFactory());
        offset+=EndDeviceStdStatus2Bitfield.getSize(getTableFactory());
        setEndDeviceManufacturerStatus(ProtocolUtils.getSubArray2(tableData, offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDimMfgStatusUsed()));
        offset+=getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDimMfgStatusUsed();
    }


    public boolean isMeteringFlag() {
        return meteringFlag;
    }

    public void setMeteringFlag(boolean meteringFlag) {
        this.meteringFlag = meteringFlag;
    }

    public boolean isTestModeFlag() {
        return testModeFlag;
    }

    public void setTestModeFlag(boolean testModeFlag) {
        this.testModeFlag = testModeFlag;
    }

    public boolean isMeterShopModeFlag() {
        return meterShopModeFlag;
    }

    public void setMeterShopModeFlag(boolean meterShopModeFlag) {
        this.meterShopModeFlag = meterShopModeFlag;
    }


    public byte[] getEndDeviceManufacturerStatus() {
        return endDeviceManufacturerStatus;
    }

    public void setEndDeviceManufacturerStatus(byte[] endDeviceManufacturerStatus) {
        this.endDeviceManufacturerStatus = endDeviceManufacturerStatus;
    }

    public int getEdMode() {
        return edMode;
    }

    public void setEdMode(int edMode) {
        this.edMode = edMode;
    }

    public EndDeviceStdStatus1Bitfield getEndDeviceStdStatus1Bitfield() {
        return endDeviceStdStatus1Bitfield;
    }

    public EndDeviceStdStatus2Bitfield getEndDeviceStdStatus2Bitfield() {
        return endDeviceStdStatus2Bitfield;
    }
}
