/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IndigoPlus.java
 *
 * Created on 5 juli 2004, 14:56
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// KV TO_DO
/*
 * Scaling of registers and profiledata using MeterDefinition and HistoricalData
 * Interpretation of profile daily flags and channel status flags
 * Determine difference between gaps and zero-consumption
 * Behaviour of whole current meters
 * Implementation and mapping of registers
 * Test with IMServ production meters
 * Set time!!!
 * Password encryption level 2 and 3?
 */

/**
 *
 * @author  Koen
 * @beginchanges
KV|05092004|Add infotype property StatusFlagChannel
KV|26112004|Set default nodeaddress to 001!
KV|01122004|Ignore negative values...
||Change statuschannel mechanism...
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
KV|18052005|Workaround to avoid exception when meter does not respond correct with channel status flag data
KV|27072005|Use CTVT logical address instead of current historical value logical address
KV|15122005|Test if protocol retrieved new interval data, intervals retrieved between from and to
 * @endchanges
 */
public class IndigoPlus extends AbstractIEC1107Protocol {

    @Override
    public String getProtocolDescription() {
        return "Actaris Indigo+ IEC1107";
    }

    LogicalAddressFactory logicalAddressFactory;
    IndigoProfile indigoProfile;
    int statusFlagChannel,readCurrentDay;
    int emptyNodeAddress;

    @Inject
    public IndigoPlus(PropertySpecService propertySpecService) {
        super(propertySpecService, false, new Encryption());
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return indigoProfile.getProfileData(calendarFrom.getTime(),calendarTo.getTime(),isStatusFlagChannel(),isReadCurrentDay());
    }

    public int getNumberOfChannels() throws IOException {
       int nrOfChannels = getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();
       if ((!(isStatusFlagChannel())) && (getLogicalAddressFactory().getMeteringDefinition().isChannelUnitsStatusFlagsChannel())) {
           return nrOfChannels - 1;
       }
       return nrOfChannels;
    }

    protected void doConnect() throws java.io.IOException {
        logicalAddressFactory = new LogicalAddressFactory(this, this);
        indigoProfile = new IndigoProfile(this, this,logicalAddressFactory);
    }

    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("************************* Extended Logging *************************\n");
        strBuff.append(getLogicalAddressFactory().getMeterIdentity().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getMeterStatus().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getDefaultStatus().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeGMT().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeLocal().toString()).append("\n");

        strBuff.append(getLogicalAddressFactory().getTotalRegisters(extendedLogging - 1).toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getRateRegisters(extendedLogging - 1).toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getDemandRegisters(extendedLogging - 1).toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getDefaultRegisters(extendedLogging - 1).toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getHistoricalData(extendedLogging - 1).toString()).append("\n");

        strBuff.append(getLogicalAddressFactory().getMeteringDefinition().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getClockDefinition().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getBillingPeriodDefinition().toString()).append("\n");
        strBuff.append(getLogicalAddressFactory().getGeneralMeterData().toString()).append("\n");


        for (int i=-1;i<16;i++) {
            int billingPoint;
            if (i==-1) {
                billingPoint = 255;
            }
            else {
                billingPoint = i;
            }
            strBuff.append("Cumulative registers (total & tariff):\n");
            for(int obisC=1;obisC<=9;obisC++) {
                String code = "1.1."+obisC+".8.0."+billingPoint;
                strBuff.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
            for(int obisC=1;obisC<=2;obisC++) {
                for(int obisE=1;obisE<=9;obisE++) {
                    String code = "1.1."+obisC+".8."+obisE+"."+billingPoint;
                    strBuff.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
                }
            }
            String defaultRegCode = "1.2.1.8.0."+billingPoint;
            strBuff.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");
            defaultRegCode = "1.2.9.8.0."+billingPoint;
            strBuff.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");
            defaultRegCode = "1.2.129.8.0."+billingPoint;
            strBuff.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");

            strBuff.append("Cumulative maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));

            strBuff.append("Current average registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_RISING_DEMAND,billingPoint));

            strBuff.append("Maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));

            strBuff.append("General purpose registers:\n");
            if( billingPoint != 255) {
                String code = "1.1.0.1.2."+billingPoint;
                strBuff.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
            if( billingPoint == 255) {
               String code = "1.1.0.1.0.255";
               strBuff.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
        }
        return strBuff.toString();
    }

    private String buildDemandReg(int index, int obisD, int billingPoint) throws IOException {
       int obisC;
       if (obisD == ObisCode.CODE_D_RISING_DEMAND) {
           obisC = DemandRegisters.OBISCMAPPINGRISINGDEMAND[index];
       }
       else {
           obisC = getLogicalAddressFactory().getHistoricalData(billingPoint == 255 ? 0 : billingPoint).getObisC(index);
       }
       if (obisC != 255) {
          String code = "1.1."+obisC+"."+obisD+".0."+billingPoint;
          return code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n";
       }
       return "";
    }

    public Date getTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        calendar.setTime(getLogicalAddressFactory().getDateTimeGMT().getDate());
        return calendar.getTime();
    }

    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getRoundtripCorrection());
        setTime(calendar.getTime());
    }

    private void setTime(Date date) throws IOException {
        try {
           getLogicalAddressFactory().setDateTimeGMT(date);
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("setTime() error, possibly wrong password level! "+e.toString());
        }
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList("StatusFlagChannel", "ReadCurrentDay", "EmptyNodeAddress");
    }

    protected void doValidateProperties(java.util.Properties properties) throws MissingPropertyException, InvalidPropertyException {
        statusFlagChannel = Integer.parseInt(properties.getProperty("StatusFlagChannel","0"));
        readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay","0"));
        emptyNodeAddress = Integer.parseInt(properties.getProperty("EmptyNodeAddress","0"));
        setNodeId(properties.getProperty(MeterProtocol.NODEID,(emptyNodeAddress==0?"001":"")));
    }

    public String getFirmwareVersion() throws IOException {
        return getLogicalAddressFactory().getMeterIdentity().getSoftwareVersionNumber();

    }

    public String getSerialNumber() throws IOException {
       return getLogicalAddressFactory().getMeterIdentity().getMeterId();
    }

    /*
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {

        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getSerialNumber().trim();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());

    }

    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }

    /*******************************************************************************************
     M e t e r E x c e p t i o n I n f o  i n t e r f a c e
     *******************************************************************************************/
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and
     *  use code in method as a sample of how to retrieve the error code.
     *  This code has been taken from a real protocol implementation.
     */

    static Map<String, String> exceptionInfoMap = new HashMap<>();
    static {
           exceptionInfoMap.put("ERRDAT","Error setting the time");
           exceptionInfoMap.put("ERRADD","Protocol error");
    }

    public String getExceptionInfo(String id) {

        String exceptionInfo = exceptionInfoMap.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        }
        else {
            return "No meter specific exception info for " + id;
        }
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }

    /**
     * Getter for property statusFlagChannel.
     * @return Value of property statusFlagChannel.
     */
    public boolean isStatusFlagChannel() {
        return statusFlagChannel==1;
    }

    public boolean isReadCurrentDay() {
        return readCurrentDay==1;
    }
 }
