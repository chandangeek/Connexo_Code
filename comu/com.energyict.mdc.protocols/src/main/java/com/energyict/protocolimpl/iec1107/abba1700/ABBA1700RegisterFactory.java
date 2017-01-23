/*
 * ABBA1700RegisterFactory.java
 *
 * Created on 17 juni 2003, 10:27
 * Changes:
 * KV 16022004 add external input channels
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author  Koen
 */
public class ABBA1700RegisterFactory {

    //Constants for the registerIdentifications:
    public static final String SerialNumberKey = "SerialNumber";
    public static final String BillingResetKey = "BillingReset";
    public static final String TimeDateKey = "TimeDate";
    public static final String VoltageTransformerRatio = "VTPrimaryAndSecundary";
    public static final String VoltageTransformerRatioPrimary = "VTPrimary";
    public static final String VoltageTransformerRatioSecondary = "VTSecundary";
    public static final String CurrentTransformerRatio = "CTPrimaryAndSecundary";
    public static final String CurrentTransformerRatioPrimary = "CTPrimary";
    public static final String CurrentTransformerRatioSecondary = "CTSecundary";
    public static final String ProgrammingCounterKey = "ProgrammingCounter";
    public static final String PhaseFailureCounterKey = "PhaseFailureCounter";
    public static final String PhaseFailureCounterKey2 = "PhaseFailureCounter2";    // For meterType 2
    public static final String ReverseRunCounterKey = "ReverseRunCounter";
    public static final String ReverseRunCounterKey2 = "ReverseRunCounter2";
    public static final String PowerDownCounterKey = "PowerDownCounter";
    public static final String PowerDownCounterKey2 = "PowerDownCounter2";
    public static final String HistoricalEventsKey = "HistoricalEvents";
    public static final String BatterySupportStatusKey = "BatterySupportStatus";
    public static final String HistoricalValuesKey = "HistoricalValues";

    public static final int MAX_CMD_REGS=8;
    public static final int MAX_MD_REGS=24;

    // 32 TOU registers
    //static public final int MAX_TARIFF_REGS=32;
    //static public final int EXTRA_OFFSET=124;

    // 16 TOU registers
    //static public final int MAX_TARIFF_REGS=16;
    //static public final int EXTRA_OFFSET=0;


    private Map registers = new HashMap();
    private ProtocolLink protocolLink = null;
    private MeterExceptionInfo meterExceptionInfo = null; // KV 19012004
    private ABBA1700DataIdentityFactory abba1700DataItendityFactory=null;

    ABBA1700MeterType meterType;

    protected ABBA1700DataIdentityFactory getABBA1700DataIdentityFactory() {
      return abba1700DataItendityFactory;
    }

    protected ProtocolLink getProtocolLink() {
       return protocolLink;
    }

    protected Map getRegisters() {
       return registers;
    }

    // length = -1, not used

