package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** @author fbo */

public class ABBA1140RegisterFactory {

    static public final int MAX_CMD_REGS=4;
    static public final int MAX_MD_REGS=12;
    static public final int NUMBER_OF_HISTORICAL_REGS=24;
    static public final int NUMBER_OF_DAILY_REGS=14;

    private Map registers = new TreeMap();
    private ProtocolLink protocolLink;
    private MeterExceptionInfo meterExceptionInfo;
    private ABBA1140DataIdentityFactory dataIdentityFactory;
    private DataType dataType;
	private ABBA1140 abba1140;

    private ABBA1140Register cTPrimary;
    private ABBA1140Register cTPrimaryAndSecundary;
    private ABBA1140Register cTSecundary;
    private ABBA1140Register cummMainCustDef1;
    private ABBA1140Register cummMainCustDef2;
    private ABBA1140Register cummMainExport;
    private ABBA1140Register cummMainImport;
    private ABBA1140Register cummMainQ1;
    private ABBA1140Register cummMainQ2;
    private ABBA1140Register cummMainQ3;
    private ABBA1140Register cummMainQ4;
    private ABBA1140Register cummMainVAExport;
    private ABBA1140Register cummMainVAImport;
    private ABBA1140Register cummulativeMaximumDemand;
    private ABBA1140Register cummulativeRegisters;
    private ABBA1140Register cumulativeMaximumDemand0;
    private ABBA1140Register cumulativeMaximumDemand1;
    private ABBA1140Register cumulativeMaximumDemand2;
    private ABBA1140Register cumulativeMaximumDemand3;
    private ABBA1140Register custDefRegConfig;
    private ABBA1140Register historicalEvents;
    private ABBA1140Register historicalRegister;
    private ABBA1140Register dailyHistoricalRegister;
    private ABBA1140Register endOfBillingPeriod;
    private ABBA1140Register firmwareVersion;
    private ABBA1140Register historicalSystemStatus;
    private ABBA1140Register integrationPeriod;
    private ABBA1140Register loadProfile;
    private ABBA1140Register loadProfile256Blocks;
    private ABBA1140Register loadProfile64Blocks;
    private ABBA1140Register loadProfileConfiguration;
    private ABBA1140Register loadProfileReadByDate;
    private ABBA1140Register loadProfileSet;
    private ABBA1140Register maximumDemand0;
    private ABBA1140Register maximumDemand1;
    private ABBA1140Register maximumDemand10;
    private ABBA1140Register maximumDemand11;
    private ABBA1140Register maximumDemand2;
    private ABBA1140Register maximumDemand3;
    private ABBA1140Register maximumDemand4;
    private ABBA1140Register maximumDemand5;
    private ABBA1140Register maximumDemand6;
    private ABBA1140Register maximumDemand7;
    private ABBA1140Register maximumDemand8;
    private ABBA1140Register maximumDemand9;
    private ABBA1140Register maximumDemandRegisters;
    private ABBA1140Register schemeID;
    private ABBA1140Register serialNumber;
    private ABBA1140Register systemStatus;
    private ABBA1140Register tariffSources;
    private ABBA1140Register timeDate;
    private ABBA1140Register timeOfUse0;
    private ABBA1140Register timeOfUse1;
    private ABBA1140Register timeOfUse2;
    private ABBA1140Register timeOfUse3;
    private ABBA1140Register timeOfUse4;
    private ABBA1140Register timeOfUse5;
    private ABBA1140Register timeOfUse6;
    private ABBA1140Register timeOfUse7;
	private ABBA1140Register loadProfileDSTConfig;

	private ABBA1140Register terminalCoverEventLog;
	private ABBA1140Register mainCoverEventLog;
	private ABBA1140Register phaseFailureEventLog;
	private ABBA1140Register reverserunEventLog;
	private ABBA1140Register powerFailEventLog;
	private ABBA1140Register transientEventLog;
	private ABBA1140Register endOfBillingEventLog;
	private ABBA1140Register meterErrorEventLog;
	private ABBA1140Register internalBatteryEventLog;

    public ABBA1140RegisterFactory() {
        initRegisters();
    }

    /**
     * Creates a new instance of ABBA1140RegisterFactory
     * @param protocolLink
     * @param meterExceptionInfo
     */
    public ABBA1140RegisterFactory(
            ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo ) {

        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo;
        this.dataType = new DataType(protocolLink.getTimeZone());
        this.dataIdentityFactory = new ABBA1140DataIdentityFactory(protocolLink,meterExceptionInfo);
        initRegisters();

    }

