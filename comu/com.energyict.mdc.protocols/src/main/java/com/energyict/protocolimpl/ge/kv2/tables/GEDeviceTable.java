/*
 * GEDeviceTable.java
 *
 * Created on 19 oktober 2005, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class GEDeviceTable extends AbstractTable {


    private int mfgVersion; // 1 byte
    private int mfgRevision; // 1 byte
    // rom constants
    private String gePartNumber; // 5 bytes
    private int fwVersion; // 1 byte
    private int fwRevision; // 1 byte
    private int fwBuild; // 1 byte
    private long mfgTestVector; // 4 bytes
    private int meterType; // 1 byte (0 = CM21P, 1 = kV, 2 = kV Modem, 3 = kV2)
    private int meterMode; // 1 byte (0 = Demand-only, 1 = Demand/LP, 2 = TOU)
    private int registerFunction; // 1 byte
    private int installedOption1; // 1 byte
    private int installedOption2; // 1 byte
    private int installedOption3; // 1 byte
    private int installedOption4; // 1 byte
    private int installedOption5; // 1 byte
    private int installedOption6; // 1 byte
    private long upgradesBitfield; // 32 bit
    private boolean upgrade0TOU; // Indicates if meter has been upgraded for TOU.  (0 = False, 1 = True)
    private boolean upgrade1SecondMeasure; // Indicates if meter has been upgraded for Second Measure.  (0 = False, 1 = True)
    private boolean upgrade2Recording; // Indicates if meter has been upgraded for recording (4-channel)/self-read.  (0 = False, 1 = True)
    private boolean upgrade3EventLogging; // Indicates if meter has been upgraded for event logging.  (0 = False, 1 = True)
    private boolean upgrade4Altcomm; // Indicates if meter has been upgraded for alternate communication 1.  (0 = False, 1 = True)
    private boolean upgrade5DSPSampleOutput; // Indicates if meter has been upgraded for DSP sample output.  (0 = False, 1 = True)
    private boolean upgrade6PulseInitiatorOutput; // Indicates whether meter has been upgraded for pulse initiator output.  (0 = False, 1 = True)
    private boolean upgrade7; // Reserved for future use.
    private boolean upgrade8Recording20Channels; // Indicates if the meter has been upgraded for 20-channel recording.  (0 = False, 1 = True)
    private boolean upgrade9TransformerLossComp; // Indicates if the meter has been upgraded for transformer loss compensation.  (0 = False, 1 = True)
    private boolean upgrade10TransformerAccuracyAdj; // Indicates if the meter has been upgraded for transformer accuracy adjustment.  (0 = False, 1 = True)
    private boolean upgrade11RevenueGuardPlus; // Indicates if the meter has been upgraded for Revenue Guard Plus.  (0 = False, 1 = True)
    private boolean upgrade12VoltageEventMonitoring; // Indicates if the meter has been upgraded for voltage event monitoring.  (0 = False, 1 = True)
    private boolean upgrade13BiDirMeasurement; // Indicates if the meter has been upgraded for bi-directional measurement.  (0 = False, 1 = True)
    private boolean upgrade14WaveFormCapture; // Indicates if the meter has been upgraded for waveform capture.  (0 = False, 1 = True)
    private boolean upgrade15ExpandedMeasure; // Indicates if the meter has been upgraded for expanded measure. (0 = False, 1 = True)
    private boolean upgrade16PowerQualityMonitoring; // Indicates if the meter has been upgraded for power quality monitoring. (0 = False, 1 = True)
    private boolean upgrade17Totalization; // Indicates whether meter has been upgraded for  totalization.  (0 = False, 1 = True)
    // long reserved; // 4 bytes
    // flash constants
    private String partNumber; // 5 bytes
    private int romVersion; // 1 byte
    private int romRevision; // 1 byte
    private int romBuild; // 1 byte
    private int flashVersion; // 1 byte
    private int flashRevision; // 1 byte
    private int flashBuild; // 1 byte
    private int checksum; // 2 bytes
    private int patchFlags; // 2 bytes
    // user calculation constants
    private String userPartNumber; // 5 bytes
    private int userRomVersion; // 1 byte
    private int userRomRevision; // 1 byte
    private int userRomBuild; // 1 byte
    private int userFlashVersion; // 1 byte
    private int userFlashRevision; // 1 byte
    private int userFlashBuild; // 1 byte
    private int userChecksum; // 2 bytes

    /** Creates a new instance of GEDeviceTable */
    public GEDeviceTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(0,true));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GEDeviceTable: \n");
        strBuff.append("    mfgVersion="+mfgVersion+", ");
        strBuff.append("mfgRevision="+mfgRevision+", ");
        // rom constants
        strBuff.append("gePartNumber="+gePartNumber+", "); // 5 bytes
        strBuff.append("fwVersion="+fwVersion+", "); // 1 byte
        strBuff.append("fwRevision="+fwRevision+", "); // 1 byte
        strBuff.append("fwBuild="+fwBuild+", "); // 1 byte
        strBuff.append("mfgTestVector="+mfgTestVector+"\n"); // 4 bytes
        strBuff.append("    meterType="+meterType+", "); // 1 byte
        strBuff.append("meterMode="+meterMode+", "); // 1 byte
        strBuff.append("registerFunction="+registerFunction+", "); // 1 byte
        strBuff.append("installedOption1="+installedOption1+", "); // 1 byte
        strBuff.append("installedOption2="+installedOption2+", "); // 1 byte
        strBuff.append("installedOption3="+installedOption3+", "); // 1 byte
        strBuff.append("installedOption4="+installedOption4+", "); // 1 byte
        strBuff.append("installedOption5="+installedOption5+", "); // 1 byte
        strBuff.append("installedOption6="+installedOption6+"\n"); // 1 byte
        strBuff.append("    upgradesBitfield=0x"+Long.toHexString(upgradesBitfield)+", "); // 32 bit
        strBuff.append("upgrade0TOU="+upgrade0TOU+", "); // Indicates if meter has been upgraded for TOU.  (0 = False, 1 = True)
        strBuff.append("upgrade1SecondMeasure="+upgrade1SecondMeasure+", "); // Indicates if meter has been upgraded for Second Measure.  (0 = False, 1 = True)
        strBuff.append("upgrade2Recording="+upgrade2Recording+", "); // Indicates if meter has been upgraded for recording (4-channel)/self-read.  (0 = False, 1 = True)
        strBuff.append("upgrade3EventLogging="+upgrade3EventLogging+", "); // Indicates if meter has been upgraded for event logging.  (0 = False, 1 = True)
        strBuff.append("upgrade4Altcomm="+upgrade4Altcomm+", "); // Indicates if meter has been upgraded for alternate communication 1.  (0 = False, 1 = True)
        strBuff.append("upgrade5DSPSampleOutput="+upgrade5DSPSampleOutput+", "); // Indicates if meter has been upgraded for DSP sample output.  (0 = False, 1 = True)
        strBuff.append("upgrade6PulseInitiatorOutput="+upgrade6PulseInitiatorOutput+", "); // Indicates whether meter has been upgraded for pulse initiator output.  (0 = False, 1 = True)
        strBuff.append("upgrade7="+upgrade7+", "); // Reserved for future use.
        strBuff.append("upgrade8Recording20Channels="+upgrade8Recording20Channels+", "); // Indicates if the meter has been upgraded for 20-channel recording.  (0 = False, 1 = True)
        strBuff.append("upgrade9TransformerLossComp="+upgrade9TransformerLossComp+", "); // Indicates if the meter has been upgraded for transformer loss compensation.  (0 = False, 1 = True)
        strBuff.append("upgrade10TransformerAccuracyAdj="+upgrade10TransformerAccuracyAdj+", "); // Indicates if the meter has been upgraded for transformer accuracy adjustment.  (0 = False, 1 = True)
        strBuff.append("upgrade11RevenueGuardPlus="+upgrade11RevenueGuardPlus+", "); // Indicates if the meter has been upgraded for Revenue Guard Plus.  (0 = False, 1 = True)
        strBuff.append("upgrade12VoltageEventMonitoring="+upgrade12VoltageEventMonitoring+", "); // Indicates if the meter has been upgraded for voltage event monitoring.  (0 = False, 1 = True)
        strBuff.append("upgrade13BiDirMeasurement="+upgrade13BiDirMeasurement+", "); // Indicates if the meter has been upgraded for bi-directional measurement.  (0 = False, 1 = True)
        strBuff.append("upgrade14WaveFormCapture="+upgrade14WaveFormCapture+", "); // Indicates if the meter has been upgraded for waveform capture.  (0 = False, 1 = True)
        strBuff.append("upgrade15ExpandedMeasure="+upgrade15ExpandedMeasure+", "); // Indicates if the meter has been upgraded for expanded measure. (0 = False, 1 = True)
        strBuff.append("upgrade16PowerQualityMonitoring="+upgrade16PowerQualityMonitoring+", "); // Indicates if the meter has been upgraded for power quality monitoring. (0 = False, 1 = True)
        strBuff.append("upgrade17Totalization="+upgrade17Totalization+"\n"); // Indicates whether meter has been upgraded for  totalization.  (0 = False, 1 = True)
        // long reserved; // 4 bytes
        // flash constants
        strBuff.append("    partNumber="+partNumber+", "); // 5 bytes
        strBuff.append("romVersion="+romVersion+", "); // 1 byte
        strBuff.append("romRevision="+romRevision+", "); // 1 byte
        strBuff.append("romBuild="+romBuild+", "); // 1 byte
        strBuff.append("flashVersion="+flashVersion+", "); // 1 byte
        strBuff.append("flashRevision="+flashRevision+", "); // 1 byte
        strBuff.append("flashBuild="+flashBuild+", "); // 1 byte
        strBuff.append("checksum="+checksum+", "); // 2 bytes
        strBuff.append("patchFlags="+patchFlags+"\n"); // 2 bytes
        // user calculation constants
        strBuff.append("    userPartNumber="+userPartNumber+", "); // 5 bytes
        strBuff.append("userRomVersion="+userRomVersion+", "); // 1 byte
        strBuff.append("userRomRevision="+userRomRevision+", "); // 1 byte
        strBuff.append("userRomBuild="+userRomBuild+", "); // 1 byte
        strBuff.append("userFlashVersion="+userFlashVersion+", "); // 1 byte
        strBuff.append("userFlashRevision="+userFlashRevision+", "); // 1 byte
        strBuff.append("userFlashBuild="+userFlashBuild+", "); // 1 byte
        strBuff.append("userChecksum="+userChecksum+"\n"); // 2 bytes


        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setMfgVersion(C12ParseUtils.getInt(tableData,offset++));
        setMfgRevision(C12ParseUtils.getInt(tableData,offset++));
        // rom constants
        setGePartNumber(new String(ProtocolUtils.getSubArray2(tableData,offset, 5)));
        offset+=5;
        setFwVersion(C12ParseUtils.getInt(tableData,offset++));
        setFwRevision(C12ParseUtils.getInt(tableData,offset++));
        setFwBuild(C12ParseUtils.getInt(tableData,offset++));
        setMfgTestVector(C12ParseUtils.getLong(tableData,offset,4,dataOrder));
        offset+=4;
        setMeterType(C12ParseUtils.getInt(tableData,offset++));
        setMeterMode(C12ParseUtils.getInt(tableData,offset++));
        setRegisterFunction(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption1(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption2(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption3(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption4(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption5(C12ParseUtils.getInt(tableData,offset++));
        setInstalledOption6(C12ParseUtils.getInt(tableData,offset++));
        setUpgradesBitfield(C12ParseUtils.getLong(tableData,offset,4,dataOrder));
        offset+=4;
        setUpgrade0TOU((getUpgradesBitfield() & 0x00000001) == 0x00000001);
        setUpgrade1SecondMeasure((getUpgradesBitfield() & 0x00000002) == 0x00000002);
        setUpgrade2Recording((getUpgradesBitfield() & 0x00000004) == 0x00000004);
        setUpgrade3EventLogging((getUpgradesBitfield() & 0x00000008) == 0x00000008);
        setUpgrade4Altcomm((getUpgradesBitfield() & 0x00000010) == 0x00000010);
        setUpgrade5DSPSampleOutput((getUpgradesBitfield() & 0x00000020) == 0x00000020);
        setUpgrade6PulseInitiatorOutput((getUpgradesBitfield() & 0x00000040) == 0x00000040);
        setUpgrade7((getUpgradesBitfield() & 0x00000080) == 0x00000080);
        setUpgrade8Recording20Channels((getUpgradesBitfield() & 0x00000100) == 0x00000100);
        setUpgrade9TransformerLossComp((getUpgradesBitfield() & 0x00000200) == 0x00000200);
        setUpgrade10TransformerAccuracyAdj((getUpgradesBitfield() & 0x00000400) == 0x00000400);
        setUpgrade11RevenueGuardPlus((getUpgradesBitfield() & 0x00000800) == 0x00000800);
        setUpgrade12VoltageEventMonitoring((getUpgradesBitfield() & 0x00001000) == 0x00001000);
        setUpgrade13BiDirMeasurement((getUpgradesBitfield() & 0x00002000) == 0x00002000);
        setUpgrade14WaveFormCapture((getUpgradesBitfield() & 0x00004000) == 0x00004000);
        setUpgrade15ExpandedMeasure((getUpgradesBitfield() & 0x00008000) == 0x00008000);
        setUpgrade16PowerQualityMonitoring((getUpgradesBitfield() & 0x00010000) == 0x00010000);
        setUpgrade17Totalization((getUpgradesBitfield() & 0x00020000) == 0x00020000);
        offset+=4; // long reserved; // 4 bytes
        // flash constants
        setPartNumber(new String(ProtocolUtils.getSubArray2(tableData,offset, 5)));
        offset+=5;
        setRomVersion(C12ParseUtils.getInt(tableData,offset++));
        setRomRevision(C12ParseUtils.getInt(tableData,offset++));
        setRomBuild(C12ParseUtils.getInt(tableData,offset++));
        setFlashVersion(C12ParseUtils.getInt(tableData,offset++));
        setFlashRevision(C12ParseUtils.getInt(tableData,offset++));
        setFlashBuild(C12ParseUtils.getInt(tableData,offset++));
        setChecksum(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        setPatchFlags(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
        offset+=2;
        // user calculation constants
        setUserPartNumber(new String(ProtocolUtils.getSubArray2(tableData,offset, 5)));
        offset+=5;
        setUserRomVersion(C12ParseUtils.getInt(tableData,offset++));
        setUserRomRevision(C12ParseUtils.getInt(tableData,offset++));
        setUserRomBuild(C12ParseUtils.getInt(tableData,offset++));
        setUserFlashVersion(C12ParseUtils.getInt(tableData,offset++));
        setUserFlashRevision(C12ParseUtils.getInt(tableData,offset++));
        setUserFlashBuild(C12ParseUtils.getInt(tableData,offset++));

// The KV2 only returns 58 bytes instead of 59 bytes needed if we read 2 bytes checksum...
//        setUserChecksum(C12ParseUtils.getInt(tableData,offset,2,dataOrder));
//        offset+=2;
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public int getMfgVersion() {
        return mfgVersion;
    }

    public void setMfgVersion(int mfgVersion) {
        this.mfgVersion = mfgVersion;
    }

    public int getMfgRevision() {
        return mfgRevision;
    }

    public void setMfgRevision(int mfgRevision) {
        this.mfgRevision = mfgRevision;
    }

    public String getGePartNumber() {
        return gePartNumber;
    }

    public void setGePartNumber(String gePartNumber) {
        this.gePartNumber = gePartNumber;
    }

    public int getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(int fwVersion) {
        this.fwVersion = fwVersion;
    }

    public int getFwRevision() {
        return fwRevision;
    }

    public void setFwRevision(int fwRevision) {
        this.fwRevision = fwRevision;
    }

    public int getFwBuild() {
        return fwBuild;
    }

    public void setFwBuild(int fwBuild) {
        this.fwBuild = fwBuild;
    }

    public long getMfgTestVector() {
        return mfgTestVector;
    }

    public void setMfgTestVector(long mfgTestVector) {
        this.mfgTestVector = mfgTestVector;
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

    public long getUpgradesBitfield() {
        return upgradesBitfield;
    }

    public void setUpgradesBitfield(long upgradesBitfield) {
        this.upgradesBitfield = upgradesBitfield;
    }

    public boolean isUpgrade0TOU() {
        return upgrade0TOU;
    }

    public void setUpgrade0TOU(boolean upgrade0TOU) {
        this.upgrade0TOU = upgrade0TOU;
    }

    public boolean isUpgrade1SecondMeasure() {
        return upgrade1SecondMeasure;
    }

    public void setUpgrade1SecondMeasure(boolean upgrade1SecondMeasure) {
        this.upgrade1SecondMeasure = upgrade1SecondMeasure;
    }

    public boolean isUpgrade2Recording() {
        return upgrade2Recording;
    }

    public void setUpgrade2Recording(boolean upgrade2Recording) {
        this.upgrade2Recording = upgrade2Recording;
    }

    public boolean isUpgrade3EventLogging() {
        return upgrade3EventLogging;
    }

    public void setUpgrade3EventLogging(boolean upgrade3EventLogging) {
        this.upgrade3EventLogging = upgrade3EventLogging;
    }

    public boolean isUpgrade4Altcomm() {
        return upgrade4Altcomm;
    }

    public void setUpgrade4Altcomm(boolean upgrade4Altcomm) {
        this.upgrade4Altcomm = upgrade4Altcomm;
    }

    public boolean isUpgrade5DSPSampleOutput() {
        return upgrade5DSPSampleOutput;
    }

    public void setUpgrade5DSPSampleOutput(boolean upgrade5DSPSampleOutput) {
        this.upgrade5DSPSampleOutput = upgrade5DSPSampleOutput;
    }

    public boolean isUpgrade6PulseInitiatorOutput() {
        return upgrade6PulseInitiatorOutput;
    }

    public void setUpgrade6PulseInitiatorOutput(boolean upgrade6PulseInitiatorOutput) {
        this.upgrade6PulseInitiatorOutput = upgrade6PulseInitiatorOutput;
    }

    public boolean isUpgrade7() {
        return upgrade7;
    }

    public void setUpgrade7(boolean upgrade7) {
        this.upgrade7 = upgrade7;
    }

    public boolean isUpgrade8Recording20Channels() {
        return upgrade8Recording20Channels;
    }

    public void setUpgrade8Recording20Channels(boolean upgrade8Recording20Channels) {
        this.upgrade8Recording20Channels = upgrade8Recording20Channels;
    }

    public boolean isUpgrade9TransformerLossComp() {
        return upgrade9TransformerLossComp;
    }

    public void setUpgrade9TransformerLossComp(boolean upgrade9TransformerLossComp) {
        this.upgrade9TransformerLossComp = upgrade9TransformerLossComp;
    }

    public boolean isUpgrade10TransformerAccuracyAdj() {
        return upgrade10TransformerAccuracyAdj;
    }

    public void setUpgrade10TransformerAccuracyAdj(boolean upgrade10TransformerAccuracyAdj) {
        this.upgrade10TransformerAccuracyAdj = upgrade10TransformerAccuracyAdj;
    }

    public boolean isUpgrade11RevenueGuardPlus() {
        return upgrade11RevenueGuardPlus;
    }

    public void setUpgrade11RevenueGuardPlus(boolean upgrade11RevenueGuardPlus) {
        this.upgrade11RevenueGuardPlus = upgrade11RevenueGuardPlus;
    }

    public boolean isUpgrade12VoltageEventMonitoring() {
        return upgrade12VoltageEventMonitoring;
    }

    public void setUpgrade12VoltageEventMonitoring(boolean upgrade12VoltageEventMonitoring) {
        this.upgrade12VoltageEventMonitoring = upgrade12VoltageEventMonitoring;
    }

    public boolean isUpgrade13BiDirMeasurement() {
        return upgrade13BiDirMeasurement;
    }

    public void setUpgrade13BiDirMeasurement(boolean upgrade13BiDirMeasurement) {
        this.upgrade13BiDirMeasurement = upgrade13BiDirMeasurement;
    }

    public boolean isUpgrade14WaveFormCapture() {
        return upgrade14WaveFormCapture;
    }

    public void setUpgrade14WaveFormCapture(boolean upgrade14WaveFormCapture) {
        this.upgrade14WaveFormCapture = upgrade14WaveFormCapture;
    }

    public boolean isUpgrade15ExpandedMeasure() {
        return upgrade15ExpandedMeasure;
    }

    public void setUpgrade15ExpandedMeasure(boolean upgrade15ExpandedMeasure) {
        this.upgrade15ExpandedMeasure = upgrade15ExpandedMeasure;
    }

    public boolean isUpgrade16PowerQualityMonitoring() {
        return upgrade16PowerQualityMonitoring;
    }

    public void setUpgrade16PowerQualityMonitoring(boolean upgrade16PowerQualityMonitoring) {
        this.upgrade16PowerQualityMonitoring = upgrade16PowerQualityMonitoring;
    }

    public boolean isUpgrade17Totalization() {
        return upgrade17Totalization;
    }

    public void setUpgrade17Totalization(boolean upgrade17Totalization) {
        this.upgrade17Totalization = upgrade17Totalization;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public int getRomVersion() {
        return romVersion;
    }

    public void setRomVersion(int romVersion) {
        this.romVersion = romVersion;
    }

    public int getRomRevision() {
        return romRevision;
    }

    public void setRomRevision(int romRevision) {
        this.romRevision = romRevision;
    }

    public int getRomBuild() {
        return romBuild;
    }

    public void setRomBuild(int romBuild) {
        this.romBuild = romBuild;
    }

    public int getFlashVersion() {
        return flashVersion;
    }

    public void setFlashVersion(int flashVersion) {
        this.flashVersion = flashVersion;
    }

    public int getFlashRevision() {
        return flashRevision;
    }

    public void setFlashRevision(int flashRevision) {
        this.flashRevision = flashRevision;
    }

    public int getFlashBuild() {
        return flashBuild;
    }

    public void setFlashBuild(int flashBuild) {
        this.flashBuild = flashBuild;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getPatchFlags() {
        return patchFlags;
    }

    public void setPatchFlags(int patchFlags) {
        this.patchFlags = patchFlags;
    }

    public String getUserPartNumber() {
        return userPartNumber;
    }

    public void setUserPartNumber(String userPartNumber) {
        this.userPartNumber = userPartNumber;
    }

    public int getUserRomVersion() {
        return userRomVersion;
    }

    public void setUserRomVersion(int userRomVersion) {
        this.userRomVersion = userRomVersion;
    }

    public int getUserRomRevision() {
        return userRomRevision;
    }

    public void setUserRomRevision(int userRomRevision) {
        this.userRomRevision = userRomRevision;
    }

    public int getUserRomBuild() {
        return userRomBuild;
    }

    public void setUserRomBuild(int userRomBuild) {
        this.userRomBuild = userRomBuild;
    }

    public int getUserFlashVersion() {
        return userFlashVersion;
    }

    public void setUserFlashVersion(int userFlashVersion) {
        this.userFlashVersion = userFlashVersion;
    }

    public int getUserFlashRevision() {
        return userFlashRevision;
    }

    public void setUserFlashRevision(int userFlashRevision) {
        this.userFlashRevision = userFlashRevision;
    }

    public int getUserFlashBuild() {
        return userFlashBuild;
    }

    public void setUserFlashBuild(int userFlashBuild) {
        this.userFlashBuild = userFlashBuild;
    }

    public int getUserChecksum() {
        return userChecksum;
    }

    public void setUserChecksum(int userChecksum) {
        this.userChecksum = userChecksum;
    }


}
