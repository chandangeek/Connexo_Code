package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/** @author fbo */

public class ABBA230RegisterFactory {
    
    static public final int MAX_CMD_REGS=4;
    static public final int MAX_MD_REGS=12;
    
    private Map registers = new TreeMap();
    private ProtocolLink protocolLink;
    private MeterExceptionInfo meterExceptionInfo;
    private ABBA230DataIdentityFactory dataIdentityFactory;
    private DataType dataType;
    
    private ABBA230Register cTPrimary;
    private ABBA230Register cTPrimaryAndSecundary;
    private ABBA230Register cTSecundary;
    private ABBA230Register cummMainCustDef1;
    private ABBA230Register cummMainCustDef2;
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
    private ABBA230Register historicalEvents;
    private ABBA230Register historicalRegister;
    private ABBA230Register historicalSystemStatus;
    private ABBA230Register integrationPeriod;
    private ABBA230Register loadProfile;
    private ABBA230Register loadProfile256Blocks;
    private ABBA230Register loadProfile64Blocks;
    private ABBA230Register loadProfileConfiguration;
    private ABBA230Register loadProfileReadByDate;
    private ABBA230Register loadProfileSet;
    private ABBA230Register maximumDemand0;
    private ABBA230Register maximumDemand1;
    private ABBA230Register maximumDemand10;
    private ABBA230Register maximumDemand11;
    private ABBA230Register maximumDemand2;
    private ABBA230Register maximumDemand3;
    private ABBA230Register maximumDemand4;
    private ABBA230Register maximumDemand5;
    private ABBA230Register maximumDemand6;
    private ABBA230Register maximumDemand7;
    private ABBA230Register maximumDemand8;
    private ABBA230Register maximumDemand9;
    private ABBA230Register maximumDemandRegisters;
    private ABBA230Register schemeID;
    private ABBA230Register serialNumber;
    private ABBA230Register systemStatus;
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
    
    /**
     * Creates a new instance of ABBA1140RegisterFactory
     * @param protocolLink
     * @param meterExceptionInfo
     */
    public ABBA230RegisterFactory(
            ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo ) {
        
        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo;
        this.dataType = new DataType(protocolLink.getTimeZone());
        this.dataIdentityFactory = new ABBA230DataIdentityFactory(protocolLink,meterExceptionInfo);
        initRegisters();
        
    }
    
