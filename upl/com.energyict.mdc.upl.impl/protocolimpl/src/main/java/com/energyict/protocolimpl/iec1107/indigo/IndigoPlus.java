/*
 * IndigoPlus.java
 *
 * Created on 5 juli 2004, 14:56
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;


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

    private LogicalAddressFactory logicalAddressFactory;
    private IndigoProfile indigoProfile;
    private int statusFlagChannel,readCurrentDay;
    private int emptyNodeAddress;

    public IndigoPlus(PropertySpecService propertySpecService, NlsService nlsService) {
        super(false,new Encryption(), propertySpecService, nlsService);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Thu Dec 29 09:39:05 2016 +0100 $";
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return indigoProfile.getProfileData(calendarFrom.getTime(),calendarTo.getTime(),isStatusFlagChannel(),isReadCurrentDay());
    }

    @Override
    public int getNumberOfChannels() throws IOException {
       int nrOfChannels = getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();
       if ((!(isStatusFlagChannel())) && (getLogicalAddressFactory().getMeteringDefinition().isChannelUnitsStatusFlagsChannel())) {
           return nrOfChannels - 1;
       }
       return nrOfChannels;
    }

    @Override
    protected void doConnect() throws java.io.IOException {
        logicalAddressFactory = new LogicalAddressFactory(this, this);
        indigoProfile = new IndigoProfile(this, this,logicalAddressFactory);
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        // extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
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

    @Override
    public Date getTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        calendar.setTime(getLogicalAddressFactory().getDateTimeGMT().getDate());
        return calendar.getTime();
    }

    @Override
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

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("StatusFlagChannel", PropertyTranslationKeys.IEC1107_STATUS_FLAG_CHANNEL));
        propertySpecs.add(this.integerSpec("ReadCurrentDay", PropertyTranslationKeys.IEC1107_READ_CURRENT_DAY));
        propertySpecs.add(this.integerSpec("EmptyNodeAddress", PropertyTranslationKeys.IEC1107_EMPTY_NODE_ADDRESS));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        super.setUPLProperties(properties);
        try {
            statusFlagChannel = Integer.parseInt(properties.getTypedProperty("StatusFlagChannel", "0"));
            readCurrentDay = Integer.parseInt(properties.getTypedProperty("ReadCurrentDay", "0"));
            emptyNodeAddress = Integer.parseInt(properties.getTypedProperty("EmptyNodeAddress", "0"));
            setNodeId(properties.getTypedProperty(NODEID.getName(), emptyNodeAddress==0?"001":""));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getLogicalAddressFactory().getMeterIdentity().getSoftwareVersionNumber();
    }

    @Override
    public String getSerialNumber() {
        try {
            return getLogicalAddressFactory().getMeterIdentity().getMeterId();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries()+1);
        }
    }

    private com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();
    static {
           EXCEPTION_INFO_MAP.put("ERRDAT","Error setting the time");
           EXCEPTION_INFO_MAP.put("ERRADD","Protocol error");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }

    private boolean isStatusFlagChannel() {
        return statusFlagChannel==1;
    }

    private boolean isReadCurrentDay() {
        return readCurrentDay==1;
    }

}