    protected ABBA1140DataIdentityFactory getABBA1140DataIdentityFactory() {
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

    public ABBA1140Register getCTPrimary() {
        return cTPrimary;
    }

    public ABBA1140Register getCTPrimaryAndSecundary() {
        return cTPrimaryAndSecundary;
    }

    public ABBA1140Register getCTSecundary() {
        return cTSecundary;
    }

    public ABBA1140Register getCummMainCustDef1() {
        return cummMainCustDef1;
    }

    public ABBA1140Register getCummMainCustDef2() {
        return cummMainCustDef2;
    }

    public ABBA1140Register getCummMainExport() {
        return cummMainExport;
    }

    public ABBA1140Register getCummMainImport() {
        return cummMainImport;
    }

    public ABBA1140Register getCummMainQ1() {
        return cummMainQ1;
    }

    public ABBA1140Register getCummMainQ2() {
        return cummMainQ2;
    }

    public ABBA1140Register getCummMainQ3() {
        return cummMainQ3;
    }

    public ABBA1140Register getCummMainQ4() {
        return cummMainQ4;
    }

    public ABBA1140Register getCummMainVAExport() {
        return cummMainVAExport;
    }

    public ABBA1140Register getCummMainVAImport() {
        return cummMainVAImport;
    }

    public ABBA1140Register getCummulativeMaximumDemand() {
        return cummulativeMaximumDemand;
    }

    public ABBA1140Register getCummulativeRegisters() {
        return cummulativeRegisters;
    }

    public ABBA1140Register getCumulativeMaximumDemand0() {
        return cumulativeMaximumDemand0;
    }

    public ABBA1140Register getCumulativeMaximumDemand1() {
        return cumulativeMaximumDemand1;
    }

    public ABBA1140Register getCumulativeMaximumDemand2() {
        return cumulativeMaximumDemand2;
    }

    public ABBA1140Register getCumulativeMaximumDemand3() {
        return cumulativeMaximumDemand3;
    }

    public ABBA1140Register getCustDefRegConfig() {
        return custDefRegConfig;
    }

    public ABBA1140Register getHistoricalEvents() {
        return historicalEvents;
    }

    public ABBA1140Register getHistoricalRegister() {
        return historicalRegister;
    }

    public ABBA1140Register getHistoricalSystemStatus() {
        return historicalSystemStatus;
    }

    public ABBA1140Register getIntegrationPeriod() {
        return integrationPeriod;
    }

    public ABBA1140Register getLoadProfile() {
        return loadProfile;
    }

    public ABBA1140Register getLoadProfile256Blocks() {
        return loadProfile256Blocks;
    }

    public ABBA1140Register getLoadProfile64Blocks() {
        return loadProfile64Blocks;
    }

    public ABBA1140Register getLoadProfileConfiguration() {
        return loadProfileConfiguration;
    }

    public ABBA1140Register getLoadProfileReadByDate() {
        return loadProfileReadByDate;
    }

    public ABBA1140Register getLoadProfileSet() {
        return loadProfileSet;
    }

    public ABBA1140Register getMaximumDemand0() {
        return maximumDemand0;
    }

    public ABBA1140Register getMaximumDemand1() {
        return maximumDemand1;
    }

    public ABBA1140Register getMaximumDemand10() {
        return maximumDemand10;
    }

    public ABBA1140Register getMaximumDemand11() {
        return maximumDemand11;
    }

    public ABBA1140Register getMaximumDemand2() {
        return maximumDemand2;
    }

    public ABBA1140Register getMaximumDemand3() {
        return maximumDemand3;
    }

    public ABBA1140Register getMaximumDemand4() {
        return maximumDemand4;
    }

    public ABBA1140Register getMaximumDemand5() {
        return maximumDemand5;
    }

    public ABBA1140Register getMaximumDemand6() {
        return maximumDemand6;
    }

    public ABBA1140Register getMaximumDemand7() {
        return maximumDemand7;
    }

    public ABBA1140Register getMaximumDemand8() {
        return maximumDemand8;
    }

    public ABBA1140Register getMaximumDemand9() {
        return maximumDemand9;
    }

    public ABBA1140Register getMaximumDemandRegisters() {
        return maximumDemandRegisters;
    }

    public ABBA1140Register getSchemeID() {
        return schemeID;
    }

    public ABBA1140Register getSerialNumber() {
        return serialNumber;
    }

    public ABBA1140Register getSystemStatus() {
        return systemStatus;
    }

    public ABBA1140Register getTariffSources() {
        return tariffSources;
    }

    public ABBA1140Register getTimeDate() {
        return timeDate;
    }

    public ABBA1140Register getTimeOfUse0() {
        return timeOfUse0;
    }

    public ABBA1140Register getTimeOfUse1() {
        return timeOfUse1;
    }

    public ABBA1140Register getTimeOfUse2() {
        return timeOfUse2;
    }

    public ABBA1140Register getTimeOfUse3() {
        return timeOfUse3;
    }

    public ABBA1140Register getTimeOfUse4() {
        return timeOfUse4;
    }

    public ABBA1140Register getTimeOfUse5() {
        return timeOfUse5;
    }

    public ABBA1140Register getTimeOfUse6() {
        return timeOfUse6;
    }

    public ABBA1140Register getTimeOfUse7() {
        return timeOfUse7;
    }

    public ABBA1140Register getDailyHistoricalRegister() {
		return dailyHistoricalRegister;
	}

    public ABBA1140Register getEndOfBillingPeriod() {
		return endOfBillingPeriod;
	}

    public ABBA1140Register getFirmwareVersion() {
		return firmwareVersion;
	}

    // length = -1, not used
    private void initRegisters() {

        Unit mWh = Unit.get(BaseUnit.WATTHOUR,-3);
        Unit mvarh = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3);
        Unit mvah = Unit.get(BaseUnit.VOLTAMPEREHOUR, -3);

        serialNumber = cr("798", "SerialNumber", ABBA1140RegisterData.ABBA_STRING,0, -1,null );
        schemeID = cr("795", "SchemeID", ABBA1140RegisterData.ABBA_STRING,0,8, null );
        timeDate = cr("861", "TimeDate", ABBA1140RegisterData.ABBA_DATE,0,-1, null, ABBA1140Register.WRITEABLE, ABBA1140Register.NOT_CACHED);

        cummulativeRegisters = cr("507", "CummulativeRegisters", ABBA1140RegisterData.ABBA_BYTEARRAY,0,-1, null );

        cummMainImport = cr("507", "CummMainImport", ABBA1140RegisterData.ABBA_REGISTER,0,8,mWh);
        cummMainExport = cr("507", "CummMainExport", ABBA1140RegisterData.ABBA_REGISTER,8,8,mWh);
        cummMainQ1 = cr("507", "CummMainQ1", ABBA1140RegisterData.ABBA_REGISTER,16,8,mvarh);
        cummMainQ2 = cr("507", "CummMainQ2", ABBA1140RegisterData.ABBA_REGISTER,24,8,mvarh);
        cummMainQ3 = cr("507", "CummMainQ3", ABBA1140RegisterData.ABBA_REGISTER,32,8,mvarh);
        cummMainQ4 = cr("507", "CummMainQ4", ABBA1140RegisterData.ABBA_REGISTER,40,8,mvarh);
        cummMainVAImport = cr("507", "CummMainVAImport", ABBA1140RegisterData.ABBA_REGISTER,48,8,mvah);
        cummMainVAExport = cr("507", "CummMainVAExport", ABBA1140RegisterData.ABBA_REGISTER,56,8,mvah);
        // reserved for future use
        cummMainCustDef1 = cr("507", "CummMainCustDef1", ABBA1140RegisterData.ABBA_REGISTER,112,8,Unit.get(BaseUnit.COUNT,0));
        cummMainCustDef2 = cr("507", "CummMainCustDef2", ABBA1140RegisterData.ABBA_REGISTER,120,8,Unit.get(BaseUnit.COUNT,0));

        timeOfUse0 = cr("508", "TimeOfUse0", ABBA1140RegisterData.ABBA_REGISTER,0,8,null);
        timeOfUse1 = cr("508", "TimeOfUse1", ABBA1140RegisterData.ABBA_REGISTER,8,8,null);
        timeOfUse2 = cr("508", "TimeOfUse2", ABBA1140RegisterData.ABBA_REGISTER,16,8,null);
        timeOfUse3 = cr("508", "TimeOfUse3", ABBA1140RegisterData.ABBA_REGISTER,24,8,null);
        timeOfUse4 = cr("508", "TimeOfUse4", ABBA1140RegisterData.ABBA_REGISTER,32,8,null);
        timeOfUse5 = cr("508", "TimeOfUse5", ABBA1140RegisterData.ABBA_REGISTER,40,8,null);
        timeOfUse6 = cr("508", "TimeOfUse6", ABBA1140RegisterData.ABBA_REGISTER,48,8,null);
        timeOfUse7 = cr("508", "TimeOfUse7", ABBA1140RegisterData.ABBA_REGISTER,56,8,null);

        cummulativeMaximumDemand = cr("509", "CummulativeMaximumDemand", ABBA1140RegisterData.ABBA_BYTEARRAY,0,-1, null );
        cumulativeMaximumDemand0 = cr("509","CumulativeMaximumDemand0", ABBA1140RegisterData.ABBA_CMD,0,9,null);
        cumulativeMaximumDemand1 = cr("509","CumulativeMaximumDemand1", ABBA1140RegisterData.ABBA_CMD,9,9,null);
        cumulativeMaximumDemand2 = cr("509","CumulativeMaximumDemand2", ABBA1140RegisterData.ABBA_CMD,18,9,null);
        cumulativeMaximumDemand3 = cr("509","CumulativeMaximumDemand3", ABBA1140RegisterData.ABBA_CMD,27,9,null);

        maximumDemandRegisters = cr("510", "MaximumDemandRegisters", ABBA1140RegisterData.ABBA_BYTEARRAY,0,208, null );
        maximumDemand0 = cr( "510", "MaximumDemand0", ABBA1140RegisterData.ABBA_MD,0,12,null);
        maximumDemand1 = cr( "510", "MaximumDemand1", ABBA1140RegisterData.ABBA_MD,12,12,null);
        maximumDemand2 = cr( "510", "MaximumDemand2", ABBA1140RegisterData.ABBA_MD,24,12,null);
        maximumDemand3 = cr( "510", "MaximumDemand3", ABBA1140RegisterData.ABBA_MD,36,12,null);
        maximumDemand4 = cr( "510", "MaximumDemand4", ABBA1140RegisterData.ABBA_MD,48,12,null);
        maximumDemand5 = cr( "510", "MaximumDemand5", ABBA1140RegisterData.ABBA_MD,60,12,null);
        maximumDemand6 = cr( "510", "MaximumDemand6", ABBA1140RegisterData.ABBA_MD,72,12,null);
        maximumDemand7 = cr( "510", "MaximumDemand7", ABBA1140RegisterData.ABBA_MD,84,12,null);
        maximumDemand8 = cr( "510", "MaximumDemand8", ABBA1140RegisterData.ABBA_MD,96,12,null);
        maximumDemand9 = cr( "510", "MaximumDemand9", ABBA1140RegisterData.ABBA_MD,108,12,null);
        maximumDemand10 = cr( "510", "MaximumDemand10", ABBA1140RegisterData.ABBA_MD,120,12,null);
        maximumDemand11 = cr( "510", "MaximumDemand11", ABBA1140RegisterData.ABBA_MD,132,12,null);

        historicalRegister = cr("543", "HistoricalRegister", ABBA1140RegisterData.ABBA_HISTORICALVALUES,0,457, null);
        historicalEvents = cr("544", "HistoricalEvents", ABBA1140RegisterData.ABBA_HISTORICALEVENTS,0,792, null);

        dailyHistoricalRegister = cr("545", "DailyHistoricalRegister", ABBA1140RegisterData.ABBA_HISTORICALVALUES,0,302, null);

        // event logs
        terminalCoverEventLog = cr("691", "TerminalCoverEventLog", ABBA1140RegisterData.ABBA_TERMINALCOVEREVENTLOG,0,14, null);
        mainCoverEventLog = cr("692", "MainCoverEventLog", ABBA1140RegisterData.ABBA_MAINCOVEREVENTLOG,0,14, null);
        phaseFailureEventLog = cr("693", "PhaseFailureEventLog", ABBA1140RegisterData.ABBA_PHASEFAILUREEVENTLOG,0,17, null);
        reverserunEventLog = cr("694", "ReverserunEventLog", ABBA1140RegisterData.ABBA_REVERSERUNEVENTLOG,0,14, null);
        powerFailEventLog = cr("695", "PowerFailEventLog", ABBA1140RegisterData.ABBA_POWEREFAILEVENTLOG,0,14, null);
        transientEventLog = cr("696", "TransientEventLog", ABBA1140RegisterData.ABBA_TRANSIENTEVENTLOG,0,14, null);
        internalBatteryEventLog = cr("697", "InternalBatteryEventLog", ABBA1140RegisterData.ABBA_INTERNALBATTERYEVENTLOG,0,14, null);
        endOfBillingEventLog = cr("699", "EndOfBillingEventLog", ABBA1140RegisterData.ABBA_ENDOFBILLINGEVENTLOG,0,17, null);
        meterErrorEventLog = cr("701", "MeterErrorEventLog", ABBA1140RegisterData.ABBA_METERERROREVENTLOG,0,14, null);

        loadProfile = cr("550", "LoadProfile", ABBA1140RegisterData.ABBA_BYTEARRAY,0,-1, null);

        /* The 2 ways to specifiy how much load profile data to retrieve:
         * 551: nr of days
         * 554: between from and to */
        loadProfileSet = cr("551", "LoadProfileSet", ABBA1140RegisterData.ABBA_HEX_LE,0,2, null, ABBA1140Register.WRITEABLE, ABBA1140Register.NOT_CACHED);
        loadProfile64Blocks = cr("551", "LoadProfile64Blocks", ABBA1140RegisterData.ABBA_HEX,0,2, null);
        loadProfile256Blocks = cr("551", "LoadProfile256Blocks", ABBA1140RegisterData.ABBA_HEX,2,2, null);

        loadProfileReadByDate = cr("554", "LoadProfileReadByDate", ABBA1140RegisterData.ABBA_LOAD_PROFILE_BY_DATE,0, 2, null, ABBA1140Register.WRITEABLE, ABBA1140Register.NOT_CACHED);

        systemStatus = cr("724", "SystemStatus", ABBA1140RegisterData.ABBA_SYSTEMSTATUS,0,4, null);

        custDefRegConfig = cr("600", "CustDefRegConfig", ABBA1140RegisterData.ABBA_CUSTDEFREGCONFIG,0,4, null);

        tariffSources = cr("667", "TariffSources", ABBA1140RegisterData.ABBA_TARIFFSOURCES,0,8, null );

        cTPrimaryAndSecundary = cr("616", "CTPrimaryAndSecundary", ABBA1140RegisterData.ABBA_STRING,0,-1, null);
        cTPrimary = cr("616", "CTPrimary", ABBA1140RegisterData.ABBA_HEX,0,4, Unit.get(BaseUnit.UNITLESS,-2));
        cTSecundary = cr("616", "CTSecundary", ABBA1140RegisterData.ABBA_HEX,4,4, Unit.get(BaseUnit.UNITLESS,-2));

        loadProfileConfiguration = cr("777", "LoadProfileConfiguration", ABBA1140RegisterData.ABBA_LOAD_PROFILE_CONFIG,0,2, null);
        integrationPeriod = cr("878", "IntegrationPeriod", ABBA1140RegisterData.ABBA_INTEGRATION_PERIOD,0,1, null);

        endOfBillingPeriod = cr("655", "EndOfBillingPeriod", ABBA1140RegisterData.ABBA_HEX, 0, 1, null, ABBA1140Register.WRITEABLE, ABBA1140Register.NOT_CACHED);
        loadProfileDSTConfig = cr("778", "LoadProfileDSTConfig", ABBA1140RegisterData.ABBA_HEX,0,1, null);
        firmwareVersion = cr("998", "FirmwareVersion", ABBA1140RegisterData.ABBA_STRING, 0, 12, null);

    }