    private void initRegisters() {
        registers.put(SerialNumberKey, new ABBA1700Register("798", ABBA1700RegisterData.ABBA_STRING, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("SchemeID", new ABBA1700Register("795", ABBA1700RegisterData.ABBA_STRING, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(TimeDateKey, new ABBA1700Register("861", ABBA1700RegisterData.ABBA_DATE, 0, -1, null, ABBA1700Register.WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put("CummulativeMaximumDemand", new ABBA1700Register("509", ABBA1700RegisterData.ABBA_BYTEARRAY, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put("MaximumDemandRegisters", new ABBA1700Register("510", ABBA1700RegisterData.ABBA_BYTEARRAY, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));

        registers.put("CummulativeRegisters", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_BYTEARRAY, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put("CummMainImport", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 0, 8, Unit.get(BaseUnit.WATTHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainExport", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 8, 8, Unit.get(BaseUnit.WATTHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainQ1", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 16, 8, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainQ2", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 24, 8, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainQ3", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 32, 8, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainQ4", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 40, 8, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainVA", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 48, 8, Unit.get(BaseUnit.VOLTAMPEREHOUR, -3), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainCustDef1", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 56, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainCustDef2", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 64, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CummMainCustDef3", new ABBA1700Register("507", ABBA1700RegisterData.ABBA_REGISTER, 72, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));

        // KV 16022004
        registers.put("ExternalInput1", new ABBA1700Register("516", ABBA1700RegisterData.ABBA_REGISTER, 0, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("ExternalInput2", new ABBA1700Register("516", ABBA1700RegisterData.ABBA_REGISTER, 8, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("ExternalInput3", new ABBA1700Register("516", ABBA1700RegisterData.ABBA_REGISTER, 16, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("ExternalInput4", new ABBA1700Register("516", ABBA1700RegisterData.ABBA_REGISTER, 24, 8, Unit.get(BaseUnit.COUNT, 0), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));

        // KV 10062004
        for (int i = 0; i < getMeterType().getNrOfTariffRegisters(); i++) {
            registers.put("TimeOfUse" + i, new ABBA1700Register("508", ABBA1700RegisterData.ABBA_REGISTER, i * 8, 8, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        }
        for (int i = 0; i < MAX_MD_REGS; i++) {
            registers.put("MaximumDemand" + i, new ABBA1700Register("510", ABBA1700RegisterData.ABBA_MD, i * 12, 12, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        }
        for (int i = 0; i < MAX_CMD_REGS; i++) {
            registers.put("CumulativeMaximumDemand" + i, new ABBA1700Register("509", ABBA1700RegisterData.ABBA_CMD, i * 9, 9, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        }

        registers.put(VoltageTransformerRatio, new ABBA1700Register("614", ABBA1700RegisterData.ABBA_STRING, 0, 7, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(VoltageTransformerRatioPrimary, new ABBA1700Register("614", ABBA1700RegisterData.ABBA_BIGDECIMAL, 0, 4, Unit.get(BaseUnit.UNITLESS, -2), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(VoltageTransformerRatioSecondary, new ABBA1700Register("614", ABBA1700RegisterData.ABBA_BIGDECIMAL, 4, 3, Unit.get(BaseUnit.UNITLESS, -2), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(CurrentTransformerRatio, new ABBA1700Register("616", ABBA1700RegisterData.ABBA_STRING, 0, 6, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(CurrentTransformerRatioPrimary, new ABBA1700Register("616", ABBA1700RegisterData.ABBA_BIGDECIMAL, 0, 4, Unit.get(BaseUnit.UNITLESS, -2), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(CurrentTransformerRatioSecondary, new ABBA1700Register("616", ABBA1700RegisterData.ABBA_BIGDECIMAL, 4, 2, Unit.get(BaseUnit.UNITLESS, -2), ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("LoadProfileConfiguration", new ABBA1700Register("777", ABBA1700RegisterData.ABBA_64BITFIELD, 0, 2, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("IntegrationPeriod", new ABBA1700Register("878", ABBA1700RegisterData.ABBA_INTEGER, 0, 1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("SubintervalPeriod", new ABBA1700Register("878", ABBA1700RegisterData.ABBA_INTEGER, 1, 1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("NumberOfSubintervals", new ABBA1700Register("878", ABBA1700RegisterData.ABBA_INTEGER, 2, 1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));

        registers.put("LoadProfileSet", new ABBA1700Register("551", ABBA1700RegisterData.ABBA_HEX_LE, 0, 2, null, ABBA1700Register.WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put("LoadProfile64Blocks", new ABBA1700Register("551", ABBA1700RegisterData.ABBA_HEX, 0, 2, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("LoadProfile256Blocks", new ABBA1700Register("551", ABBA1700RegisterData.ABBA_HEX, 2, 2, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("LoadProfile", new ABBA1700Register("550", ABBA1700RegisterData.ABBA_BYTEARRAY, 0, -1, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));

        registers.put("SystemStatus", new ABBA1700Register("724", ABBA1700RegisterData.ABBA_SYSTEMSTATUS, 0, 4, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("HistoricalSystemStatus", new ABBA1700Register("691", ABBA1700RegisterData.ABBA_SYSTEMSTATUS, 0, 4, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(HistoricalEventsKey, new ABBA1700Register("544", ABBA1700RegisterData.ABBA_HISTORICALEVENTS, 0, 792, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));

        registers.put("MDSources", new ABBA1700Register("668", ABBA1700RegisterData.ABBA_MDSOURCES, 0, 8, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("CustDefRegConfig", new ABBA1700Register("601", ABBA1700RegisterData.ABBA_CUSTDEFREGCONFIG, 0, meterType.hasExtendedCustomerRegisters() ? 15 : 6, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));


        registers.put("HistoricalDisplayScalings", new ABBA1700Register("548", ABBA1700RegisterData.ABBA_HISTORICALDISPLAYSCALINGS, 0, (22 + getMeterType().getNrOfTariffRegisters() + 8 + getMeterType().getExtraOffsetHistoricDisplayScaling()) * 12, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put("TariffSources", new ABBA1700Register("667", ABBA1700RegisterData.ABBA_TARIFFSOURCES, 0, getMeterType().getNrOfTariffRegisters(), null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));
        registers.put(HistoricalValuesKey, new ABBA1700Register("543", ABBA1700RegisterData.ABBA_HISTORICALVALUES, 0, (10 * 8 + getMeterType().getNrOfTariffRegisters() * 8 + 4 * 8 + 8 * 9 + 24 * 12 + 15) * 12, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.CACHED));

        registers.put("InstantaneousValues", new ABBA1700Register("606", ABBA1700RegisterData.ABBA_INSTANTANEOUSVALUES, 0, 7, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put("InstantaneousValuesRequest", new ABBA1700Register("605", ABBA1700RegisterData.ABBA_HEX, 0, 1, null, ABBA1700Register.WRITEABLE, ABBA1700Register.NOT_CACHED));

        // KV 30082006
        registers.put(BillingResetKey, new ABBA1700Register("655", -1, 0, -1, null, ABBA1700Register.WRITEABLE, ABBA1700Register.NOT_CACHED));

        registers.put(ProgrammingCounterKey, new ABBA1700Register("680", ABBA1700RegisterData.ABBA_PROGRAMMING_COUNTER, 0, 14, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(PhaseFailureCounterKey, new ABBA1700Register("693", ABBA1700RegisterData.ABBA_PHASE_FAILURE_COUNTER, 0, 17, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(PhaseFailureCounterKey2, new ABBA1700Register("693", ABBA1700RegisterData.ABBA_PHASE_FAILURE_COUNTER2, 0, 63, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(ReverseRunCounterKey, new ABBA1700Register("694", ABBA1700RegisterData.ABBA_REVERSE_RUN_COUNTER, 0, 14, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(ReverseRunCounterKey2, new ABBA1700Register("694", ABBA1700RegisterData.ABBA_REVERSE_RUN_COUNTER2, 0, 46, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(PowerDownCounterKey, new ABBA1700Register("695", ABBA1700RegisterData.ABBA_POWER_DOWN_COUNTER, 0, 14, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(PowerDownCounterKey2, new ABBA1700Register("695", ABBA1700RegisterData.ABBA_POWER_DOWN_COUNTER2, 0, 22, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
        registers.put(BatterySupportStatusKey, new ABBA1700Register("546", ABBA1700RegisterData.ABBA_BATTERY_STATUS, 0, 12, null, ABBA1700Register.NOT_WRITEABLE, ABBA1700Register.NOT_CACHED));
    }

    private void initLocals() {
        Iterator iterator = registers.values().iterator();
        while(iterator.hasNext()) {
            ABBA1700Register reg = (ABBA1700Register)iterator.next();
            reg.setABBA1700RegisterFactory(this);
        }
    }

    /**
     * Creates a new instance of ABBA1700RegisterFactory
     */
    public ABBA1700RegisterFactory(ProtocolLink protocolLink, MeterExceptionInfo meterExceptionInfo, ABBA1700MeterType abba1700MeterType) { // KV 19012004
        this.protocolLink = protocolLink;
        this.meterExceptionInfo = meterExceptionInfo; // KV 19012004
        meterType = abba1700MeterType;
        abba1700DataItendityFactory = new ABBA1700DataIdentityFactory(protocolLink,meterExceptionInfo,meterType);
        initRegisters();
        initLocals();

    }

    public void setRegister(String name,String value) throws IOException {
        try {
           ABBA1700Register register = findRegister(name);
            if (register.isWriteable()) {
                register.writeRegister(value);
            } else {
                throw new IOException("ABBA1700, setRegister, register not writeable");
            }

        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, setRegister, "+e.getMessage());
        }
    }

    public void setRegister(String name,Object object) throws IOException {
        try {
           ABBA1700Register register = findRegister(name);
            if (register.isWriteable()) {
                register.writeRegister(object);
            } else {
                throw new IOException("ABBA1700, setRegister, register not writeable");
            }

        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, setRegister, "+e.getMessage());
        }
    }

    public void invokeRegister(String name) throws IOException {
        try {
           ABBA1700Register register = findRegister(name);
            if (register.isWriteable()) {
                register.invokeRegister();
            } else {
                throw new IOException("ABBA1700, setRegister, register not writeable");
            }

        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, setRegister, "+e.getMessage());
        }
    }

    public ABBA1700Register getABBA1700Register(String name) throws IOException {
        return findRegister(name);
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
           ABBA1700Register register2Retrieve = findRegister(name);
            // current register set
           if (billingPoint == -1) {
               register2Retrieve = findRegister(name);
               return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached()));
           }
           // billing point register set
           else if ((billingPoint>=0) && (billingPoint<=11)) {
               if (HistoricalValues.has(register2Retrieve.getDataID())) {
                   // retrieve the billing set data
                   ABBA1700Register register = findRegister(HistoricalValuesKey);
                   HistoricalValues historicalValues = (HistoricalValues)register.parse(register.readRegister(register.isCached(),billingPoint));
                   HistoricalValueSetInfo hvsi = historicalValues.getHistoricalValueSetInfo();
                   // find register within the data
                   register2Retrieve = findRegister(name);
                   Object obj = register2Retrieve.parse(historicalValues.getData(register2Retrieve.getDataID()));
                   try {
                      ((MainRegister)obj).setHistoricalValueSetInfo(hvsi);
                   }
                   catch(ClassCastException e) {
                       // absorb
                   }
                   return obj;
               }
               else {
                   register2Retrieve = findRegister(name);
                   return register2Retrieve.parse(register2Retrieve.readRegister(register2Retrieve.isCached(),billingPoint));
               }
            } else {
                throw new IOException("ABBA1700, getRegister, invalid billing point " + billingPoint + "!");
           }
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, getRegister, "+e.getMessage());
        }
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public byte[] getRegisterRawData(String name,int dataLength) throws IOException {
        try {
           ABBA1700Register register = findRegister(name);
           return (register.readRegister(register.isCached(),dataLength,0));
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, getRegisterRawData, "+e.getMessage());
        }
    }

    public byte[] getRegisterRawDataStream(String name, int nrOfBlocks) throws IOException {
        try {
           ABBA1700Register register = findRegister(name);
           return (register.readRegisterStream(register.isCached(),nrOfBlocks));
        }
        catch(FlagIEC1107ConnectionException e) {
           throw new IOException("ABBA1700, getRegisterRawDataStream, "+e.getMessage());
        }
    }

    // search the map for the register info
    private ABBA1700Register findRegister(String name) throws IOException {
       ABBA1700Register register = (ABBA1700Register)registers.get(name);
        if (register == null) {
            throw new IOException("ABBA1700RegisterFactory, findRegister, " + name + " does not exist!");
        } else {
            return register;
        }
    }

    /**
     * Getter for property meterType.
     *
     * @return Value of property meterType.
     */
    public ABBA1700MeterType getMeterType() {
        return meterType;
    }

    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }


}
