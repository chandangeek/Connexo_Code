package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.*;

/** @author fbo */

public class ABBA230RegisterFactory {
    
    static public final int MAX_CMD_REGS=2;
    static public final int MAX_MD_REGS=2;
    
    private Map registers = new TreeMap();
    private ProtocolLink protocolLink;
    private MeterExceptionInfo meterExceptionInfo;
    private ABBA230DataIdentityFactory dataIdentityFactory;
    private DataType dataType;
    
    private ABBA230Register cTPrimary;
    private ABBA230Register cTPrimaryAndSecundary;
    private ABBA230Register cTSecundary;
    private ABBA230Register cummMainvarhImport;
    private ABBA230Register cummMainvarhExport;
    private ABBA230Register cummMainExport;
    private ABBA230Register cummMainImport;
    private ABBA230Register cummMainQ1;
    private ABBA230Register cummMainQ2;
    private ABBA230Register cummMainQ3;
    private ABBA230Register cummMainQ4;
    private ABBA230Register cummMainVAExport;
    private ABBA230Register cummMainVAImport;
    private ABBA230Register cummulativeMaximumDemand;
    private ABBA230Register cummulativeRegisters;
    private ABBA230Register cumulativeMaximumDemand0;
    private ABBA230Register cumulativeMaximumDemand1;
    private ABBA230Register cumulativeMaximumDemand2;
    private ABBA230Register cumulativeMaximumDemand3;
    private ABBA230Register custDefRegConfig;
    //private ABBA230Register historicalEvents;
    private ABBA230Register historicalRegister;
    private ABBA230Register dailyHistoricalRegister;
    private ABBA230Register historicalSystemStatus;

    private ABBA230Register loadProfile;
    private ABBA230Register loadProfile256Blocks;
    private ABBA230Register loadProfile64Blocks;
    private ABBA230Register loadProfileConfiguration;
    private ABBA230Register loadProfileReadByDate;
    private ABBA230Register loadProfileByDate64Blocks;
    private ABBA230Register loadProfileSet;
    private ABBA230Register loadProfileDSTConfig;
    private ABBA230Register loadProfileIntegrationPeriod;

    private ABBA230Register instrumentationProfile ;
    private ABBA230Register instrumentationProfile256Blocks ;
    private ABBA230Register instrumentationProfile64Blocks ;
    private ABBA230Register instrumentationProfileConfiguration;
    private ABBA230Register instrumentationProfileReadByDate ;
    private ABBA230Register instrumentationProfileByDate64Blocks ;
    private ABBA230Register instrumentationProfileSet ;
    private ABBA230Register instrumentationProfileDSTConfig;
    private ABBA230Register instrumentationProfileIntegrationPeriod;

    private ABBA230Register maximumDemandRegisters;
    private ABBA230Register maximumDemand0;
    private ABBA230Register maximumDemand1;
    private ABBA230Register schemeID;
    private ABBA230Register serialNumber;
    private ABBA230Register systemStatusDataIdentity;
    private ABBA230Register tariffSources;
    private ABBA230Register timeDate;
    private ABBA230Register timeOfUse0;
    private ABBA230Register timeOfUse1;
    private ABBA230Register timeOfUse2;
    private ABBA230Register timeOfUse3;
    private ABBA230Register timeOfUse4;
    private ABBA230Register timeOfUse5;
    private ABBA230Register timeOfUse6;
    private ABBA230Register timeOfUse7;
    private ABBA230Register timeOfUse8;
    private ABBA230Register timeOfUse9;
    private ABBA230Register timeOfUse10;
    private ABBA230Register timeOfUse11;
    private ABBA230Register timeOfUse12;
    private ABBA230Register timeOfUse13;
    private ABBA230Register timeOfUse14;
    private ABBA230Register timeOfUse15;
    private ABBA230Register contactorStatus,contactorCloser;

    private ABBA230Register resetRegister;
    private ABBA230Register endOfBillingPeriod;
    
    private ABBA230Register overVoltageEventLog;
    private ABBA230Register underVoltageEventLog;
    private ABBA230Register programmingEventLog;
    private ABBA230Register longPowerFailEventLog;
    private ABBA230Register terminalCoverEventLog;
    private ABBA230Register mainCoverEventLog;
    private ABBA230Register magneticTamperEventLog;
    private ABBA230Register reverserunEventLog;
    private ABBA230Register powerFailEventLog;
    private ABBA230Register transientEventLog;
    private ABBA230Register endOfBillingEventLog;
    