    /** factory method for ABBARegisters */
    private ABBA1140Register cr(
            String id, String name, int type, int offset, int length, Unit unit ){

        ABBA1140Register register =
                new ABBA1140Register( id, name, type, offset, length, unit,
                ABBA1140Register.NOT_WRITEABLE, ABBA1140Register.CACHED, this );

        registers.put( name, register );
        return register;
    }

    /** factory method for ABBARegisters */
    private ABBA1140Register cr(
            String id, String name, int type, int offset, int length, Unit unit,
            boolean writeable, boolean cached ){

        ABBA1140Register register =
                new ABBA1140Register( id, name, type, offset, length, unit,
                writeable, cached, this );

        registers.put( name, register );
        return register;
    }

    public void setRegister(String name,String value) throws IOException {
        try {
            ABBA1140Register register = findRegister(name);
            if (register.isWriteable()) register.writeRegister(value);
            else throw new IOException("ABBA1140, setRegister, register not writeable");

        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, setRegister, "+e.getMessage());
        }
    }

    public void setRegister(String name,Object object) throws IOException {
        try {
            ABBA1140Register register = findRegister(name);
            if (register.isWriteable()) register.writeRegister(object);
            else throw new IOException("ABBA1140, setRegister, register not writeable");

        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, setRegister, "+e.getMessage());
        }
    }


