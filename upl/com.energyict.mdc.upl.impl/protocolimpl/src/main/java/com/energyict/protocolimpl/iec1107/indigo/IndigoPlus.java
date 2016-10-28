/*
 * IndigoPlus.java
 *
 * Created on 5 juli 2004, 14:56
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

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
public class IndigoPlus extends AbstractIEC1107Protocol implements SerialNumberSupport {

    private static final int DEBUG=0;

    LogicalAddressFactory logicalAddressFactory;
    IndigoProfile indigoProfile;
    int statusFlagChannel,readCurrentDay;
    int emptyNodeAddress;

    /** Creates a new instance of IndigoPlus */
    public IndigoPlus() {
        super(false,new Encryption());
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:14 +0200 (Thu, 26 Nov 2015)$";
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
        StringBuilder builder = new StringBuilder();
        builder.append("************************* Extended Logging *************************\n");
        builder.append(getLogicalAddressFactory().getMeterIdentity().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getMeterStatus().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getDefaultStatus().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getDateTimeGMT().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getDateTimeLocal().toString()).append("\n");

        builder.append(getLogicalAddressFactory().getTotalRegisters(extendedLogging - 1).toString()).append("\n");
        builder.append(getLogicalAddressFactory().getRateRegisters(extendedLogging - 1).toString()).append("\n");
        builder.append(getLogicalAddressFactory().getDemandRegisters(extendedLogging - 1).toString()).append("\n");
        builder.append(getLogicalAddressFactory().getDefaultRegisters(extendedLogging - 1).toString()).append("\n");
        builder.append(getLogicalAddressFactory().getHistoricalData(extendedLogging - 1).toString()).append("\n");

        builder.append(getLogicalAddressFactory().getMeteringDefinition().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getClockDefinition().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getBillingPeriodDefinition().toString()).append("\n");
        builder.append(getLogicalAddressFactory().getGeneralMeterData().toString()).append("\n");


        for (int i=-1;i<16;i++) {
            int billingPoint;
            if (i==-1) {
                billingPoint = 255;
            } else {
                billingPoint = i;
            }
            builder.append("Cumulative registers (total & tariff):\n");
            for(int obisC=1;obisC<=9;obisC++) {
                String code = "1.1."+obisC+".8.0."+billingPoint;
                builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
            for(int obisC=1;obisC<=2;obisC++) {
                for(int obisE=1;obisE<=9;obisE++) {
                    String code = "1.1."+obisC+".8."+obisE+"."+billingPoint;
                    builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
                }
            }
            String defaultRegCode = "1.2.1.8.0."+billingPoint;
            builder.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");
            defaultRegCode = "1.2.9.8.0."+billingPoint;
            builder.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");
            defaultRegCode = "1.2.129.8.0."+billingPoint;
            builder.append(defaultRegCode).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))).append(" default register\n");

            builder.append("Cumulative maximum demand registers:\n");
            builder.append(buildDemandReg(0,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(1,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(2,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(3,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));

            builder.append("Current average registers:\n");
            builder.append(buildDemandReg(0,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            builder.append(buildDemandReg(1,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            builder.append(buildDemandReg(2,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            builder.append(buildDemandReg(3,ObisCode.CODE_D_RISING_DEMAND,billingPoint));

            builder.append("Maximum demand registers:\n");
            builder.append(buildDemandReg(0,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(1,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(2,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            builder.append(buildDemandReg(3,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));

            builder.append("General purpose registers:\n");
            if( billingPoint != 255) {
                String code = "1.1.0.1.2."+billingPoint;
                builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
            if( billingPoint == 255) {
               String code = "1.1.0.1.0.255";
               builder.append(code).append(", ").append(ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))).append("\n");
            }
        }
        return builder.toString();
    }

    private String buildDemandReg(int index, int obisD, int billingPoint) throws IOException {
       int obisC;
       if (obisD == ObisCode.CODE_D_RISING_DEMAND) {
           obisC = DemandRegisters.OBISCMAPPINGRISINGDEMAND[index];
       } else {
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
            throw new ProtocolConnectionException("setTime() error, possibly wrong password level! "+e.toString(), e.getReason());
        }
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList(
                    "StatusFlagChannel",
                    "ReadCurrentDay",
                    "EmptyNodeAddress");
    }

    protected void doValidateProperties(java.util.Properties properties) throws MissingPropertyException, InvalidPropertyException {
        statusFlagChannel = Integer.parseInt(properties.getProperty("StatusFlagChannel","0"));
        readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay","0"));
        emptyNodeAddress = Integer.parseInt(properties.getProperty("EmptyNodeAddress","0"));
        setNodeId(properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(),(emptyNodeAddress==0?"001":"")));
    }

    public String getFirmwareVersion() throws IOException {
        return getLogicalAddressFactory().getMeterIdentity().getSoftwareVersionNumber();

    }

    public String getSerialNumber() {
        try {
            return getLogicalAddressFactory().getMeterIdentity().getMeterId();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries()+1);
        }
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

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();
    static {
           EXCEPTION_INFO_MAP.put("ERRDAT","Error setting the time");
           EXCEPTION_INFO_MAP.put("ERRADD","Protocol error");
    }

    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
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
