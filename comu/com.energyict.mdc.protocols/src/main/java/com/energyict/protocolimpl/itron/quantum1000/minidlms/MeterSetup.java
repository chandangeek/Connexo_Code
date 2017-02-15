/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterSetup.java
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
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class MeterSetup extends AbstractDataDefinition {

    private int lineFrequency; // Unsigned8,
    private int synchronization; // Unsigned8, 1=LineSync, 0=XtalSync
    private boolean DSTEnabled; // Boolean,
    private boolean enableDemandResetSwitchInNormalMode; // Boolean,
    private boolean enableDemandResetSwitchInTestMode; // Boolean,
    private boolean enableTestModeSwitchInNormalMode; // Boolean,
    private boolean enableTestModeSwitchInTestMode; // Boolean,
    private boolean enableScrollLockSwitchInNormalMode; // Boolean,
    private boolean enableScrollLockSwitchInTestMode; // Boolean,
    private boolean enableAlternateModeSwitchInNormalMode; // Boolean,
    private boolean enableAlternateModeSwitchInTestMode; // Boolean,
    private int programId; // Unsigned16,
    private String shortMeterId; // OctetString(16),
    private BigDecimal ctMultiplier; // FLOAT,
    private BigDecimal ptMultiplier; // FLOAT,
    private BigDecimal customMultiplier; // FLOAT,
    private int dateFormat; // Unsigned8,
    private String shortSerialNumber; // OctetString(16),

// Average Power Factor Calculation Parameters
    private boolean quadrant_I_enable; // Boolean,
    private boolean quadrant_II_enable; // Boolean,
    private boolean quadrant_III_enable; // Boolean,
    private boolean quadrant_IV_enable; // Boolean,
    private boolean useArithmeticVA; // Boolean,

// Global EOI source Parameters
    private long intervalLength; // Unsigned32,
    private long graceLength; // Unsigned32,
    private boolean timedEOIallowed; //  Booolean
    private boolean externalEOIallowed; //  Boolean,
    private long testModeSwitchTimeOut; //  Unsigned32,
    private float nominalVoltage; //  FLOAT,
    private float nominalCurrent; //  FLOAT,

// installation
    private int form; // UNSIGNED8,
    private int service; // UNSIGNED8,

// ct/vt compensation -phase A
    private boolean phaseAvtCompEnable; // Boolean,
    private float phaseAvtRatioCorrect; // FLOAT,
    private float phaseAvtPhaseCorrect; // FLOAT,
    private float phaseAvtSecondayVolts; // FLOAT,
    private boolean phaseActCompEnable; // Boolean,
    private float phaseActRatioCorrect; // FLOAT,
    private float phaseActPhaseCorrect; // FLOAT,

// ct/vt compensation -phase B
    private boolean phaseBvtCompEnable; // Boolean,
    private float phaseBvtRatioCorrect; // FLOAT,
    private float phaseBvtPhaseCorrect; // FLOAT,
    private float phaseBvtSecondayVolts; // FLOAT,
    private boolean phaseBctCompEnable; // Boolean,
    private float phaseBctRatioCorrect; // FLOAT,
    private float phaseBctPhaseCorrect; // FLOAT,

// ct/vt compensation -phase C
    private boolean phaseCvtCompEnable; // Boolean,
    private float phaseCvtRatioCorrect; // FLOAT,
    private float phaseCvtPhaseCorrect; // FLOAT,
    private float phaseCvtSecondayVolts; // FLOAT,
    private boolean phaseCctCompEnable; // Boolean,
    private float phaseCctRatioCorrect; // FLOAT,
    private float phaseCctPhaseCorrect; // FLOAT,

    /**
     * Creates a new instance of MeterSetup
     */
    public MeterSetup(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterSetup:\n");
        strBuff.append("   DSTEnabled="+isDSTEnabled()+"\n");
        strBuff.append("   ctMultiplier="+getCtMultiplier()+"\n");
        strBuff.append("   customMultiplier="+getCustomMultiplier()+"\n");
        strBuff.append("   dateFormat="+getDateFormat()+"\n");
        strBuff.append("   enableAlternateModeSwitchInNormalMode="+isEnableAlternateModeSwitchInNormalMode()+"\n");
        strBuff.append("   enableAlternateModeSwitchInTestMode="+isEnableAlternateModeSwitchInTestMode()+"\n");
        strBuff.append("   enableDemandResetSwitchInNormalMode="+isEnableDemandResetSwitchInNormalMode()+"\n");
        strBuff.append("   enableDemandResetSwitchInTestMode="+isEnableDemandResetSwitchInTestMode()+"\n");
        strBuff.append("   enableScrollLockSwitchInNormalMode="+isEnableScrollLockSwitchInNormalMode()+"\n");
        strBuff.append("   enableScrollLockSwitchInTestMode="+isEnableScrollLockSwitchInTestMode()+"\n");
        strBuff.append("   enableTestModeSwitchInNormalMode="+isEnableTestModeSwitchInNormalMode()+"\n");
        strBuff.append("   enableTestModeSwitchInTestMode="+isEnableTestModeSwitchInTestMode()+"\n");
        strBuff.append("   externalEOIallowed="+isExternalEOIallowed()+"\n");
        strBuff.append("   form="+getForm()+"\n");
        strBuff.append("   graceLength="+getGraceLength()+"\n");
        strBuff.append("   intervalLength="+getIntervalLength()+"\n");
        strBuff.append("   lineFrequency="+getLineFrequency()+"\n");
        strBuff.append("   nominalCurrent="+getNominalCurrent()+"\n");
        strBuff.append("   nominalVoltage="+getNominalVoltage()+"\n");
        strBuff.append("   phaseActCompEnable="+isPhaseActCompEnable()+"\n");
        strBuff.append("   phaseActPhaseCorrect="+getPhaseActPhaseCorrect()+"\n");
        strBuff.append("   phaseActRatioCorrect="+getPhaseActRatioCorrect()+"\n");
        strBuff.append("   phaseAvtCompEnable="+isPhaseAvtCompEnable()+"\n");
        strBuff.append("   phaseAvtPhaseCorrect="+getPhaseAvtPhaseCorrect()+"\n");
        strBuff.append("   phaseAvtRatioCorrect="+getPhaseAvtRatioCorrect()+"\n");
        strBuff.append("   phaseAvtSecondayVolts="+getPhaseAvtSecondayVolts()+"\n");
        strBuff.append("   phaseBctCompEnable="+isPhaseBctCompEnable()+"\n");
        strBuff.append("   phaseBctPhaseCorrect="+getPhaseBctPhaseCorrect()+"\n");
        strBuff.append("   phaseBctRatioCorrect="+getPhaseBctRatioCorrect()+"\n");
        strBuff.append("   phaseBvtCompEnable="+isPhaseBvtCompEnable()+"\n");
        strBuff.append("   phaseBvtPhaseCorrect="+getPhaseBvtPhaseCorrect()+"\n");
        strBuff.append("   phaseBvtRatioCorrect="+getPhaseBvtRatioCorrect()+"\n");
        strBuff.append("   phaseBvtSecondayVolts="+getPhaseBvtSecondayVolts()+"\n");
        strBuff.append("   phaseCctCompEnable="+isPhaseCctCompEnable()+"\n");
        strBuff.append("   phaseCctPhaseCorrect="+getPhaseCctPhaseCorrect()+"\n");
        strBuff.append("   phaseCctRatioCorrect="+getPhaseCctRatioCorrect()+"\n");
        strBuff.append("   phaseCvtCompEnable="+isPhaseCvtCompEnable()+"\n");
        strBuff.append("   phaseCvtPhaseCorrect="+getPhaseCvtPhaseCorrect()+"\n");
        strBuff.append("   phaseCvtRatioCorrect="+getPhaseCvtRatioCorrect()+"\n");
        strBuff.append("   phaseCvtSecondayVolts="+getPhaseCvtSecondayVolts()+"\n");
        strBuff.append("   programId="+getProgramId()+"\n");
        strBuff.append("   ptMultiplier="+getPtMultiplier()+"\n");
        strBuff.append("   quadrant_III_enable="+isQuadrant_III_enable()+"\n");
        strBuff.append("   quadrant_II_enable="+isQuadrant_II_enable()+"\n");
        strBuff.append("   quadrant_IV_enable="+isQuadrant_IV_enable()+"\n");
        strBuff.append("   quadrant_I_enable="+isQuadrant_I_enable()+"\n");
        strBuff.append("   service="+getService()+"\n");
        strBuff.append("   shortMeterId="+getShortMeterId()+"\n");
        strBuff.append("   shortSerialNumber="+getShortSerialNumber()+"\n");
        strBuff.append("   synchronization="+getSynchronization()+"\n");
        strBuff.append("   testModeSwitchTimeOut="+getTestModeSwitchTimeOut()+"\n");
        strBuff.append("   timedEOIallowed="+isTimedEOIallowed()+"\n");
        strBuff.append("   useArithmeticVA="+isUseArithmeticVA()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 22; // DLMS_METER_SET_UP
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setLineFrequency(ProtocolUtils.getInt(data,offset++,1)); // Unsigned8,
        setSynchronization(ProtocolUtils.getInt(data,offset++,1)); // Unsigned8, 1=LineSync, 0=XtalSync
        setDSTEnabled(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableDemandResetSwitchInNormalMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableDemandResetSwitchInTestMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableTestModeSwitchInNormalMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableTestModeSwitchInTestMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableScrollLockSwitchInNormalMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableScrollLockSwitchInTestMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableAlternateModeSwitchInNormalMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setEnableAlternateModeSwitchInTestMode(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setProgramId(ProtocolUtils.getInt(data,offset,2)); // Unsigned16,
        offset+=2;
        setShortMeterId(new String(ProtocolUtils.getSubArray2(data, offset, 16))); // OctetString(16),
        offset+=16;
        setCtMultiplier(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)))); // bigdecimal,
        offset+=4;
        setPtMultiplier(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)))); // bigdecimal,
        offset+=4;
        setCustomMultiplier(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)))); // bigdecimal,
        offset+=4;
        setDateFormat(ProtocolUtils.getInt(data,offset++,1));
        setShortSerialNumber(new String(ProtocolUtils.getSubArray2(data, offset, 16))); // OctetString(16),
        offset+=16;