    public ABBA1140Register getABBA1140Register(String name) throws IOException {
        return findRegister(name);
    }

    public Object getRegister(ABBA1140Register register) throws IOException {
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
            ABBA1140Register register2Retrieve = findRegister(name);
            // current register set
            if (billingPoint == -1 || billingPoint == 255 ) {
                register2Retrieve = findRegister(name);
                return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached()));
            }
            // billing point register set
            else if ((billingPoint>=0) && (billingPoint<NUMBER_OF_HISTORICAL_REGS)) { //JME: Changed highest billingpoint from 14 to 23 (new firmware stores 24 historical values)

                if (HistoricalRegister.has(register2Retrieve.getDataID())) {
                    // retrieve the billing set data
                    ABBA1140Register register = findRegister("HistoricalRegister");
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
            else if ((billingPoint>=NUMBER_OF_HISTORICAL_REGS) && (billingPoint<(NUMBER_OF_HISTORICAL_REGS + NUMBER_OF_DAILY_REGS))) {

                    if (HistoricalRegister.has(register2Retrieve.getDataID())) {
                        // retrieve the billing set data
                        ABBA1140Register register = findRegister("DailyHistoricalRegister");
                        HistoricalRegister historicalValues = (HistoricalRegister)register.parse(register.readRegister(register.isCached(),billingPoint - NUMBER_OF_HISTORICAL_REGS));

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
                        return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached(),billingPoint - NUMBER_OF_HISTORICAL_REGS));
                    }
                } else throw new IOException("Elster A1140, getRegister, invalid billing point "+billingPoint+"!");
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, getRegister, "+e.getMessage());
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public byte[] getRegisterRawData(String name,int dataLength) throws IOException {
        try {
            ABBA1140Register register = findRegister(name);
            return (register.readRegister(register.isCached(),dataLength,0));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, getRegisterRawData, "+e.getMessage());
        }
    }

    public byte[] getRegisterRawDataStream(String name, int nrOfBlocks) throws IOException {
        try {
            ABBA1140Register register = findRegister(name);
            return (register.readRegisterStream(register.isCached(),nrOfBlocks));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, getRegisterRawDataStream, "+e.getMessage());
        }
    }

    // search the map for the register info
    private ABBA1140Register findRegister(String name) throws IOException {
        ABBA1140Register register = (ABBA1140Register)registers.get(name);
        if (register == null) {
            String msg = "ABBA1140RegisterFactory, findRegister, " + name + " does not exist!";
            throw new IOException(msg);
        } else return register;
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

	public void setABBA1140(ABBA1140 abba1140) {
		this.abba1140  = abba1140;
	}

	public ABBA1140 getAbba1140() {
		return abba1140;
	}

	public ABBA1140Register getLoadProfileDSTConfig() {
		return loadProfileDSTConfig;
	}

	public ABBA1140Register getTerminalCoverEventLog() {
		return terminalCoverEventLog;
	}

	public ABBA1140Register getMainCoverEventLog() {
		return mainCoverEventLog;
	}

	public ABBA1140Register getPhaseFailureEventLog() {
		return phaseFailureEventLog;
	}

	public ABBA1140Register getReverserunEventLog() {
		return reverserunEventLog;
	}

	public ABBA1140Register getPowerFailEventLog() {
		return powerFailEventLog;
	}

	public ABBA1140Register getTransientEventLog() {
		return transientEventLog;
	}

	public ABBA1140Register getEndOfBillingEventLog() {
		return endOfBillingEventLog;
	}

	public ABBA1140Register getMeterErrorEventLog() {
		return meterErrorEventLog;
	}

	public ABBA1140Register getInternalBatteryEventLog() {
		return internalBatteryEventLog;
	}
}
