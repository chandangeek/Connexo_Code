/*
 * GeneralDiagnosticInfo.java
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
import java.util.Date;

/**
 *
 * @author Koen
 */
public class GeneralDiagnosticInfo extends AbstractDataDefinition {

    private int voltageStatus; // Unsigned8,
    private int numberOfTestModeEntries; // Unsigned16,
    private int numberOfOutages; // Unsigned16,
    private int numberOfTimesProgrammed; // Unsigned16,
    private int numberOfCalibrations; // Unsigned16,
    private Date lastOutageTime; // EXTENDED_DATE_AND_TIME,
    private Date lastReprogramTime; // EXTENDED_DATE_AND_TIME,
    private Date lastCalibrationTime; // EXTENDED_DATE_AND_TIME,
    private Date lastTestModeEntryTime; // EXTENDED_DATE_AND_TIME,
    private Date DSTspring; // DATE_AND_TIME,
    private Date DSTfall; // DATE_AND_TIME,
    private int resetReasons; // Unsigned8,
    private String firmwareRevision; // OctetString(6)
    private boolean scrollLockSwitch; // Boolean,
    private boolean AltModeSwitch; // Boolean,
    private boolean TestModeSwitch; // Boolean,
    private boolean DemandResetSwitch; // Boolean,
    private boolean FactoryDefaultSwitch; // Boolean,
    private boolean ProgramProtectSwitch; // Boolean,
    private boolean ModemAutoAnswerSwitch; // Boolean,
    private int currentSession; // UNSIGNED8,
    private int numberOfActiveSessions; // UNSIGNED8,
    private SessionInfo[] sessionInfos;
    private int numberOfProgrammedMassMem; // UNSIGNED8,
    private int numberOfCompleteInitializations; // UNSIGNED16,
    private boolean isGlobalEOIActive; // Boolean,
    private Date nextGlobalEOITime; // EXTENDED_DATE_AND_TIME,
    private int secondsRemainingTilGlobalEOI; // UNSIGNED16,
    private boolean isGlobalEOIPoweredUp; // Boolean,
    private boolean isInGracePeriod; // Boolean,
    private Result lastGlobalEOIErrorResult; // RESULT,
    private long externalEOICount; // UNSIGNED32,
    private Date lastExternalEOI; // EXTENDED_DATE_AND_TIME,
    private boolean isGlobalEOIStatusOK; // Boolean,
    private long frontEndMsgCount; // UNSIGNED32,
    private long skippedPackets; // UNSIGNED32,
    private long checksumMessages; // UNSIGNED32,
    private long basicQtyMessages; // UNSIGNED32,
    private long basicEnergyMessages; // UNSIGNED32,
    private long harmonicsMessages; // UNSIGNED32,
    private int maxMsgsInAPass; // UNSIGNED8,
    private boolean frontEndRawPresent; // BOOLEAN,
    private long totalHalfSeconds; // UNSIGNED32,
    private long totalSeconds; // UNSIGNED32,
    private long lcdSendQueueFullCount; // UNSIGNED32,
    private long lcdSendErrorCount; // UNSIGNED32,
    private long lcdReceiveErrorCount; // UNSIGNED32,
    private long numberOfRecoveryPowerups; // UNSIGNED32,
    private boolean isInfoManActive; // Boolean,
    private long infoManErrorFlags; // UNSIGNED32,
    private Result infoManLastErrorResult; // RESULT,
    private Result infoManPowerUpResult; // RESULT,
    private String DLMSlastWriteError; // ARRAY[30] of OCTET_STRING
    private String DLMSlastReadError; // ARRAY[30] of OCTET_STRING
    private int lossCompensationLevel; // UNSIGNED8,

