/*
 * LoadProfileControlTable.java
 *
 * Created on 7 november 2005, 17:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.base.FirmwareVersion;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileControlTable extends AbstractTable {

    /*
     * intervalFormatCodex 1=uint8, 2=uint16, 4=uint32, 8=int8, 16=int16, 32=int32, 64=non integer format 1, 128 = non integer format 2
     * scalarsSetx applied to the interval data before storing in the load profile table
     * divisorSetx applied to the interval data before storing in the load profile table
     */

    private LoadProfileSourceSelection[] loadProfileSelectionSet1;
    private int intervalFormatCode1; // 8 bit
    private int[] scalarsSet1; // 16 bit
    private int[] divisorSet1;

    private LoadProfileSourceSelection[] loadProfileSelectionSet2;
    private int intervalFormatCode2; // 8 bit
    private int[] scalarsSet2; // 16 bit
    private int[] divisorSet2;

    private LoadProfileSourceSelection[] loadProfileSelectionSet3;
    private int intervalFormatCode3; // 8 bit
    private int[] scalarsSet3; // 16 bit
    private int[] divisorSet3;

    private LoadProfileSourceSelection[] loadProfileSelectionSet4;
    private int intervalFormatCode4; // 8 bit
    private int[] scalarsSet4; // 16 bit
    private int[] divisorSet4;

    /** Creates a new instance of LoadProfileControlTable */
    public LoadProfileControlTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(62));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileControlTable: \n");

        // set1
        for (int t=0;t<getLoadProfileSelectionSet1().length;t++)
            strBuff.append("    loadProfileSelectionSet1["+t+"]="+getLoadProfileSelectionSet1()[t]+"\n");
        strBuff.append("    intervalFormatCode1="+getIntervalFormatCode1()+"\n");
        for (int t=0;t<getScalarsSet1().length;t++)
            strBuff.append("    scalarsSet1["+t+"]="+getScalarsSet1()[t]+"\n");
        for (int t=0;t<getDivisorSet1().length;t++)
            strBuff.append("    divisorSet1["+t+"]="+getDivisorSet1()[t]+"\n");

        // set2
        for (int t=0;t<getLoadProfileSelectionSet2().length;t++)
            strBuff.append("    loadProfileSelectionSet2["+t+"]="+getLoadProfileSelectionSet2()[t]+"\n");
        strBuff.append("    intervalFormatCode2="+getIntervalFormatCode2()+"\n");
        for (int t=0;t<getScalarsSet2().length;t++)
            strBuff.append("    scalarsSet2["+t+"]="+getScalarsSet2()[t]+"\n");
        for (int t=0;t<getDivisorSet2().length;t++)
            strBuff.append("    divisorSet2["+t+"]="+getDivisorSet2()[t]+"\n");

        // set3
        for (int t=0;t<getLoadProfileSelectionSet3().length;t++)
            strBuff.append("    loadProfileSelectionSet3["+t+"]="+getLoadProfileSelectionSet3()[t]+"\n");
        strBuff.append("    intervalFormatCode3="+getIntervalFormatCode3()+"\n");
        for (int t=0;t<getScalarsSet3().length;t++)
            strBuff.append("    scalarsSet3["+t+"]="+getScalarsSet3()[t]+"\n");
        for (int t=0;t<getDivisorSet3().length;t++)
            strBuff.append("    divisorSet3["+t+"]="+getDivisorSet3()[t]+"\n");

        // set4
        for (int t=0;t<getLoadProfileSelectionSet4().length;t++)
            strBuff.append("    loadProfileSelectionSet4["+t+"]="+getLoadProfileSelectionSet4()[t]+"\n");
        strBuff.append("    intervalFormatCode4="+getIntervalFormatCode4()+"\n");
        for (int t=0;t<getScalarsSet4().length;t++)
            strBuff.append("    scalarsSet4["+t+"]="+getScalarsSet4()[t]+"\n");
        for (int t=0;t<getDivisorSet4().length;t++)
            strBuff.append("    divisorSet4["+t+"]="+getDivisorSet4()[t]+"\n");

        return strBuff.toString();
    }


    private int checkVersionForFormatCode(int formatCode) throws IOException {
        // overrule for GEKV
        if (getTableFactory().getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
            FirmwareVersion fw = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getManufacturerIdentificationTable().getFirmwareVersion();
            FirmwareVersion fw2CheckAgainst = new FirmwareVersion("5.2");
            if (fw.equal(fw2CheckAgainst) || fw.before(fw2CheckAgainst)) {
                return IntervalFormat.INT16;
            }
        }

        return formatCode;
    }

    protected void parse(byte[] tableData) throws IOException {
        //ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();


        int offset=0;
        setLoadProfileSelectionSet1(new LoadProfileSourceSelection[alpt.getLoadProfileSet().getNrOfChannelsSet()[0]]);
        setScalarsSet1(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[0]]);
        setDivisorSet1(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[0]]);
        setLoadProfileSelectionSet2(new LoadProfileSourceSelection[alpt.getLoadProfileSet().getNrOfChannelsSet()[1]]);
        setScalarsSet2(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[1]]);
        setDivisorSet2(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[1]]);
        setLoadProfileSelectionSet3(new LoadProfileSourceSelection[alpt.getLoadProfileSet().getNrOfChannelsSet()[2]]);
        setScalarsSet3(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[2]]);
        setDivisorSet3(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[2]]);
        setLoadProfileSelectionSet4(new LoadProfileSourceSelection[alpt.getLoadProfileSet().getNrOfChannelsSet()[3]]);
        setScalarsSet4(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[3]]);
        setDivisorSet4(new int[alpt.getLoadProfileSet().getNrOfChannelsSet()[3]]);
        // set1
        if ((cfgt.getStdTablesUsed()[8]&0x01)==0x01) {
            for (int t=0;t<getLoadProfileSelectionSet1().length;t++) {
                getLoadProfileSelectionSet1()[t]=new LoadProfileSourceSelection(tableData, offset, getTableFactory());
                offset+=getLoadProfileSelectionSet1()[t].getSize(getTableFactory());
            } // for (int t=0;t<loadProfileSelectionSet1[i].length;t++) {
            setIntervalFormatCode1(checkVersionForFormatCode(C12ParseUtils.getInt(tableData,offset)));
            offset++;
            if (alpt.getLoadProfileSet().isScalarDivisorFlagSet1()) {
                for (int t=0;t<getScalarsSet1().length;t++) {
                    getScalarsSet1()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
                for (int t=0;t<getDivisorSet1().length;t++) {
                    getDivisorSet1()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
            }
        } // if ((cfgt.getStdTablesUsed()[8]&0x01)==0x01)

        // set2
        if ((cfgt.getStdTablesUsed()[8]&0x02)==0x02) {
            for (int t=0;t<getLoadProfileSelectionSet2().length;t++) {
                getLoadProfileSelectionSet2()[t]=new LoadProfileSourceSelection(tableData, offset, getTableFactory());
                offset+=getLoadProfileSelectionSet2()[t].getSize(getTableFactory());
            } // for (int t=0;t<loadProfileSelectionSet2[i].length;t++) {
            setIntervalFormatCode2(checkVersionForFormatCode(C12ParseUtils.getInt(tableData,offset)));
            offset++;
            if (alpt.getLoadProfileSet().isScalarDivisorFlagSet2()) {
                for (int t=0;t<getScalarsSet2().length;t++) {
                    getScalarsSet2()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
                for (int t=0;t<getDivisorSet2().length;t++) {
                    getDivisorSet2()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
            }
        } // if ((cfgt.getStdTablesUsed()[8]&0x02)==0x02)

        // set3
        if ((cfgt.getStdTablesUsed()[8]&0x04)==0x04) {
            for (int t=0;t<getLoadProfileSelectionSet3().length;t++) {
                getLoadProfileSelectionSet3()[t]=new LoadProfileSourceSelection(tableData, offset, getTableFactory());
                offset+=getLoadProfileSelectionSet3()[t].getSize(getTableFactory());
            } // for (int t=0;t<loadProfileSelectionSet3[i].length;t++) {
            setIntervalFormatCode3(checkVersionForFormatCode(C12ParseUtils.getInt(tableData,offset)));
            offset++;
            if (alpt.getLoadProfileSet().isScalarDivisorFlagSet3()) {
                for (int t=0;t<getScalarsSet3().length;t++) {
                    getScalarsSet3()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
                for (int t=0;t<getDivisorSet3().length;t++) {
                    getDivisorSet3()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
            }
        } // if ((cfgt.getStdTablesUsed()[8]&0x04)==0x04)

        // set4
        if ((cfgt.getStdTablesUsed()[8]&0x08)==0x08) {
            for (int t=0;t<getLoadProfileSelectionSet4().length;t++) {
                getLoadProfileSelectionSet4()[t]=new LoadProfileSourceSelection(tableData, offset, getTableFactory());
                offset+=getLoadProfileSelectionSet4()[t].getSize(getTableFactory());
            } // for (int t=0;t<loadProfileSelectionSet4[i].length;t++) {
            setIntervalFormatCode4(checkVersionForFormatCode(C12ParseUtils.getInt(tableData,offset)));
            offset++;
            if (alpt.getLoadProfileSet().isScalarDivisorFlagSet4()) {
                for (int t=0;t<getScalarsSet4().length;t++) {
                    getScalarsSet4()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
                for (int t=0;t<getDivisorSet4().length;t++) {
                    getDivisorSet4()[t]=C12ParseUtils.getInt(tableData, offset, 2,dataOrder);
                    offset+=2;
                }
            }
        } // if ((cfgt.getStdTablesUsed()[8]&0x08)==0x08)

    }

    public LoadProfileSourceSelection[] getLoadProfileSelectionSet1() {
        return loadProfileSelectionSet1;
    }

    public void setLoadProfileSelectionSet1(LoadProfileSourceSelection[] loadProfileSelectionSet1) {
        this.loadProfileSelectionSet1 = loadProfileSelectionSet1;
    }

    public int getIntervalFormatCode1() {
        return intervalFormatCode1;
    }

    public void setIntervalFormatCode1(int intervalFormatCode1) {
        this.intervalFormatCode1 = intervalFormatCode1;
    }

    public int[] getScalarsSet1() {
        return scalarsSet1;
    }

    public void setScalarsSet1(int[] scalarsSet1) {
        this.scalarsSet1 = scalarsSet1;
    }

    public int[] getDivisorSet1() {
        return divisorSet1;
    }

    public void setDivisorSet1(int[] divisorSet1) {
        this.divisorSet1 = divisorSet1;
    }

    public LoadProfileSourceSelection[] getLoadProfileSelectionSet2() {
        return loadProfileSelectionSet2;
    }

    public void setLoadProfileSelectionSet2(LoadProfileSourceSelection[] loadProfileSelectionSet2) {
        this.loadProfileSelectionSet2 = loadProfileSelectionSet2;
    }

    public int getIntervalFormatCode2() {
        return intervalFormatCode2;
    }

    public void setIntervalFormatCode2(int intervalFormatCode2) {
        this.intervalFormatCode2 = intervalFormatCode2;
    }

    public int[] getScalarsSet2() {
        return scalarsSet2;
    }

    public void setScalarsSet2(int[] scalarsSet2) {
        this.scalarsSet2 = scalarsSet2;
    }

    public int[] getDivisorSet2() {
        return divisorSet2;
    }

    public void setDivisorSet2(int[] divisorSet2) {
        this.divisorSet2 = divisorSet2;
    }

    public LoadProfileSourceSelection[] getLoadProfileSelectionSet3() {
        return loadProfileSelectionSet3;
    }

    public void setLoadProfileSelectionSet3(LoadProfileSourceSelection[] loadProfileSelectionSet3) {
        this.loadProfileSelectionSet3 = loadProfileSelectionSet3;
    }

    public int getIntervalFormatCode3() {
        return intervalFormatCode3;
    }

    public void setIntervalFormatCode3(int intervalFormatCode3) {
        this.intervalFormatCode3 = intervalFormatCode3;
    }

    public int[] getScalarsSet3() {
        return scalarsSet3;
    }

    public void setScalarsSet3(int[] scalarsSet3) {
        this.scalarsSet3 = scalarsSet3;
    }

    public int[] getDivisorSet3() {
        return divisorSet3;
    }

    public void setDivisorSet3(int[] divisorSet3) {
        this.divisorSet3 = divisorSet3;
    }

    public LoadProfileSourceSelection[] getLoadProfileSelectionSet4() {
        return loadProfileSelectionSet4;
    }

    public void setLoadProfileSelectionSet4(LoadProfileSourceSelection[] loadProfileSelectionSet4) {
        this.loadProfileSelectionSet4 = loadProfileSelectionSet4;
    }

    public int getIntervalFormatCode4() {
        return intervalFormatCode4;
    }

    public void setIntervalFormatCode4(int intervalFormatCode4) {
        this.intervalFormatCode4 = intervalFormatCode4;
    }

    public int[] getScalarsSet4() {
        return scalarsSet4;
    }

    public void setScalarsSet4(int[] scalarsSet4) {
        this.scalarsSet4 = scalarsSet4;
    }

    public int[] getDivisorSet4() {
        return divisorSet4;
    }

    public void setDivisorSet4(int[] divisorSet4) {
        this.divisorSet4 = divisorSet4;
    }
}