    protected ABBA230DataIdentityFactory getABBA1140DataIdentityFactory() {
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

    public ABBA230Register getCummMainCustDef1() {
        return cummMainCustDef1;
    }

    public ABBA230Register getCummMainCustDef2() {
        return cummMainCustDef2;
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

    public ABBA230Register getHistoricalEvents() {
        return historicalEvents;
    }

    public ABBA230Register getHistoricalRegister() {
        return historicalRegister;
    }

    public ABBA230Register getHistoricalSystemStatus() {
        return historicalSystemStatus;
    }

    public ABBA230Register getIntegrationPeriod() {
        return integrationPeriod;
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

    public ABBA230Register getMaximumDemand0() {
        return maximumDemand0;
    }

    public ABBA230Register getMaximumDemand1() {
        return maximumDemand1;
    }

    public ABBA230Register getMaximumDemand10() {
        return maximumDemand10;
    }

    public ABBA230Register getMaximumDemand11() {
        return maximumDemand11;
    }

    public ABBA230Register getMaximumDemand2() {
        return maximumDemand2;
    }

    public ABBA230Register getMaximumDemand3() {
        return maximumDemand3;
    }

    public ABBA230Register getMaximumDemand4() {
        return maximumDemand4;
    }

    public ABBA230Register getMaximumDemand5() {
        return maximumDemand5;
    }

    public ABBA230Register getMaximumDemand6() {
        return maximumDemand6;
    }

    public ABBA230Register getMaximumDemand7() {
        return maximumDemand7;
    }

    public ABBA230Register getMaximumDemand8() {
        return maximumDemand8;
    }

    public ABBA230Register getMaximumDemand9() {
        return maximumDemand9;
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

    public ABBA230Register getSystemStatus() {
        return systemStatus;
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
        
        cummMainImport = cr("507", "CummMainImport", ABBA230RegisterData.ABBA_REGISTER,0,8,mWh);
        cummMainExport = cr("507", "CummMainExport", ABBA230RegisterData.ABBA_REGISTER,8,8,mWh);
        cummMainQ1 = cr("507", "CummMainQ1", ABBA230RegisterData.ABBA_REGISTER,16,8,mvarh);
        cummMainQ2 = cr("507", "CummMainQ2", ABBA230RegisterData.ABBA_REGISTER,24,8,mvarh);
        cummMainQ3 = cr("507", "CummMainQ3", ABBA230RegisterData.ABBA_REGISTER,32,8,mvarh);
        cummMainQ4 = cr("507", "CummMainQ4", ABBA230RegisterData.ABBA_REGISTER,40,8,mvarh);
        cummMainVAImport = cr("507", "CummMainVAImport", ABBA230RegisterData.ABBA_REGISTER,48,8,mvah);
        cummMainVAExport = cr("507", "CummMainVAExport", ABBA230RegisterData.ABBA_REGISTER,56,8,mvah);
        // reserved for future use
        cummMainCustDef1 = cr("507", "CummMainCustDef1", ABBA230RegisterData.ABBA_REGISTER,112,8,Unit.get(BaseUnit.COUNT,0));
        cummMainCustDef2 = cr("507", "CummMainCustDef2", ABBA230RegisterData.ABBA_REGISTER,120,8,Unit.get(BaseUnit.COUNT,0));
        
        timeOfUse0 = cr("508", "TimeOfUse0", ABBA230RegisterData.ABBA_REGISTER,0,8,null);
        timeOfUse1 = cr("508", "TimeOfUse1", ABBA230RegisterData.ABBA_REGISTER,8,8,null);
        timeOfUse2 = cr("508", "TimeOfUse2", ABBA230RegisterData.ABBA_REGISTER,16,8,null);
        timeOfUse3 = cr("508", "TimeOfUse3", ABBA230RegisterData.ABBA_REGISTER,24,8,null);
        timeOfUse4 = cr("508", "TimeOfUse4", ABBA230RegisterData.ABBA_REGISTER,32,8,null);
        timeOfUse5 = cr("508", "TimeOfUse5", ABBA230RegisterData.ABBA_REGISTER,40,8,null);
        timeOfUse6 = cr("508", "TimeOfUse6", ABBA230RegisterData.ABBA_REGISTER,48,8,null);
        timeOfUse7 = cr("508", "TimeOfUse7", ABBA230RegisterData.ABBA_REGISTER,56,8,null);
        
        cummulativeMaximumDemand = cr("509", "CummulativeMaximumDemand", ABBA230RegisterData.ABBA_BYTEARRAY,0,-1, null );
        cumulativeMaximumDemand0 = cr("509","CumulativeMaximumDemand0", ABBA230RegisterData.ABBA_CMD,0,9,null);
        cumulativeMaximumDemand1 = cr("509","CumulativeMaximumDemand1", ABBA230RegisterData.ABBA_CMD,9,9,null);
        cumulativeMaximumDemand2 = cr("509","CumulativeMaximumDemand2", ABBA230RegisterData.ABBA_CMD,18,9,null);
        cumulativeMaximumDemand3 = cr("509","CumulativeMaximumDemand3", ABBA230RegisterData.ABBA_CMD,27,9,null);
        
        maximumDemandRegisters = cr("510", "MaximumDemandRegisters", ABBA230RegisterData.ABBA_BYTEARRAY,0,208, null );
        maximumDemand0 = cr( "510", "MaximumDemand0", ABBA230RegisterData.ABBA_MD,0,12,null);
        maximumDemand1 = cr( "510", "MaximumDemand1", ABBA230RegisterData.ABBA_MD,12,12,null);
        maximumDemand2 = cr( "510", "MaximumDemand2", ABBA230RegisterData.ABBA_MD,24,12,null);
        maximumDemand3 = cr( "510", "MaximumDemand3", ABBA230RegisterData.ABBA_MD,36,12,null);
        maximumDemand4 = cr( "510", "MaximumDemand4", ABBA230RegisterData.ABBA_MD,48,12,null);
        maximumDemand5 = cr( "510", "MaximumDemand5", ABBA230RegisterData.ABBA_MD,60,12,null);
        maximumDemand6 = cr( "510", "MaximumDemand6", ABBA230RegisterData.ABBA_MD,72,12,null);
        maximumDemand7 = cr( "510", "MaximumDemand7", ABBA230RegisterData.ABBA_MD,84,12,null);
        maximumDemand8 = cr( "510", "MaximumDemand8", ABBA230RegisterData.ABBA_MD,96,12,null); 
        maximumDemand9 = cr( "510", "MaximumDemand9", ABBA230RegisterData.ABBA_MD,108,12,null);
        maximumDemand10 = cr( "510", "MaximumDemand10", ABBA230RegisterData.ABBA_MD,120,12,null);
        maximumDemand11 = cr( "510", "MaximumDemand11", ABBA230RegisterData.ABBA_MD,132,12,null);
        
        historicalRegister = cr("543", "HistoricalRegister", ABBA230RegisterData.ABBA_HISTORICALVALUES,0,457, null);
        historicalEvents = cr("544", "HistoricalEvents", ABBA230RegisterData.ABBA_HISTORICALEVENTS,0,792, null);
        
        loadProfile = cr("550", "LoadProfile", ABBA230RegisterData.ABBA_BYTEARRAY,0,-1, null);
        
        /* The 2 ways to specifiy how much load profile data to retrieve:
         * 551: nr of days
         * 554: between from and to */
        loadProfileSet = cr("551", "LoadProfileSet", ABBA230RegisterData.ABBA_HEX_LE,0,2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        loadProfile64Blocks = cr("551", "LoadProfile64Blocks", ABBA230RegisterData.ABBA_HEX,0,2, null);
        loadProfile256Blocks = cr("551", "LoadProfile256Blocks", ABBA230RegisterData.ABBA_HEX,2,2, null);
        
        loadProfileReadByDate = cr("554", "LoadProfileReadByDate", ABBA230RegisterData.ABBA_LOAD_PROFILE_BY_DATE,0, 2, null, ABBA230Register.WRITEABLE, ABBA230Register.NOT_CACHED);
        
        systemStatus = cr("724", "SystemStatus", ABBA230RegisterData.ABBA_SYSTEMSTATUS,0,4, null);
        
        custDefRegConfig = cr("600", "CustDefRegConfig", ABBA230RegisterData.ABBA_CUSTDEFREGCONFIG,0,4, null);
        
        tariffSources = cr("667", "TariffSources", ABBA230RegisterData.ABBA_TARIFFSOURCES,0,8, null );
        
        cTPrimaryAndSecundary = cr("616", "CTPrimaryAndSecundary", ABBA230RegisterData.ABBA_STRING,0,-1, null);
        cTPrimary = cr("616", "CTPrimary", ABBA230RegisterData.ABBA_BIGDECIMAL,0,4, Unit.get(BaseUnit.UNITLESS,-2));
        cTSecundary = cr("616", "CTSecundary", ABBA230RegisterData.ABBA_BIGDECIMAL,4,2, Unit.get(BaseUnit.UNITLESS,-2));
        
        loadProfileConfiguration = cr("777", "LoadProfileConfiguration", ABBA230RegisterData.ABBA_LOAD_PROFILE_CONFIG,0,2, null);
        integrationPeriod = cr("878", "IntegrationPeriod", ABBA230RegisterData.ABBA_INTEGRATION_PERIOD,0,1, null);
        
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
            if (register.isWriteable()) register.writeRegister(value);
            else throw new IOException("ABBA1140, setRegister, register not writeable");
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, setRegister, "+e.getMessage());
        }
    }
    
    public void setRegister(String name,Object object) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            if (register.isWriteable()) register.writeRegister(object);
            else throw new IOException("ABBA1140, setRegister, register not writeable");
            
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, setRegister, "+e.getMessage());
        }
    }
    
    
    public ABBA230Register getABBA1140Register(String name) throws IOException {
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
            else if ((billingPoint>=0) && (billingPoint<=14)) {
                
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
            } else throw new IOException("ABBA1140, getRegister, invalid billing point "+billingPoint+"!");
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
            ABBA230Register register = findRegister(name);
            return (register.readRegister(register.isCached(),dataLength,0));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, getRegisterRawData, "+e.getMessage());
        }
    }
    
    public byte[] getRegisterRawDataStream(String name, int nrOfBlocks) throws IOException {
        try {
            ABBA230Register register = findRegister(name);
            return (register.readRegisterStream(register.isCached(),nrOfBlocks));
        } catch(FlagIEC1107ConnectionException e) {
            throw new IOException("ABBA1140, getRegisterRawDataStream, "+e.getMessage());
        }
    }
    
    // search the map for the register info
    private ABBA230Register findRegister(String name) throws IOException {
        ABBA230Register register = (ABBA230Register)registers.get(name);
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


    
}