    private ABBA230Register contactorOpenOpticalLog;
    private ABBA230Register contactorOpenModuleLog;
    private ABBA230Register contactorOpenLoadMonitorLowEventLog;
    private ABBA230Register contactorOpenLoadMonitorHighEventLog;
    private ABBA230Register contactorOpenAutoDisconnectEventLog;
    private ABBA230Register contactorArmOpticalEventLog;
    private ABBA230Register contactorArmModuleEventLog;
    private ABBA230Register contactorArmLoadMonitorEventLog;
    private ABBA230Register contactorArmDisconnectEventLog;
    private ABBA230Register contactorCloseOpticalEventLog;
    private ABBA230Register contactorCloseModuleEventLog;
    private ABBA230Register contactorCloseButtonEventLog;
    
    
    private ABBA230Register meterErrorEventLog;
    private ABBA230Register batteryVoltageLowEventLog;
    private ABBA230Register dspFWVersion;
    private ABBA230Register applFWVersion;
    
    private static final String cummulativeMainId = "507";
    private static final String timeOfUseId = "508";
    private static final String cummulativeMaxDemandId = "509";
    
    private static Unit instrumentationChannelUnitMapping[] = {Unit.get(BaseUnit.AMPERE, -1),
            Unit.get(BaseUnit.VOLT, -1),
            Unit.get(BaseUnit.UNITLESS, 0),
            Unit.get(BaseUnit.WATT, 0),
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 0),
            Unit.get(BaseUnit.VOLTAMPERE, 0),
            Unit.get(BaseUnit.UNITLESS, 0),
            Unit.get(BaseUnit.HERTZ, -1),
            Unit.get(BaseUnit.DEGREE, -1),
            Unit.get(BaseUnit.AMPERE, -1),
            Unit.get(BaseUnit.VOLT, -1),
            Unit.get(BaseUnit.WATT, 0),
            Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 0),
            Unit.get(BaseUnit.VOLTAMPERE, 0)
    };

    private static String instrumentationChannelNameMapping[] = {"RMS current (secondary)",
            "RMS volts (secondary)",
            "Power Factor",
            "Active power (secondary)",
            "Reactive power (secondary)",
            "Apparant power (secondary)",
            "Phase rotation",
            "Frequency",
            "Phase angle",
            "RMS current (primary)",
            "RMS volts (primary)",
            "Active power (primary)",
            "Reactive power (primary)",
            "Apparant power (primary)"
    };

    SystemStatus systemStatus=null;
    ABBA230 abba230;
    
    /**
     * Creates a new instance of ABBA230RegisterFactory
     * @param abba230
     * @param meterExceptionInfo
     */
    public ABBA230RegisterFactory(
    		ABBA230 abba230, MeterExceptionInfo meterExceptionInfo ) {
        
        this.protocolLink = (ProtocolLink)abba230;
        this.meterExceptionInfo = meterExceptionInfo;
        this.dataType = new DataType(protocolLink.getTimeZone());
        this.dataIdentityFactory = new ABBA230DataIdentityFactory(protocolLink,meterExceptionInfo);
        initRegisters();
        this.abba230=abba230;
        
    }
    
    protected ABBA230DataIdentityFactory getABBA230DataIdentityFactory() {
        return dataIdentityFactory;
    }
    
    protected ProtocolLink getProtocolLink() {
        return protocolLink;
    }
    
    protected Map getRegisters() {
        return registers;
    }
    
    DataType getDataType(){
        return dataType;
    }
    
    public ABBA230Register getCTPrimary() {
        return cTPrimary;
    }

    public ABBA230Register getCTPrimaryAndSecundary() {
        return cTPrimaryAndSecundary;
    }

    public ABBA230Register getCTSecundary() {
        return cTSecundary;
    }


    public ABBA230Register getCummMainExport() {
        return cummMainExport;
    }

    public ABBA230Register getCummMainImport() {
        return cummMainImport;
    }

    public ABBA230Register getCummMainQ1() {
        return cummMainQ1;
    }

    public ABBA230Register getCummMainQ2() {
        return cummMainQ2;
    }

    public ABBA230Register getCummMainQ3() {
        return cummMainQ3;
    }

    public ABBA230Register getCummMainQ4() {
        return cummMainQ4;
    }

    public ABBA230Register getCummMainVAExport() {
        return cummMainVAExport;
    }

    public ABBA230Register getCummMainVAImport() {
        return cummMainVAImport;
    }

    public ABBA230Register getCummulativeMaximumDemand() {
        return cummulativeMaximumDemand;
    }

    public ABBA230Register getCummulativeRegisters() {
        return cummulativeRegisters;
    }

    public ABBA230Register getCumulativeMaximumDemand0() {
        return cumulativeMaximumDemand0;
    }

    public ABBA230Register getCumulativeMaximumDemand1() {
        return cumulativeMaximumDemand1;
    }

    public ABBA230Register getCumulativeMaximumDemand2() {
        return cumulativeMaximumDemand2;
    }

    public ABBA230Register getCumulativeMaximumDemand3() {
        return cumulativeMaximumDemand3;
    }

    public ABBA230Register getCustDefRegConfig() {
        return custDefRegConfig;
    }

