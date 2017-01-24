/*
 * GEDeviceTable.java
 *
 * Created on 19 oktober 2005, 10:33
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
public class GEDeviceTable extends AbstractTable {

    private boolean touUpgrade;
    private boolean secondMeasureUpgrade;
    private boolean recordingUpgrade;

    // metermodes 0..2 and registerfunctions 0..4
    static public final int DEMAND_ONLY=0;
    static public final int DEMAND_LP=1;
    static public final int TOU=2;
    static public final int INTERNAL=3;
    static public final int[] registerFunctions={INTERNAL,INTERNAL,INTERNAL,INTERNAL,DEMAND_ONLY,DEMAND_LP,TOU,INTERNAL};

    private int meterType; // 1=KV96
    private int meterMode; // 0=demand, 1=demand/LP, 2 = tou
    private int registerFunction; // 0..7 see above
    private int installedOption1; // 0, 2 or 4
    private int installedOption2; // 0
    private int installedOption3; // 0
    private int installedOption4; // 0
    private int installedOption5; // 0
    private int installedOption6; // 0


    /** Creates a new instance of GEDeviceTable */
    public GEDeviceTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(0,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GEDeviceTable: \n");
        strBuff.append("    touUpgrade="+isTouUpgrade()+", secondMeasureUpgrade="+isSecondMeasureUpgrade()+", recordingUpgrade="+isRecordingUpgrade()+"\n");
        strBuff.append("    meterType="+getMeterType()+", meterMode="+getMeterMode()+", registerFunction="+getRegisterFunction()+", installedOption1="+getInstalledOption1()+", installedOption2="+getInstalledOption2()+", installedOption3="+getInstalledOption3()+", installedOption4="+getInstalledOption4()+", installedOption5="+getInstalledOption5()+", installedOption6="+getInstalledOption6()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int temp = C12ParseUtils.getInt(tableData,0);

        setTouUpgrade(((temp&0x01) == 0x01));
        setSecondMeasureUpgrade(((temp&0x02) == 0x02));
        setRecordingUpgrade(((temp&0x04) == 0x04));

        setMeterType(C12ParseUtils.getInt(tableData,1));
        setMeterMode(C12ParseUtils.getInt(tableData,2));
        setRegisterFunction(C12ParseUtils.getInt(tableData,3));
        setInstalledOption1(C12ParseUtils.getInt(tableData,4));
        setInstalledOption2(C12ParseUtils.getInt(tableData,5));
        setInstalledOption3(C12ParseUtils.getInt(tableData,6));
        setInstalledOption4(C12ParseUtils.getInt(tableData,7));
        setInstalledOption5(C12ParseUtils.getInt(tableData,8));
        setInstalledOption6(C12ParseUtils.getInt(tableData,9));


    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public boolean isTouUpgrade() {
        return touUpgrade;
    }

    public void setTouUpgrade(boolean touUpgrade) {
        this.touUpgrade = touUpgrade;
    }

    public boolean isSecondMeasureUpgrade() {
        return secondMeasureUpgrade;
    }

    public void setSecondMeasureUpgrade(boolean secondMeasureUpgrade) {
        this.secondMeasureUpgrade = secondMeasureUpgrade;
    }

    public boolean isRecordingUpgrade() {
        return recordingUpgrade;
    }

    public void setRecordingUpgrade(boolean recordingUpgrade) {
        this.recordingUpgrade = recordingUpgrade;
    }

    public int getMeterType() {
        return meterType;
    }

    public void setMeterType(int meterType) {
        this.meterType = meterType;
    }

    public int getMeterMode() {
        return meterMode;
    }

    public void setMeterMode(int meterMode) {
        this.meterMode = meterMode;
    }

    public int getRegisterFunction() {
        return registerFunction;
    }

    public void setRegisterFunction(int registerFunction) {
        this.registerFunction = registerFunction;
    }

    public int getInstalledOption1() {
        return installedOption1;
    }

    public void setInstalledOption1(int installedOption1) {
        this.installedOption1 = installedOption1;
    }

    public int getInstalledOption2() {
        return installedOption2;
    }

    public void setInstalledOption2(int installedOption2) {
        this.installedOption2 = installedOption2;
    }

    public int getInstalledOption3() {
        return installedOption3;
    }

    public void setInstalledOption3(int installedOption3) {
        this.installedOption3 = installedOption3;
    }

    public int getInstalledOption4() {
        return installedOption4;
    }

    public void setInstalledOption4(int installedOption4) {
        this.installedOption4 = installedOption4;
    }

    public int getInstalledOption5() {
        return installedOption5;
    }

    public void setInstalledOption5(int installedOption5) {
        this.installedOption5 = installedOption5;
    }

    public int getInstalledOption6() {
        return installedOption6;
    }

    public void setInstalledOption6(int installedOption6) {
        this.installedOption6 = installedOption6;
    }

}