    /** Creates a new instance of GeneralDiagnosticInfo */
    public GeneralDiagnosticInfo(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("GeneralDiagnosticInfo:\n");
        strBuff.append("   DLMSlastReadError="+getDLMSlastReadError()+"\n");
        strBuff.append("   DLMSlastWriteError="+getDLMSlastWriteError()+"\n");
        strBuff.append("   DSTfall="+getDSTfall()+"\n");
        strBuff.append("   DSTspring="+getDSTspring()+"\n");
        strBuff.append("   altModeSwitch="+isAltModeSwitch()+"\n");
        strBuff.append("   basicEnergyMessages="+getBasicEnergyMessages()+"\n");
        strBuff.append("   basicQtyMessages="+getBasicQtyMessages()+"\n");
        strBuff.append("   checksumMessages="+getChecksumMessages()+"\n");
        strBuff.append("   currentSession="+getCurrentSession()+"\n");
        strBuff.append("   demandResetSwitch="+isDemandResetSwitch()+"\n");
        strBuff.append("   externalEOICount="+getExternalEOICount()+"\n");
        strBuff.append("   factoryDefaultSwitch="+isFactoryDefaultSwitch()+"\n");
        strBuff.append("   firmwareRevision="+getFirmwareRevision()+"\n");
        strBuff.append("   frontEndMsgCount="+getFrontEndMsgCount()+"\n");
        strBuff.append("   frontEndRawPresent="+isFrontEndRawPresent()+"\n");
        strBuff.append("   harmonicsMessages="+getHarmonicsMessages()+"\n");
        strBuff.append("   infoManErrorFlags="+getInfoManErrorFlags()+"\n");
        strBuff.append("   infoManLastErrorResult="+getInfoManLastErrorResult()+"\n");
        strBuff.append("   infoManPowerUpResult="+getInfoManPowerUpResult()+"\n");
        strBuff.append("   isGlobalEOIActive="+isIsGlobalEOIActive()+"\n");
        strBuff.append("   isGlobalEOIPoweredUp="+isIsGlobalEOIPoweredUp()+"\n");
        strBuff.append("   isGlobalEOIStatusOK="+isIsGlobalEOIStatusOK()+"\n");
        strBuff.append("   isInGracePeriod="+isIsInGracePeriod()+"\n");
        strBuff.append("   isInfoManActive="+isIsInfoManActive()+"\n");
        strBuff.append("   lastCalibrationTime="+getLastCalibrationTime()+"\n");
        strBuff.append("   lastExternalEOI="+getLastExternalEOI()+"\n");
        strBuff.append("   lastGlobalEOIErrorResult="+getLastGlobalEOIErrorResult()+"\n");
        strBuff.append("   lastOutageTime="+getLastOutageTime()+"\n");
        strBuff.append("   lastReprogramTime="+getLastReprogramTime()+"\n");
        strBuff.append("   lastTestModeEntryTime="+getLastTestModeEntryTime()+"\n");
        strBuff.append("   lcdReceiveErrorCount="+getLcdReceiveErrorCount()+"\n");
        strBuff.append("   lcdSendErrorCount="+getLcdSendErrorCount()+"\n");
        strBuff.append("   lcdSendQueueFullCount="+getLcdSendQueueFullCount()+"\n");
        strBuff.append("   lossCompensationLevel="+getLossCompensationLevel()+"\n");
        strBuff.append("   maxMsgsInAPass="+getMaxMsgsInAPass()+"\n");
        strBuff.append("   modemAutoAnswerSwitch="+isModemAutoAnswerSwitch()+"\n");
        strBuff.append("   nextGlobalEOITime="+getNextGlobalEOITime()+"\n");
        strBuff.append("   numberOfActiveSessions="+getNumberOfActiveSessions()+"\n");
        strBuff.append("   numberOfCalibrations="+getNumberOfCalibrations()+"\n");
        strBuff.append("   numberOfCompleteInitializations="+getNumberOfCompleteInitializations()+"\n");
        strBuff.append("   numberOfOutages="+getNumberOfOutages()+"\n");
        strBuff.append("   numberOfProgrammedMassMem="+getNumberOfProgrammedMassMem()+"\n");
        strBuff.append("   numberOfRecoveryPowerups="+getNumberOfRecoveryPowerups()+"\n");
        strBuff.append("   numberOfTestModeEntries="+getNumberOfTestModeEntries()+"\n");
        strBuff.append("   numberOfTimesProgrammed="+getNumberOfTimesProgrammed()+"\n");
        strBuff.append("   programProtectSwitch="+isProgramProtectSwitch()+"\n");
        strBuff.append("   resetReasons="+getResetReasons()+"\n");
        strBuff.append("   scrollLockSwitch="+isScrollLockSwitch()+"\n");
        strBuff.append("   secondsRemainingTilGlobalEOI="+getSecondsRemainingTilGlobalEOI()+"\n");
        for (int i=0;i<getSessionInfos().length;i++) {
            strBuff.append("       sessionInfos["+i+"]="+getSessionInfos()[i]+"\n");
        }
        strBuff.append("   skippedPackets="+getSkippedPackets()+"\n");
        strBuff.append("   testModeSwitch="+isTestModeSwitch()+"\n");
        strBuff.append("   totalHalfSeconds="+getTotalHalfSeconds()+"\n");
        strBuff.append("   totalSeconds="+getTotalSeconds()+"\n");
        strBuff.append("   voltageStatus="+getVoltageStatus()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0036; //54;
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setVoltageStatus(ProtocolUtils.getInt(data,offset++, 1));
        setNumberOfTestModeEntries(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setNumberOfOutages(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setNumberOfTimesProgrammed(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setNumberOfCalibrations(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setLastOutageTime(Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setLastReprogramTime(Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setLastCalibrationTime(Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setLastTestModeEntryTime(Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setDSTspring(Utils.getDateFromDateTime(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeSize();
        setDSTfall(Utils.getDateFromDateTime(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeSize();
        setResetReasons(ProtocolUtils.getInt(data,offset++, 1));
        setFirmwareRevision(new String(ProtocolUtils.getSubArray2(data, offset, 6)));
        offset+=6;
        setScrollLockSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setAltModeSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setTestModeSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setDemandResetSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setFactoryDefaultSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setProgramProtectSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setModemAutoAnswerSwitch(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setCurrentSession(ProtocolUtils.getInt(data,offset++, 1));
        setNumberOfActiveSessions(ProtocolUtils.getInt(data,offset++, 1));
        setSessionInfos(new SessionInfo[6]);
        for (int i=0;i<getSessionInfos().length;i++) {
            getSessionInfos()[i] = new SessionInfo(data,offset);
            offset+=SessionInfo.size();
        }

        setNumberOfProgrammedMassMem(ProtocolUtils.getInt(data,offset++, 1));
        setNumberOfCompleteInitializations(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setIsGlobalEOIActive(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setNextGlobalEOITime(Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setSecondsRemainingTilGlobalEOI(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setIsGlobalEOIPoweredUp(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setIsInGracePeriod(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setLastGlobalEOIErrorResult(new Result(data,offset));
        offset += Result.size();
        setExternalEOICount(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        Date lastExternalEOI = Utils.getDateFromDateTimeExtended(data, offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone());
        offset+=Utils.getDateTimeExtendedSize();
        setIsGlobalEOIStatusOK(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setFrontEndMsgCount(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setSkippedPackets(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setChecksumMessages(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setBasicQtyMessages(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setBasicEnergyMessages(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setHarmonicsMessages(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setMaxMsgsInAPass(ProtocolUtils.getInt(data,offset++, 1));
        setFrontEndRawPresent(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setTotalHalfSeconds(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setTotalSeconds(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setLcdSendQueueFullCount(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setLcdSendErrorCount(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setLcdReceiveErrorCount(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setNumberOfRecoveryPowerups(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setIsInfoManActive(ProtocolUtils.getInt(data,offset++, 1) == 1);
        setInfoManErrorFlags(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        Result infoManLastErrorResult = new Result(data,offset);
        offset += Result.size();
        Result infoManPowerUpResult = new Result(data,offset);
        offset += Result.size();
        String DLMSlastWriteError = new String(ProtocolUtils.getSubArray2(data, offset, 30));
        offset+=30;
        String DLMSlastReadError = new String(ProtocolUtils.getSubArray2(data, offset, 30));
        offset+=30;
        setLossCompensationLevel(ProtocolUtils.getInt(data,offset++, 1));
    }

    public int getVoltageStatus() {
        return voltageStatus;
    }

    public void setVoltageStatus(int voltageStatus) {
        this.voltageStatus = voltageStatus;
    }

    public int getNumberOfTestModeEntries() {
        return numberOfTestModeEntries;
    }

    public void setNumberOfTestModeEntries(int numberOfTestModeEntries) {
        this.numberOfTestModeEntries = numberOfTestModeEntries;
    }

    public int getNumberOfOutages() {
        return numberOfOutages;
    }

    public void setNumberOfOutages(int numberOfOutages) {
        this.numberOfOutages = numberOfOutages;
    }

    public int getNumberOfTimesProgrammed() {
        return numberOfTimesProgrammed;
    }

    public void setNumberOfTimesProgrammed(int numberOfTimesProgrammed) {
        this.numberOfTimesProgrammed = numberOfTimesProgrammed;
    }

    public int getNumberOfCalibrations() {
        return numberOfCalibrations;
    }

    public void setNumberOfCalibrations(int numberOfCalibrations) {
        this.numberOfCalibrations = numberOfCalibrations;
    }

    public Date getLastOutageTime() {
        return lastOutageTime;
    }

    public void setLastOutageTime(Date lastOutageTime) {
        this.lastOutageTime = lastOutageTime;
    }

    public Date getLastReprogramTime() {
        return lastReprogramTime;
    }

    public void setLastReprogramTime(Date lastReprogramTime) {
        this.lastReprogramTime = lastReprogramTime;
    }

    public Date getLastCalibrationTime() {
        return lastCalibrationTime;
    }

    public void setLastCalibrationTime(Date lastCalibrationTime) {
        this.lastCalibrationTime = lastCalibrationTime;
    }

    public Date getLastTestModeEntryTime() {
        return lastTestModeEntryTime;
    }

    public void setLastTestModeEntryTime(Date lastTestModeEntryTime) {
        this.lastTestModeEntryTime = lastTestModeEntryTime;
    }

    public Date getDSTspring() {
        return DSTspring;
    }

    public void setDSTspring(Date DSTspring) {
        this.DSTspring = DSTspring;
    }

    public Date getDSTfall() {
        return DSTfall;
    }

    public void setDSTfall(Date DSTfall) {
        this.DSTfall = DSTfall;
    }

    public int getResetReasons() {
        return resetReasons;
    }

    public void setResetReasons(int resetReasons) {
        this.resetReasons = resetReasons;
    }

    public String getFirmwareRevision() {
        return firmwareRevision;
    }

    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    public boolean isScrollLockSwitch() {
        return scrollLockSwitch;
    }

    public void setScrollLockSwitch(boolean scrollLockSwitch) {
        this.scrollLockSwitch = scrollLockSwitch;
    }

    public boolean isAltModeSwitch() {
        return AltModeSwitch;
    }

    public void setAltModeSwitch(boolean AltModeSwitch) {
        this.AltModeSwitch = AltModeSwitch;
    }

    public boolean isTestModeSwitch() {
        return TestModeSwitch;
    }

    public void setTestModeSwitch(boolean TestModeSwitch) {
        this.TestModeSwitch = TestModeSwitch;
    }

    public boolean isDemandResetSwitch() {
        return DemandResetSwitch;
    }

    public void setDemandResetSwitch(boolean DemandResetSwitch) {
        this.DemandResetSwitch = DemandResetSwitch;
    }

    public boolean isFactoryDefaultSwitch() {
        return FactoryDefaultSwitch;
    }

    public void setFactoryDefaultSwitch(boolean FactoryDefaultSwitch) {
        this.FactoryDefaultSwitch = FactoryDefaultSwitch;
    }

    public boolean isProgramProtectSwitch() {
        return ProgramProtectSwitch;
    }

    public void setProgramProtectSwitch(boolean ProgramProtectSwitch) {
        this.ProgramProtectSwitch = ProgramProtectSwitch;
    }

    public boolean isModemAutoAnswerSwitch() {
        return ModemAutoAnswerSwitch;
    }

    public void setModemAutoAnswerSwitch(boolean ModemAutoAnswerSwitch) {
        this.ModemAutoAnswerSwitch = ModemAutoAnswerSwitch;
    }

    public int getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(int currentSession) {
        this.currentSession = currentSession;
    }

    public int getNumberOfActiveSessions() {
        return numberOfActiveSessions;
    }

    public void setNumberOfActiveSessions(int numberOfActiveSessions) {
        this.numberOfActiveSessions = numberOfActiveSessions;
    }

    public SessionInfo[] getSessionInfos() {
        return sessionInfos;
    }

    public void setSessionInfos(SessionInfo[] sessionInfos) {
        this.sessionInfos = sessionInfos;
    }

    public int getNumberOfProgrammedMassMem() {
        return numberOfProgrammedMassMem;
    }

    public void setNumberOfProgrammedMassMem(int numberOfProgrammedMassMem) {
        this.numberOfProgrammedMassMem = numberOfProgrammedMassMem;
    }

    public int getNumberOfCompleteInitializations() {
        return numberOfCompleteInitializations;
    }

    public void setNumberOfCompleteInitializations(int numberOfCompleteInitializations) {
        this.numberOfCompleteInitializations = numberOfCompleteInitializations;
    }

    public boolean isIsGlobalEOIActive() {
        return isGlobalEOIActive;
    }

    public void setIsGlobalEOIActive(boolean isGlobalEOIActive) {
        this.isGlobalEOIActive = isGlobalEOIActive;
    }

    public Date getNextGlobalEOITime() {
        return nextGlobalEOITime;
    }

    public void setNextGlobalEOITime(Date nextGlobalEOITime) {
        this.nextGlobalEOITime = nextGlobalEOITime;
    }

    public int getSecondsRemainingTilGlobalEOI() {
        return secondsRemainingTilGlobalEOI;
    }

    public void setSecondsRemainingTilGlobalEOI(int secondsRemainingTilGlobalEOI) {
        this.secondsRemainingTilGlobalEOI = secondsRemainingTilGlobalEOI;
    }

    public boolean isIsGlobalEOIPoweredUp() {
        return isGlobalEOIPoweredUp;
    }

    public void setIsGlobalEOIPoweredUp(boolean isGlobalEOIPoweredUp) {
        this.isGlobalEOIPoweredUp = isGlobalEOIPoweredUp;
    }

    public boolean isIsInGracePeriod() {
        return isInGracePeriod;
    }

    public void setIsInGracePeriod(boolean isInGracePeriod) {
        this.isInGracePeriod = isInGracePeriod;
    }

    public Result getLastGlobalEOIErrorResult() {
        return lastGlobalEOIErrorResult;
    }

    public void setLastGlobalEOIErrorResult(Result lastGlobalEOIErrorResult) {
        this.lastGlobalEOIErrorResult = lastGlobalEOIErrorResult;
    }

    public long getExternalEOICount() {
        return externalEOICount;
    }

    public void setExternalEOICount(long externalEOICount) {
        this.externalEOICount = externalEOICount;
    }

    public Date getLastExternalEOI() {
        return lastExternalEOI;
    }

    public void setLastExternalEOI(Date lastExternalEOI) {
        this.lastExternalEOI = lastExternalEOI;
    }

    public boolean isIsGlobalEOIStatusOK() {
        return isGlobalEOIStatusOK;
    }

    public void setIsGlobalEOIStatusOK(boolean isGlobalEOIStatusOK) {
        this.isGlobalEOIStatusOK = isGlobalEOIStatusOK;
    }

    public long getFrontEndMsgCount() {
        return frontEndMsgCount;
    }

    public void setFrontEndMsgCount(long frontEndMsgCount) {
        this.frontEndMsgCount = frontEndMsgCount;
    }

    public long getSkippedPackets() {
        return skippedPackets;
    }

    public void setSkippedPackets(long skippedPackets) {
        this.skippedPackets = skippedPackets;
    }

    public long getChecksumMessages() {
        return checksumMessages;
    }

    public void setChecksumMessages(long checksumMessages) {
        this.checksumMessages = checksumMessages;
    }

    public long getBasicQtyMessages() {
        return basicQtyMessages;
    }

    public void setBasicQtyMessages(long basicQtyMessages) {
        this.basicQtyMessages = basicQtyMessages;
    }

    public long getBasicEnergyMessages() {
        return basicEnergyMessages;
    }

    public void setBasicEnergyMessages(long basicEnergyMessages) {
        this.basicEnergyMessages = basicEnergyMessages;
    }

    public long getHarmonicsMessages() {
        return harmonicsMessages;
    }

    public void setHarmonicsMessages(long harmonicsMessages) {
        this.harmonicsMessages = harmonicsMessages;
    }

    public int getMaxMsgsInAPass() {
        return maxMsgsInAPass;
    }

    public void setMaxMsgsInAPass(int maxMsgsInAPass) {
        this.maxMsgsInAPass = maxMsgsInAPass;
    }

    public boolean isFrontEndRawPresent() {
        return frontEndRawPresent;
    }

    public void setFrontEndRawPresent(boolean frontEndRawPresent) {
        this.frontEndRawPresent = frontEndRawPresent;
    }

    public long getTotalHalfSeconds() {
        return totalHalfSeconds;
    }

    public void setTotalHalfSeconds(long totalHalfSeconds) {
        this.totalHalfSeconds = totalHalfSeconds;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public long getLcdSendQueueFullCount() {
        return lcdSendQueueFullCount;
    }

    public void setLcdSendQueueFullCount(long lcdSendQueueFullCount) {
        this.lcdSendQueueFullCount = lcdSendQueueFullCount;
    }

    public long getLcdSendErrorCount() {
        return lcdSendErrorCount;
    }

    public void setLcdSendErrorCount(long lcdSendErrorCount) {
        this.lcdSendErrorCount = lcdSendErrorCount;
    }

    public long getLcdReceiveErrorCount() {
        return lcdReceiveErrorCount;
    }

    public void setLcdReceiveErrorCount(long lcdReceiveErrorCount) {
        this.lcdReceiveErrorCount = lcdReceiveErrorCount;
    }

    public long getNumberOfRecoveryPowerups() {
        return numberOfRecoveryPowerups;
    }

    public void setNumberOfRecoveryPowerups(long numberOfRecoveryPowerups) {
        this.numberOfRecoveryPowerups = numberOfRecoveryPowerups;
    }

    public boolean isIsInfoManActive() {
        return isInfoManActive;
    }

    public void setIsInfoManActive(boolean isInfoManActive) {
        this.isInfoManActive = isInfoManActive;
    }

    public long getInfoManErrorFlags() {
        return infoManErrorFlags;
    }

    public void setInfoManErrorFlags(long infoManErrorFlags) {
        this.infoManErrorFlags = infoManErrorFlags;
    }

    public Result getInfoManLastErrorResult() {
        return infoManLastErrorResult;
    }

    public void setInfoManLastErrorResult(Result infoManLastErrorResult) {
        this.infoManLastErrorResult = infoManLastErrorResult;
    }

    public Result getInfoManPowerUpResult() {
        return infoManPowerUpResult;
    }

    public void setInfoManPowerUpResult(Result infoManPowerUpResult) {
        this.infoManPowerUpResult = infoManPowerUpResult;
    }

    public String getDLMSlastWriteError() {
        return DLMSlastWriteError;
    }

    public void setDLMSlastWriteError(String DLMSlastWriteError) {
        this.DLMSlastWriteError = DLMSlastWriteError;
    }

    public String getDLMSlastReadError() {
        return DLMSlastReadError;
    }

    public void setDLMSlastReadError(String DLMSlastReadError) {
        this.DLMSlastReadError = DLMSlastReadError;
    }

    public int getLossCompensationLevel() {
        return lossCompensationLevel;
    }

    public void setLossCompensationLevel(int lossCompensationLevel) {
        this.lossCompensationLevel = lossCompensationLevel;
    }
}