//    public ABBA230Register getHistoricalEvents() {
//        return historicalEvents;
//    }

    public ABBA230Register getHistoricalRegister() {
        return historicalRegister;
    }

    public ABBA230Register getHistoricalSystemStatus() {
        return historicalSystemStatus;
    }

    public ABBA230Register getLoadProfileIntegrationPeriod() {
        return loadProfileIntegrationPeriod;
    }

    public ABBA230Register getLoadProfile() {
        return loadProfile;
    }

    public ABBA230Register getLoadProfile256Blocks() {
        return loadProfile256Blocks;
    }

    public ABBA230Register getLoadProfile64Blocks() {
        return loadProfile64Blocks;
    }

    public ABBA230Register getLoadProfileConfiguration() {
        return loadProfileConfiguration;
    }

    public ABBA230Register getLoadProfileReadByDate() {
        return loadProfileReadByDate;
    }

    public ABBA230Register getLoadProfileSet() {
        return loadProfileSet;
    }

    // Getters for InstrumentationProfile and settings
    public ABBA230Register getInstrumentationProfile() {
        return instrumentationProfile;
    }

    public ABBA230Register getInstrumentationProfile256Blocks() {
        return instrumentationProfile256Blocks;
    }

    public ABBA230Register getInstrumentationProfile64Blocks() {
        return instrumentationProfile64Blocks;
    }

    public ABBA230Register getInstrumentationProfileConfiguration() {
        return instrumentationProfileConfiguration;
    }

    public ABBA230Register getInstrumentationProfileReadByDate() {
        return instrumentationProfileReadByDate;
    }

    public ABBA230Register getInstrumentationProfileByDate64Blocks() {
        return instrumentationProfileByDate64Blocks;
    }

    public ABBA230Register getInstrumentationProfileSet() {
        return instrumentationProfileSet;
    }

    public ABBA230Register getInstrumentationProfileDSTConfig() {
        return instrumentationProfileDSTConfig;
    }

    public ABBA230Register getInstrumentationProfileIntegrationPeriod() {
        return instrumentationProfileIntegrationPeriod;
    }

    public ABBA230Register getMaximumDemand0() {
        return maximumDemand0;
    }

    public ABBA230Register getMaximumDemand1() {
        return maximumDemand1;
    }

    public ABBA230Register getMaximumDemandRegisters() {
        return maximumDemandRegisters;
    }

    public ABBA230Register getSchemeID() {
        return schemeID;
    }

    public ABBA230Register getSerialNumber() {
        return serialNumber;
    }

    public ABBA230Register getTariffSources() {
        return tariffSources;
    }

    public ABBA230Register getTimeDate() {
        return timeDate;
    }

    public ABBA230Register getTimeOfUse0() {
        return timeOfUse0;
    }

    public ABBA230Register getTimeOfUse1() {
        return timeOfUse1;
    }

    public ABBA230Register getTimeOfUse2() {
        return timeOfUse2;
    }

    public ABBA230Register getTimeOfUse3() {
        return timeOfUse3;
    }

    public ABBA230Register getTimeOfUse4() {
        return timeOfUse4;
    }

    public ABBA230Register getTimeOfUse5() {
        return timeOfUse5;
    }

    public ABBA230Register getTimeOfUse6() {
        return timeOfUse6;
    }

    public ABBA230Register getTimeOfUse7() {
        return timeOfUse7;
    }
    
    // length = -1, not used
    private void initRegisters() {
        
        Unit mWh = Unit.get(BaseUnit.WATTHOUR,-3);
        Unit mvarh = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3);
        Unit mvah = Unit.get(BaseUnit.VOLTAMPEREHOUR, -3);
        
        serialNumber = cr("798", "SerialNumber", ABBA230RegisterData.ABBA_STRING,0, -1,null );
        schemeID = cr("795", "SchemeID", ABBA230RegisterData.ABBA_STRING,0,8, null );
        timeDate = cr("861", "TimeDate", ABBA230RegisterData.ABBA_DATE,0,-1, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        
        cummulativeRegisters = cr("507", "CummulativeRegisters", ABBA230RegisterData.ABBA_BYTEARRAY,0,-1, null );
        
        cummMainImport = cr(cummulativeMainId, "CummMainImport", ABBA230RegisterData.ABBA_REGISTER,0,8,mWh);
        cummMainExport = cr(cummulativeMainId, "CummMainExport", ABBA230RegisterData.ABBA_REGISTER,8,8,mWh);
        cummMainQ1 = cr(cummulativeMainId, "CummMainQ1", ABBA230RegisterData.ABBA_REGISTER,16,8,mvarh);
        cummMainQ2 = cr(cummulativeMainId, "CummMainQ2", ABBA230RegisterData.ABBA_REGISTER,24,8,mvarh);
        cummMainQ3 = cr(cummulativeMainId, "CummMainQ3", ABBA230RegisterData.ABBA_REGISTER,32,8,mvarh);
        cummMainQ4 = cr(cummulativeMainId, "CummMainQ4", ABBA230RegisterData.ABBA_REGISTER,40,8,mvarh);
        cummMainVAImport = cr(cummulativeMainId, "CummMainVAImport", ABBA230RegisterData.ABBA_REGISTER,48,8,mvah);
        cummMainVAExport = cr(cummulativeMainId, "CummMainVAExport", ABBA230RegisterData.ABBA_REGISTER,56,8,mvah);
        // reserved for future use
        cummMainvarhImport = cr(cummulativeMainId, "CummMainvarhImport", ABBA230RegisterData.ABBA_REGISTER,112,8,mvarh);
        cummMainvarhExport = cr(cummulativeMainId, "CummMainvarhExport", ABBA230RegisterData.ABBA_REGISTER,120,8,mvarh);
        
        timeOfUse0 = cr(timeOfUseId, "TimeOfUse0", ABBA230RegisterData.ABBA_REGISTER,0,8,null);
        timeOfUse1 = cr(timeOfUseId, "TimeOfUse1", ABBA230RegisterData.ABBA_REGISTER,8,8,null);
        timeOfUse2 = cr(timeOfUseId, "TimeOfUse2", ABBA230RegisterData.ABBA_REGISTER,16,8,null);
        timeOfUse3 = cr(timeOfUseId, "TimeOfUse3", ABBA230RegisterData.ABBA_REGISTER,24,8,null);
        timeOfUse4 = cr(timeOfUseId, "TimeOfUse4", ABBA230RegisterData.ABBA_REGISTER,32,8,null);
        timeOfUse5 = cr(timeOfUseId, "TimeOfUse5", ABBA230RegisterData.ABBA_REGISTER,40,8,null);
        timeOfUse6 = cr(timeOfUseId, "TimeOfUse6", ABBA230RegisterData.ABBA_REGISTER,48,8,null);
        timeOfUse7 = cr(timeOfUseId, "TimeOfUse7", ABBA230RegisterData.ABBA_REGISTER,56,8,null);
        timeOfUse8 = cr(timeOfUseId, "TimeOfUse8", ABBA230RegisterData.ABBA_REGISTER,64,8,null);
        timeOfUse9 = cr(timeOfUseId, "TimeOfUse9", ABBA230RegisterData.ABBA_REGISTER,72,8,null);
        timeOfUse10 = cr(timeOfUseId, "TimeOfUse10", ABBA230RegisterData.ABBA_REGISTER,80,8,null);
        timeOfUse11 = cr(timeOfUseId, "TimeOfUse11", ABBA230RegisterData.ABBA_REGISTER,88,8,null);
        timeOfUse12 = cr(timeOfUseId, "TimeOfUse12", ABBA230RegisterData.ABBA_REGISTER,96,8,null);
        timeOfUse13 = cr(timeOfUseId, "TimeOfUse13", ABBA230RegisterData.ABBA_REGISTER,104,8,null);
        timeOfUse14 = cr(timeOfUseId, "TimeOfUse14", ABBA230RegisterData.ABBA_REGISTER,112,8,null);
        timeOfUse15 = cr(timeOfUseId, "TimeOfUse15", ABBA230RegisterData.ABBA_REGISTER,120,8,null);
        
        cummulativeMaximumDemand = cr(cummulativeMaxDemandId, "CummulativeMaximumDemand", ABBA230RegisterData.ABBA_BYTEARRAY,0,-1, null );
        cumulativeMaximumDemand0 = cr(cummulativeMaxDemandId,"CumulativeMaximumDemand0", ABBA230RegisterData.ABBA_CMD,0,9,null);
        cumulativeMaximumDemand1 = cr(cummulativeMaxDemandId,"CumulativeMaximumDemand1", ABBA230RegisterData.ABBA_CMD,9,9,null);
        cumulativeMaximumDemand2 = cr(cummulativeMaxDemandId,"CumulativeMaximumDemand2", ABBA230RegisterData.ABBA_CMD,18,9,null);
        cumulativeMaximumDemand3 = cr(cummulativeMaxDemandId,"CumulativeMaximumDemand3", ABBA230RegisterData.ABBA_CMD,27,9,null);
        
        maximumDemandRegisters = cr("510", "MaximumDemandRegisters", ABBA230RegisterData.ABBA_BYTEARRAY,0,24, null );
        maximumDemand0 = cr( "510", "MaximumDemand0", ABBA230RegisterData.ABBA_MD,0,12,null);
        maximumDemand1 = cr( "510", "MaximumDemand1", ABBA230RegisterData.ABBA_MD,12,12,null);
        
        historicalRegister = cr("543", "HistoricalRegister", ABBA230RegisterData.ABBA_HISTORICALVALUES,0,302, null);
        
        dailyHistoricalRegister = cr("545", "DailyHistoricalRegister", ABBA230RegisterData.ABBA_HISTORICALVALUES,0,302, null);
        

        // event logs
        overVoltageEventLog = cr("678", "OverVoltageEventLog", ABBA230RegisterData.ABBA_OVERVOLTAGEEVENTLOG,0,83, null);
        underVoltageEventLog = cr("679", "UnderVoltageEventLog", ABBA230RegisterData.ABBA_UNDERVOLTAGEEVENTLOG,0,83, null);
        programmingEventLog = cr("680", "ProgrammingEventLog", ABBA230RegisterData.ABBA_PROGRAMMINGEVENTLOG,0,64+64+45, null);
        longPowerFailEventLog = cr("685", "LongPowerFailEventLog", ABBA230RegisterData.ABBA_LONGPOWERFAILEVENTLOG,0,83, null);
        powerFailEventLog = cr("695", "PowerFailEventLog", ABBA230RegisterData.ABBA_POWEREFAILEVENTLOG,0,83, null);
        terminalCoverEventLog = cr("691", "TerminalCoverEventLog", ABBA230RegisterData.ABBA_TERMINALCOVEREVENTLOG,0,83, null);
        mainCoverEventLog = cr("692", "MainCoverEventLog", ABBA230RegisterData.ABBA_MAINCOVEREVENTLOG,0,83, null);
        magneticTamperEventLog = cr("693", "MagneticTamperEventLog", ABBA230RegisterData.ABBA_MAGNETICTAMPEREVENTLOG,0,83, null);
        reverserunEventLog = cr("694", "ReverserunEventLog", ABBA230RegisterData.ABBA_REVERSERUNEVENTLOG,0,43, null);
        transientEventLog = cr("696", "TransientEventLog", ABBA230RegisterData.ABBA_TRANSIENTEVENTLOG,0,43, null);
        endOfBillingEventLog = cr("699", "EndOfBillingEventLog", ABBA230RegisterData.ABBA_ENDOFBILLINGEVENTLOG,0,53, null);
        contactorOpenOpticalLog = cr("422", "ContactorOpenOpticalLog", ABBA230RegisterData.ABBA_CONTACTOROPENOPTICALLOG,0,53, null);
        contactorOpenModuleLog = cr("423", "ContactorOpenModuleLog", ABBA230RegisterData.ABBA_CONTACTOROPENMODULELOG,0,53, null);
        contactorOpenLoadMonitorLowEventLog = cr("424", "ContactorOpenLoadMonitorLowEventLog", ABBA230RegisterData.ABBA_CONTACTORLOADMONITORLOWLOG,0,53, null);
        contactorOpenLoadMonitorHighEventLog = cr("425", "ContactorOpenLoadMonitorHighEventLog", ABBA230RegisterData.ABBA_CONTACTOROPENLOADMONITORHIGHLOG,0,53, null);
        contactorOpenAutoDisconnectEventLog = cr("426", "ContactorOpenAutoDisconnectEventLog", ABBA230RegisterData.ABBA_CONTACTOROPENAUTODISCONNECTLOG,0,53, null);
        contactorArmOpticalEventLog = cr("427", "ContactorArmOpticalEventLog", ABBA230RegisterData.ABBA_CONTACTORARMOPTICALLOG,0,53, null);
        contactorArmModuleEventLog = cr("428", "ContactorArmModuleEventLog", ABBA230RegisterData.ABBA_CONTACTORARMMODULELOG,0,53, null);
        contactorArmLoadMonitorEventLog = cr("429", "ContactorArmLoadMonitorEventLog", ABBA230RegisterData.ABBA_CONTACTORARMLOADMONITORLOG,0,53, null);
        contactorArmDisconnectEventLog = cr("430", "ContactorArmDisconnectEventLog", ABBA230RegisterData.ABBA_CONTACTORARMDISCONNECTLOG,0,53, null);
        contactorCloseOpticalEventLog = cr("431", "ContactorCloseOpticalEventLog", ABBA230RegisterData.ABBA_CONTACTORCLOSEOPTICALLOG,0,53, null);
        contactorCloseModuleEventLog = cr("432", "ContactorCloseModuleEventLog", ABBA230RegisterData.ABBA_CONTACTORCLOSEMODULELOG,0,53, null);
        contactorCloseButtonEventLog = cr("433", "ContactorCloseButtonEventLog", ABBA230RegisterData.ABBA_CONTACTORCLOSEBUTTONLOG,0,53, null);
        meterErrorEventLog = cr("701", "MeterErrorEventLog", ABBA230RegisterData.ABBA_METERERROREVENTLOG,0,53, null);
        batteryVoltageLowEventLog = cr("705", "BatteryVoltageLowEventLog", ABBA230RegisterData.ABBA_BATTERYVOLTAGELOWEVENTLOG,0,43, null);

        /* Load Profile */
        loadProfile = cr("550", "LoadProfile", ABBA230RegisterData.ABBA_BYTEARRAY,0,-1, null);
        /* The 2 ways to specifiy how much load profile data to retrieve:
         * 551: nr of days
         * 554: between from and to */
        loadProfileSet = cr("551", "LoadProfileSet", ABBA230RegisterData.ABBA_HEX_LE,0,2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        loadProfile64Blocks = cr("551", "LoadProfile64Blocks", ABBA230RegisterData.ABBA_HEX,0,2, null);
        loadProfile256Blocks = cr("551", "LoadProfile256Blocks", ABBA230RegisterData.ABBA_HEX,2,2, null);
        
        loadProfileReadByDate = cr("554", "LoadProfileReadByDate", ABBA230RegisterData.ABBA_LOAD_PROFILE_BY_DATE,0, 2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        loadProfileByDate64Blocks = cr("554", "LoadProfileByDate64Blocks", ABBA230RegisterData.ABBA_HEX,0,2, null);
        
        loadProfileConfiguration = cr("777", "LoadProfileConfiguration", ABBA230RegisterData.ABBA_LOAD_PROFILE_CONFIG,0,2, null);
        loadProfileIntegrationPeriod = cr("878", "LoadProfileIntegrationPeriod", ABBA230RegisterData.ABBA_LOAD_PROFILE_INTEGRATION_PERIOD,0,1, null);
        loadProfileDSTConfig = cr("778", "LoadProfileDSTConfig", ABBA230RegisterData.ABBA_HEX,0,1, null);
        /* ------ */

        systemStatusDataIdentity = cr("724", "SystemStatus", ABBA230RegisterData.ABBA_SYSTEMSTATUS,0,13, null);
        
        custDefRegConfig = cr("600", "CustDefRegConfig", ABBA230RegisterData.ABBA_CUSTDEFREGCONFIG,0,4, null);
        
        tariffSources = cr("667", "TariffSources", ABBA230RegisterData.ABBA_TARIFFSOURCES,0,16, null );
        
        cTPrimaryAndSecundary = cr("616", "CTPrimaryAndSecundary", ABBA230RegisterData.ABBA_STRING,0,-1, null);
        cTPrimary = cr("616", "CTPrimary", ABBA230RegisterData.ABBA_BIGDECIMAL,0,4, Unit.get(BaseUnit.UNITLESS,-2));
        cTSecundary = cr("616", "CTSecundary", ABBA230RegisterData.ABBA_BIGDECIMAL,4,2, Unit.get(BaseUnit.UNITLESS,-2));
        
        contactorStatus = cr("411", "ContactorStatus", ABBA230RegisterData.ABBA_HEX,0,1, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        contactorCloser = cr("412", "ContactorCloser", ABBA230RegisterData.ABBA_HEX,0,1, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);

        resetRegister = cr("099", "ResetRegister", ABBA230RegisterData.ABBA_HEX, 0, 1, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        endOfBillingPeriod = cr("655", "EndOfBillingPeriod", ABBA230RegisterData.ABBA_HEX, 0, 1, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        
        /* Instrumentation Profile */
        instrumentationProfile = cr("555", "InstrumentationProfile", ABBA230RegisterData.ABBA_BYTEARRAY, 0, -1, null);
         /* The 2 ways to specify how much Instrumentation profile data to retrieve:
         * 556: nr of days
         * 558: between from and to */
        instrumentationProfileSet = cr("556", "InstrumentationProfileSet", ABBA230RegisterData.ABBA_HEX_LE, 0, 2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        instrumentationProfile64Blocks = cr("556", "InstrumentationProfile64Blocks", ABBA230RegisterData.ABBA_HEX, 0, 2, null);
        instrumentationProfile256Blocks = cr("556", "InstrumentationProfile256Blocks", ABBA230RegisterData.ABBA_HEX, 2, 2, null);

        instrumentationProfileReadByDate = cr("558", "InstrumentationProfileReadByDate", ABBA230RegisterData.ABBA_INSTRUMENTATION_PROFILE_BY_DATE, 0, 2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        instrumentationProfileByDate64Blocks = cr("558", "InstrumentationProfileByDate64Blocks", ABBA230RegisterData.ABBA_HEX, 0, 2, null);

        instrumentationProfileConfiguration = cr("775", "InstrumentationProfileConfiguration", ABBA230RegisterData.ABBA_INSTRUMENTATION_PROFILE_CONFIG, 0, 16, null);
        instrumentationProfileIntegrationPeriod = cr("879", "InstrumentationProfileIntegrationPeriod", ABBA230RegisterData.ABBA_INSTUMENTATION_PROFILE_INTEGRATION_PERIOD, 0, 1, null);
        instrumentationProfileDSTConfig = cr("776", "InstrumentationProfileDSTConfig", ABBA230RegisterData.ABBA_HEX, 0, 1, null);
        /* ------ */
    }
    
    /** factory method for ABBARegisters */
    private ABBA230Register cr(
            String id, String name, int type, int offset, int length, Unit unit ){
        
        ABBA230Register register =
                new ABBA230Register( id, name, type, offset, length, unit,
                ABBA230Register.NOT_WRITEABLE, ABBA230Register.CACHED, this );

        registers.put( name, register );
        return register;
    }
    
    /** factory method for ABBARegisters */
    private ABBA230Register cr(
            String id, String name, int type, int offset, int length, Unit unit,
            boolean writeable, boolean cached ){
        
        ABBA230Register register =
                new ABBA230Register( id, name, type, offset, length, unit, 
                writeable, cached, this );
        
        registers.put( name, register );
        return register;
    }
    
    public void setRegister(String name,String value) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            if (register.isWriteable()) {
				register.writeRegister(value);
			} else {
				throw new IOException("Elster A230, setRegister, register not writeable");
			}
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("Elster A230, setRegister, "+e.getMessage());
        }
    }
    
    public void setRegister(String name,Object object) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            if (register.isWriteable()) {
				register.writeRegister(object);
			} else {
				throw new IOException("Elster A230, setRegister, register not writeable");
			}
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("Elster A230, setRegister, "+e.getMessage());
        }
    }
    
    public ABBA230Register getABBA230Register(String name) throws IOException {
        return findRegister(name);
    }
    
    public Object getRegister(ABBA230Register register) throws IOException {
        return getRegister(register.getName(), -1);
    }
    
    public Object getRegister(String name) throws IOException {
        return getRegister(name,-1);
    }
    
    /*  Read a register in the meter, from the current set or a billing set...
     *  @return object the register read
     *  @param billingPoint -1 = current, 0 = last billing point, 1 = 2-throws last billing point, ...
     */
    public Object getRegister(String name,int billingPoint) throws IOException {
        try {
            ABBA230Register register2Retrieve = findRegister(name);
            // current register set
            if (billingPoint == -1 || billingPoint == 255 ) {
                register2Retrieve = findRegister(name);
                return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached()));
            }
            // billing point register set
            else if ((billingPoint>=0) && (billingPoint<=11)) {
                
                if (HistoricalRegister.has(register2Retrieve.getDataID())) {
                    // retrieve the billing set data
                    ABBA230Register register = findRegister("HistoricalRegister");
                    HistoricalRegister historicalValues = (HistoricalRegister)register.parse(register.readRegister(register.isCached(),billingPoint));
                    
                    // find register within the data
                    register2Retrieve = findRegister(name);
                    Object obj = register2Retrieve.parse(historicalValues.getData(register2Retrieve.getDataID()));
                    try {
                        ((MainRegister)obj).setHistoricalValues(historicalValues);
                    } catch(ClassCastException e) {
                        // absorb
                    }
                    return obj;
                } else {
                    register2Retrieve = findRegister(name);
                    return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached(),billingPoint));
                }
            } 
            else if ((billingPoint>=12) && (billingPoint<=25)) {
                
                if (HistoricalRegister.has(register2Retrieve.getDataID())) {
                    // retrieve the billing set data
                    ABBA230Register register = findRegister("DailyHistoricalRegister");
                    HistoricalRegister historicalValues = (HistoricalRegister)register.parse(register.readRegister(register.isCached(),billingPoint-12));
                    
                    // find register within the data
                    register2Retrieve = findRegister(name);
                    Object obj = register2Retrieve.parse(historicalValues.getData(register2Retrieve.getDataID()));
                    try {
                        ((MainRegister)obj).setHistoricalValues(historicalValues);
                    } catch(ClassCastException e) {
                        // absorb
                    }
                    return obj;
                } else {
                    register2Retrieve = findRegister(name);
                    return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached(),billingPoint-12));
                }
            } else {
				throw new IOException("Elster A230, getRegister, invalid billing point "+billingPoint+"!");
			}
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("Elster A230, getRegister, "+e.getMessage());
        }
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }
    
    public byte[] getRegisterRawData(String name,int dataLength) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            return (register.readRegister(register.isCached(),dataLength,0));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("Elster A230, getRegisterRawData, "+e.getMessage());
        }
    }
    
    public byte[] getRegisterRawDataStream(String name, int nrOfBlocks) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            return (register.readRegisterStream(register.isCached(),nrOfBlocks));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("Elster A230, getRegisterRawDataStream, "+e.getMessage());
        }
    }
    
    // search the map for the register info
    private ABBA230Register findRegister(String name) throws IOException {
        ABBA230Register register = (ABBA230Register)registers.get(name);
        if (register == null) {
            String msg = "Elster A230RegisterFactory, findRegister, " + name + " does not exist!";
            throw new IOException(msg);
        } else {
			return register;
		}
    }
    
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }
    
    public String toString( ){
        StringBuffer sb = new StringBuffer();
        sb.append( "RegisterFactory \n" );
        Iterator i = registers.values().iterator();
        while( i.hasNext() ){
            Object value = i.next();
            sb.append( value + " \n" );
        }
        return sb.toString();
    }

    public ABBA230Register getInstrumentationChannelRegister(int valueConfiguration, String phaseConfiguration, String storageConfiguration) {
        String name = instrumentationChannelNameMapping[valueConfiguration - 1] + ": " + phaseConfiguration + " - " + storageConfiguration;
        return cr("InstrumentationChannel" + valueConfiguration, name, ABBA230RegisterData.ABBA_REGISTER, 0, 8, instrumentationChannelUnitMapping[valueConfiguration - 1]);
    }

	public ABBA230Register getCummMainvarhImport() {
		return cummMainvarhImport;
	}

	public ABBA230Register getCummMainvarhExport() {
		return cummMainvarhExport;
	}

	public ABBA230Register getLoadProfileByDate64Blocks() {
		return loadProfileByDate64Blocks;
	}

	public ABBA230Register getContactorStatus() {
		return contactorStatus;
	}

	public void setContactorStatus(ABBA230Register contactorStatus) {
		this.contactorStatus = contactorStatus;
	}

	public ABBA230Register getTimeOfUse8() {
		return timeOfUse8;
	}

	public ABBA230Register getTimeOfUse9() {
		return timeOfUse9;
	}

	public ABBA230Register getTimeOfUse10() {
		return timeOfUse10;
	}

	public ABBA230Register getTimeOfUse11() {
		return timeOfUse11;
	}

	public ABBA230Register getTimeOfUse12() {
		return timeOfUse12;
	}

	public ABBA230Register getTimeOfUse13() {
		return timeOfUse13;
	}

	public ABBA230Register getTimeOfUse14() {
		return timeOfUse14;
	}

	public ABBA230Register getTimeOfUse15() {
		return timeOfUse15;
	}

	public ABBA230Register getLoadProfileDSTConfig() {
		return loadProfileDSTConfig;
	}

	public ABBA230Register getOverVoltageEventLog() {
		return overVoltageEventLog;
	}

	public ABBA230Register getUnderVoltageEventLog() {
		return underVoltageEventLog;
	}

	public ABBA230Register getProgrammingEventLog() {
		return programmingEventLog;
	}

	public ABBA230Register getLongPowerFailEventLog() {
		return longPowerFailEventLog;
	}

	public ABBA230Register getTerminalCoverEventLog() {
		return terminalCoverEventLog;
	}

	public ABBA230Register getMainCoverEventLog() {
		return mainCoverEventLog;
	}

	public ABBA230Register getMagneticTamperEventLog() {
		return magneticTamperEventLog;
	}

	public ABBA230Register getReverserunEventLog() {
		return reverserunEventLog;
	}

	public ABBA230Register getPowerFailEventLog() {
		return powerFailEventLog;
	}

	public ABBA230Register getTransientEventLog() {
		return transientEventLog;
	}

	public ABBA230Register getMeterErrorEventLog() {
		return meterErrorEventLog;
	}

	public ABBA230Register getContactorOpenOpticalLog() {
		return contactorOpenOpticalLog;
	}

	public ABBA230Register getContactorOpenModuleLog() {
		return contactorOpenModuleLog;
	}

	public ABBA230Register getContactorOpenLoadMonitorLowEventLog() {
		return contactorOpenLoadMonitorLowEventLog;
	}

	public ABBA230Register getContactorOpenLoadMonitorHighEventLog() {
		return contactorOpenLoadMonitorHighEventLog;
	}

	public ABBA230Register getContactorOpenAutoDisconnectEventLog() {
		return contactorOpenAutoDisconnectEventLog;
	}

	public ABBA230Register getContactorArmOpticalEventLog() {
		return contactorArmOpticalEventLog;
	}

	public ABBA230Register getContactorArmModuleEventLog() {
		return contactorArmModuleEventLog;
	}

	public ABBA230Register getContactorArmLoadMonitorEventLog() {
		return contactorArmLoadMonitorEventLog;
	}

	public ABBA230Register getContactorArmDisconnectEventLog() {
		return contactorArmDisconnectEventLog;
	}

	public ABBA230Register getContactorCloseOpticalEventLog() {
		return contactorCloseOpticalEventLog;
	}

	public ABBA230Register getContactorCloseModuleEventLog() {
		return contactorCloseModuleEventLog;
	}

	public ABBA230Register getContactorCloseButtonEventLog() {
		return contactorCloseButtonEventLog;
	}

	public ABBA230Register getEndOfBillingEventLog() {
		return endOfBillingEventLog;
	}

	public ABBA230Register getBatteryVoltageLowEventLog() {
		return batteryVoltageLowEventLog;
	}

    public SystemStatus getSystemStatus() throws IOException {
    	if (systemStatus==null) {
    		systemStatus = (SystemStatus)getRegister("SystemStatus");
    	}
    	return systemStatus;
    }
    
}
