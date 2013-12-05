/*
 * MeterFactors.java
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
import com.energyict.protocolimpl.ansi.c12.tables.ActualSourcesLimitingTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class MeterFactors extends AbstractTable {


    private BigDecimal kFactors; // K_FACTOR : ARRAY[3] OF BCD32;
    //IF ACT_SOURCES_LIM_TBL.THERMAL_DEMAND_FLAG THEN
    private BigDecimal demandKFactor; // DEMAND_K_FACTOR : ARRAY[3] OF UINT838
    //ELSE
    //ARRAY[3] OF BCD32;
//    END;
    private int scaleFactor; // SCALE_FACTOR : UINT8;
    private int demandOverlowdValue; // DEMAND_OVERLOAD_VALUE : UINT16;
    private int testModeKh; // TEST_MODE_KH : ARRAY[3] OF BCD32;
    private int testModeKFactor; // TEST_MODE_K_FACTOR : ARRAY[3] OF BCD32;

    /** Creates a new instance of TableTemplate */
    public MeterFactors(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(16,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterFactors:\n");
        strBuff.append("   KFactors="+getKFactors()+"\n");
        strBuff.append("   demandKFactor="+getDemandKFactor()+"\n");
        strBuff.append("   demandOverlowdValue="+getDemandOverlowdValue()+"\n");
        strBuff.append("   scaleFactor="+getScaleFactor()+"\n");
        strBuff.append("   testModeKFactor="+getTestModeKFactor()+"\n");
        strBuff.append("   testModeKh="+getTestModeKh()+"\n");
        return strBuff.toString();
    }


    public BigDecimal getEnergyMultiplier() {
        return getKFactors().movePointLeft(3); // kWh = pulseCount * (kf / 1000) S4 implementation guide page 174
    }

    public BigDecimal getDemandMultiplier() throws IOException {
        BigDecimal bd =  getKFactors().movePointLeft(3); // kW = pulseCount * (kf / 1000) * (intervals/hour) S4 implementation guide page 174
        bd = bd.multiply(BigDecimal.valueOf(3600/getTableFactory().getC12ProtocolLink().getProfileInterval()));
        return bd;
    }

    protected void parse(byte[] tableData) throws IOException {
        ConfigurationTable cfgt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualSourcesLimitingTable aslt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int offset=0;

        setKFactors(BigDecimal.valueOf(C12ParseUtils.getBCD2Long(tableData, offset, 3, cfgt.getDataOrder())));
        setKFactors(getKFactors().movePointLeft(3)); // see S4 C12 implementation page 113

        offset+=3;

        if (aslt.isThermalDemand()) {
            setDemandKFactor(Utils.getS4FloatingPoint(tableData, offset));
            offset+=3;
        }
        else {
            setDemandKFactor(BigDecimal.valueOf(C12ParseUtils.getBCD2Long(tableData, offset, 3, cfgt.getDataOrder())));
            setDemandKFactor(getDemandKFactor().movePointLeft(3)); // see S4 C12 implementation page 113
            offset+=3;
        }

        setScaleFactor((int)tableData[offset++] & 0xFF);
        setDemandOverlowdValue(C12ParseUtils.getInt(tableData,offset,2, cfgt.getDataOrder()));
        offset+=2;

        setTestModeKh((int)C12ParseUtils.getBCD2Long(tableData, offset, 3, cfgt.getDataOrder()));
        offset+=3;
        setTestModeKFactor((int)C12ParseUtils.getBCD2Long(tableData, offset, 3, cfgt.getDataOrder()));
        offset+=3;

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public BigDecimal getKFactors() {
        return kFactors;
    }

    public void setKFactors(BigDecimal kFactors) {
        this.kFactors = kFactors;
    }

    public BigDecimal getDemandKFactor() {
        return demandKFactor;
    }

    public void setDemandKFactor(BigDecimal demandKFactor) {
        this.demandKFactor = demandKFactor;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getDemandOverlowdValue() {
        return demandOverlowdValue;
    }

    public void setDemandOverlowdValue(int demandOverlowdValue) {
        this.demandOverlowdValue = demandOverlowdValue;
    }

    public int getTestModeKh() {
        return testModeKh;
    }

    public void setTestModeKh(int testModeKh) {
        this.testModeKh = testModeKh;
    }

    public int getTestModeKFactor() {
        return testModeKFactor;
    }

    public void setTestModeKFactor(int testModeKFactor) {
        this.testModeKFactor = testModeKFactor;
    }


}