// Average Power Factor Calculation Parameters
        setQuadrant_I_enable(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setQuadrant_II_enable(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setQuadrant_III_enable(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setQuadrant_IV_enable(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setUseArithmeticVA(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,

// Global EOI source Parameters
        setIntervalLength(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setGraceLength(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setTimedEOIallowed(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setExternalEOIallowed(ProtocolUtils.getInt(data,offset++,1) == 1); // Boolean,
        setTestModeSwitchTimeOut(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setNominalVoltage(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setNominalCurrent(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;

// installation
        setForm(ProtocolUtils.getInt(data,offset++,1));
        setService(ProtocolUtils.getInt(data,offset++,1));

// ct/vt compensation -phase A
        setPhaseAvtCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseAvtRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseAvtPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseAvtSecondayVolts(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseActCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseActRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseActPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;

// ct/vt compensation -phase B
        setPhaseBvtCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseBvtRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseBvtPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseBvtSecondayVolts(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseBctCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseBctRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseBctPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;

// ct/vt compensation -phase C
        setPhaseCvtCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseCvtRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseCvtPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseCvtSecondayVolts(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseCctCompEnable(ProtocolUtils.getInt(data,offset++,1) == 1);
        setPhaseCctRatioCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
        setPhaseCctPhaseCorrect(Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4)));
        offset+=4;
    }

    public int getLineFrequency() {
        return lineFrequency;
    }

    public void setLineFrequency(int lineFrequency) {
        this.lineFrequency = lineFrequency;
    }

    public int getSynchronization() {
        return synchronization;
    }

    public void setSynchronization(int synchronization) {
        this.synchronization = synchronization;
    }

    public boolean isDSTEnabled() {
        return DSTEnabled;
    }

    public void setDSTEnabled(boolean DSTEnabled) {
        this.DSTEnabled = DSTEnabled;
    }

    public boolean isEnableDemandResetSwitchInNormalMode() {
        return enableDemandResetSwitchInNormalMode;
    }

    public void setEnableDemandResetSwitchInNormalMode(boolean enableDemandResetSwitchInNormalMode) {
        this.enableDemandResetSwitchInNormalMode = enableDemandResetSwitchInNormalMode;
    }

    public boolean isEnableDemandResetSwitchInTestMode() {
        return enableDemandResetSwitchInTestMode;
    }

    public void setEnableDemandResetSwitchInTestMode(boolean enableDemandResetSwitchInTestMode) {
        this.enableDemandResetSwitchInTestMode = enableDemandResetSwitchInTestMode;
    }

    public boolean isEnableTestModeSwitchInNormalMode() {
        return enableTestModeSwitchInNormalMode;
    }

    public void setEnableTestModeSwitchInNormalMode(boolean enableTestModeSwitchInNormalMode) {
        this.enableTestModeSwitchInNormalMode = enableTestModeSwitchInNormalMode;
    }

    public boolean isEnableTestModeSwitchInTestMode() {
        return enableTestModeSwitchInTestMode;
    }

    public void setEnableTestModeSwitchInTestMode(boolean enableTestModeSwitchInTestMode) {
        this.enableTestModeSwitchInTestMode = enableTestModeSwitchInTestMode;
    }

    public boolean isEnableScrollLockSwitchInNormalMode() {
        return enableScrollLockSwitchInNormalMode;
    }

    public void setEnableScrollLockSwitchInNormalMode(boolean enableScrollLockSwitchInNormalMode) {
        this.enableScrollLockSwitchInNormalMode = enableScrollLockSwitchInNormalMode;
    }

    public boolean isEnableScrollLockSwitchInTestMode() {
        return enableScrollLockSwitchInTestMode;
    }

    public void setEnableScrollLockSwitchInTestMode(boolean enableScrollLockSwitchInTestMode) {
        this.enableScrollLockSwitchInTestMode = enableScrollLockSwitchInTestMode;
    }

    public boolean isEnableAlternateModeSwitchInNormalMode() {
        return enableAlternateModeSwitchInNormalMode;
    }

    public void setEnableAlternateModeSwitchInNormalMode(boolean enableAlternateModeSwitchInNormalMode) {
        this.enableAlternateModeSwitchInNormalMode = enableAlternateModeSwitchInNormalMode;
    }

    public boolean isEnableAlternateModeSwitchInTestMode() {
        return enableAlternateModeSwitchInTestMode;
    }

    public void setEnableAlternateModeSwitchInTestMode(boolean enableAlternateModeSwitchInTestMode) {
        this.enableAlternateModeSwitchInTestMode = enableAlternateModeSwitchInTestMode;
    }

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getShortMeterId() {
        return shortMeterId;
    }

    public void setShortMeterId(String shortMeterId) {
        this.shortMeterId = shortMeterId;
    }

    public BigDecimal getCtMultiplier() {
        return ctMultiplier;
    }

    public void setCtMultiplier(BigDecimal ctMultiplier) {
        this.ctMultiplier = ctMultiplier;
    }

    public BigDecimal getPtMultiplier() {
        return ptMultiplier;
    }

    public void setPtMultiplier(BigDecimal ptMultiplier) {
        this.ptMultiplier = ptMultiplier;
    }

    public BigDecimal getCustomMultiplier() {
        return customMultiplier;
    }

    public void setCustomMultiplier(BigDecimal customMultiplier) {
        this.customMultiplier = customMultiplier;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getShortSerialNumber() {
        return shortSerialNumber;
    }

    public void setShortSerialNumber(String shortSerialNumber) {
        this.shortSerialNumber = shortSerialNumber;
    }

    public boolean isQuadrant_I_enable() {
        return quadrant_I_enable;
    }

    public void setQuadrant_I_enable(boolean quadrant_I_enable) {
        this.quadrant_I_enable = quadrant_I_enable;
    }

    public boolean isQuadrant_II_enable() {
        return quadrant_II_enable;
    }

    public void setQuadrant_II_enable(boolean quadrant_II_enable) {
        this.quadrant_II_enable = quadrant_II_enable;
    }

    public boolean isQuadrant_III_enable() {
        return quadrant_III_enable;
    }

    public void setQuadrant_III_enable(boolean quadrant_III_enable) {
        this.quadrant_III_enable = quadrant_III_enable;
    }

    public boolean isQuadrant_IV_enable() {
        return quadrant_IV_enable;
    }

    public void setQuadrant_IV_enable(boolean quadrant_IV_enable) {
        this.quadrant_IV_enable = quadrant_IV_enable;
    }

    public boolean isUseArithmeticVA() {
        return useArithmeticVA;
    }

    public void setUseArithmeticVA(boolean useArithmeticVA) {
        this.useArithmeticVA = useArithmeticVA;
    }

    public long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(long intervalLength) {
        this.intervalLength = intervalLength;
    }

    public long getGraceLength() {
        return graceLength;
    }

    public void setGraceLength(long graceLength) {
        this.graceLength = graceLength;
    }

    public boolean isTimedEOIallowed() {
        return timedEOIallowed;
    }

    public void setTimedEOIallowed(boolean timedEOIallowed) {
        this.timedEOIallowed = timedEOIallowed;
    }

    public boolean isExternalEOIallowed() {
        return externalEOIallowed;
    }

    public void setExternalEOIallowed(boolean externalEOIallowed) {
        this.externalEOIallowed = externalEOIallowed;
    }

    public long getTestModeSwitchTimeOut() {
        return testModeSwitchTimeOut;
    }

    public void setTestModeSwitchTimeOut(long testModeSwitchTimeOut) {
        this.testModeSwitchTimeOut = testModeSwitchTimeOut;
    }

    public float getNominalVoltage() {
        return nominalVoltage;
    }

    public void setNominalVoltage(float nominalVoltage) {
        this.nominalVoltage = nominalVoltage;
    }

    public float getNominalCurrent() {
        return nominalCurrent;
    }

    public void setNominalCurrent(float nominalCurrent) {
        this.nominalCurrent = nominalCurrent;
    }

    public int getForm() {
        return form;
    }

    public void setForm(int form) {
        this.form = form;
    }

    public int getService() {
        return service;
    }

    public void setService(int service) {
        this.service = service;
    }

    public boolean isPhaseAvtCompEnable() {
        return phaseAvtCompEnable;
    }

    public void setPhaseAvtCompEnable(boolean phaseAvtCompEnable) {
        this.phaseAvtCompEnable = phaseAvtCompEnable;
    }

    public float getPhaseAvtRatioCorrect() {
        return phaseAvtRatioCorrect;
    }

    public void setPhaseAvtRatioCorrect(float phaseAvtRatioCorrect) {
        this.phaseAvtRatioCorrect = phaseAvtRatioCorrect;
    }

    public float getPhaseAvtPhaseCorrect() {
        return phaseAvtPhaseCorrect;
    }

    public void setPhaseAvtPhaseCorrect(float phaseAvtPhaseCorrect) {
        this.phaseAvtPhaseCorrect = phaseAvtPhaseCorrect;
    }

    public float getPhaseAvtSecondayVolts() {
        return phaseAvtSecondayVolts;
    }

    public void setPhaseAvtSecondayVolts(float phaseAvtSecondayVolts) {
        this.phaseAvtSecondayVolts = phaseAvtSecondayVolts;
    }

    public boolean isPhaseActCompEnable() {
        return phaseActCompEnable;
    }

    public void setPhaseActCompEnable(boolean phaseActCompEnable) {
        this.phaseActCompEnable = phaseActCompEnable;
    }

    public float getPhaseActRatioCorrect() {
        return phaseActRatioCorrect;
    }

    public void setPhaseActRatioCorrect(float phaseActRatioCorrect) {
        this.phaseActRatioCorrect = phaseActRatioCorrect;
    }

    public float getPhaseActPhaseCorrect() {
        return phaseActPhaseCorrect;
    }

    public void setPhaseActPhaseCorrect(float phaseActPhaseCorrect) {
        this.phaseActPhaseCorrect = phaseActPhaseCorrect;
    }

    public boolean isPhaseBvtCompEnable() {
        return phaseBvtCompEnable;
    }

    public void setPhaseBvtCompEnable(boolean phaseBvtCompEnable) {
        this.phaseBvtCompEnable = phaseBvtCompEnable;
    }

    public float getPhaseBvtRatioCorrect() {
        return phaseBvtRatioCorrect;
    }

    public void setPhaseBvtRatioCorrect(float phaseBvtRatioCorrect) {
        this.phaseBvtRatioCorrect = phaseBvtRatioCorrect;
    }

    public float getPhaseBvtPhaseCorrect() {
        return phaseBvtPhaseCorrect;
    }

    public void setPhaseBvtPhaseCorrect(float phaseBvtPhaseCorrect) {
        this.phaseBvtPhaseCorrect = phaseBvtPhaseCorrect;
    }

    public float getPhaseBvtSecondayVolts() {
        return phaseBvtSecondayVolts;
    }

    public void setPhaseBvtSecondayVolts(float phaseBvtSecondayVolts) {
        this.phaseBvtSecondayVolts = phaseBvtSecondayVolts;
    }

    public boolean isPhaseBctCompEnable() {
        return phaseBctCompEnable;
    }

    public void setPhaseBctCompEnable(boolean phaseBctCompEnable) {
        this.phaseBctCompEnable = phaseBctCompEnable;
    }

    public float getPhaseBctRatioCorrect() {
        return phaseBctRatioCorrect;
    }

    public void setPhaseBctRatioCorrect(float phaseBctRatioCorrect) {
        this.phaseBctRatioCorrect = phaseBctRatioCorrect;
    }

    public float getPhaseBctPhaseCorrect() {
        return phaseBctPhaseCorrect;
    }

    public void setPhaseBctPhaseCorrect(float phaseBctPhaseCorrect) {
        this.phaseBctPhaseCorrect = phaseBctPhaseCorrect;
    }

    public boolean isPhaseCvtCompEnable() {
        return phaseCvtCompEnable;
    }

    public void setPhaseCvtCompEnable(boolean phaseCvtCompEnable) {
        this.phaseCvtCompEnable = phaseCvtCompEnable;
    }

    public float getPhaseCvtRatioCorrect() {
        return phaseCvtRatioCorrect;
    }

    public void setPhaseCvtRatioCorrect(float phaseCvtRatioCorrect) {
        this.phaseCvtRatioCorrect = phaseCvtRatioCorrect;
    }

    public float getPhaseCvtPhaseCorrect() {
        return phaseCvtPhaseCorrect;
    }

    public void setPhaseCvtPhaseCorrect(float phaseCvtPhaseCorrect) {
        this.phaseCvtPhaseCorrect = phaseCvtPhaseCorrect;
    }

    public float getPhaseCvtSecondayVolts() {
        return phaseCvtSecondayVolts;
    }

    public void setPhaseCvtSecondayVolts(float phaseCvtSecondayVolts) {
        this.phaseCvtSecondayVolts = phaseCvtSecondayVolts;
    }

    public boolean isPhaseCctCompEnable() {
        return phaseCctCompEnable;
    }

    public void setPhaseCctCompEnable(boolean phaseCctCompEnable) {
        this.phaseCctCompEnable = phaseCctCompEnable;
    }

    public float getPhaseCctRatioCorrect() {
        return phaseCctRatioCorrect;
    }

    public void setPhaseCctRatioCorrect(float phaseCctRatioCorrect) {
        this.phaseCctRatioCorrect = phaseCctRatioCorrect;
    }

    public float getPhaseCctPhaseCorrect() {
        return phaseCctPhaseCorrect;
    }

    public void setPhaseCctPhaseCorrect(float phaseCctPhaseCorrect) {
        this.phaseCctPhaseCorrect = phaseCctPhaseCorrect;
    }
}
