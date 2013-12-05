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

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MFGParameter {


    private PowerFactorMethod powerFactorMethod;
    private ReverseRotation reverseRotation;
    private TransformerFactor transformerFactor;
    private TestModeParam testModeParam;
    private DemandResetConfig demandResetConfig;
    private int peakDemandWindow; // : UINT8;
    private VerificationLedConfig verificationLedConfig;
    private int selfReadDaysLastReset; // : UINT16;

    /** Creates a new instance of RecordTemplate */
    public MFGParameter(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();

        setPowerFactorMethod(new PowerFactorMethod(data, offset, tableFactory));
        offset += PowerFactorMethod.getSize(tableFactory);

        setReverseRotation(new ReverseRotation(data, offset, tableFactory));
        offset += ReverseRotation.getSize(tableFactory);

        setTransformerFactor(new TransformerFactor(data, offset, tableFactory));
        offset += TransformerFactor.getSize(tableFactory);

        setTestModeParam(new TestModeParam(data, offset, tableFactory));
        offset += TestModeParam.getSize(tableFactory);

        setDemandResetConfig(new DemandResetConfig(data, offset, tableFactory));
        offset += DemandResetConfig.getSize(tableFactory);

        setPeakDemandWindow((int)data[offset++]&0xFF);

        setVerificationLedConfig(new VerificationLedConfig(data, offset, tableFactory));
        offset += VerificationLedConfig.getSize(tableFactory);

        setSelfReadDaysLastReset(C12ParseUtils.getInt(data,offset,2, cfgt.getDataOrder()));
        offset+=2;

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MFGParameter:\n");
        strBuff.append("   demandResetConfig="+getDemandResetConfig()+"\n");
        strBuff.append("   peakDemandWindow="+getPeakDemandWindow()+"\n");
        strBuff.append("   powerFactorMethod="+getPowerFactorMethod()+"\n");
        strBuff.append("   reverseRotation="+getReverseRotation()+"\n");
        strBuff.append("   selfReadDaysLastReset="+getSelfReadDaysLastReset()+"\n");
        strBuff.append("   testModeParam="+getTestModeParam()+"\n");
        strBuff.append("   transformerFactor="+getTransformerFactor()+"\n");
        strBuff.append("   verificationLedConfig="+getVerificationLedConfig()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return PowerFactorMethod.getSize(tableFactory)+
               ReverseRotation.getSize(tableFactory)+
               TransformerFactor.getSize(tableFactory)+
               TestModeParam.getSize(tableFactory)+
               DemandResetConfig.getSize(tableFactory)+
               1+
               VerificationLedConfig.getSize(tableFactory)+
               2;
    }

    public PowerFactorMethod getPowerFactorMethod() {
        return powerFactorMethod;
    }

    private void setPowerFactorMethod(PowerFactorMethod powerFactorMethod) {
        this.powerFactorMethod = powerFactorMethod;
    }

    public ReverseRotation getReverseRotation() {
        return reverseRotation;
    }

    private void setReverseRotation(ReverseRotation reverseRotation) {
        this.reverseRotation = reverseRotation;
    }

    public TransformerFactor getTransformerFactor() {
        return transformerFactor;
    }

    private void setTransformerFactor(TransformerFactor transformerFactor) {
        this.transformerFactor = transformerFactor;
    }

    public TestModeParam getTestModeParam() {
        return testModeParam;
    }

    private void setTestModeParam(TestModeParam testModeParam) {
        this.testModeParam = testModeParam;
    }

    public DemandResetConfig getDemandResetConfig() {
        return demandResetConfig;
    }

    private void setDemandResetConfig(DemandResetConfig demandResetConfig) {
        this.demandResetConfig = demandResetConfig;
    }

    public int getPeakDemandWindow() {
        return peakDemandWindow;
    }

    private void setPeakDemandWindow(int peakDemandWindow) {
        this.peakDemandWindow = peakDemandWindow;
    }

    public VerificationLedConfig getVerificationLedConfig() {
        return verificationLedConfig;
    }

    private void setVerificationLedConfig(VerificationLedConfig verificationLedConfig) {
        this.verificationLedConfig = verificationLedConfig;
    }

    public int getSelfReadDaysLastReset() {
        return selfReadDaysLastReset;
    }

    private void setSelfReadDaysLastReset(int selfReadDaysLastReset) {
        this.selfReadDaysLastReset = selfReadDaysLastReset;
    }

}
