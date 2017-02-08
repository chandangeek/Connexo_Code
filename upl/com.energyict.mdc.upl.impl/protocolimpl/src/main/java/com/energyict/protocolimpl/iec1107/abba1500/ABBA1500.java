package com.energyict.protocolimpl.iec1107.abba1500;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the ABBA1500 meter protocol.
 *         <BR>
 *         <B>@beginchanges</B><BR>
 *         KV|20012005|Initial version
 *         KV|23032005|Changed header to be compatible with protocol version tool
 *         KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 *         KV|06092005|VDEW changed to do channel mapping!
 *         KV|20092005|Add VDEWCompatible custom property to allow alternative time setting
 *         KV|20042007|Fix registerreading
 *         KV|16112007|Add workaround due to a meterbug (DataReadoutRequest=2)
 *         KV|13122007|Avoid index out of bound exception and retry for datareadout reception
 *         KV|17012008|Add forced delay as property and add reconnect to connection layer in case of break received during protocolsession
 *         GN|25032008|Added roundTripTime to correct the readout time when retries have occurred
 *         JME|05012009|Added filter for CORRUPTED flag when PU or PD for firmware 3.02
 *         JME|05012009|Added eventTime to billingPointRegister (Obiscode = 1.1.0.1.0.255) to get the last billing reset time.
 *         JME|22012009|Removed break command after dataReadout, to prevent non responding meter issues.
 * @version 1.0
 * @endchanges
 */
public class ABBA1500 extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, SerialNumberSupport {

    private final PropertySpecService propertySpecService;
    private String strID;
    private String strPassword;
    private String serialNumber;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int profileInterval;
    private ChannelMap channelMap;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int dataReadoutRequest;
    private String strDateFormat;

    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;
    private String iFirmwareVersion = "";

    private FlagIEC1107Connection flagIEC1107Connection = null;
    private ABBA1500Registry abba1500Registry = null;
    private ABBA1500Profile abba1500Profile = null;
    private ObisCode serialNumbObisCode = ObisCode.fromString("1.0.0.0.0.255");

    private List<RegisterValue> registerValues = null;

    private byte[] dataReadout = null;
    private boolean profileDateRead = false;

    private boolean software7E1;

    private int forcedDelay;
    private int MaxNrOfDaysProfileData;

    public ABBA1500(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        profileDateRead = true;
        return getAbba1500Profile().getProfileData(lastReading, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        profileDateRead = true;
        return getAbba1500Profile().getProfileData(from, to, includeEvents);
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void setTime() throws IOException {
        if ((getDataReadoutRequest() != 2) || profileDateRead) {
            if (vdewCompatible == 1) {
                setTimeVDEWCompatible();
            } else {
                setTimeAlternativeMethod();
            }
        }
    }

    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1500Registry().setRegister("TimeDate2", date);
    }

    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1500Registry().setRegister("Time", date);
        getAbba1500Registry().setRegister("Date", date);
    }

    @Override
    public Date getTime() throws IOException {
        if ((getDataReadoutRequest() != 2) | profileDateRead) {
            long roundTripTime = Calendar.getInstance().getTime().getTime();
            Date date = (Date) getAbba1500Registry().getRegister("TimeDate");
            roundTripTime = Calendar.getInstance().getTime().getTime() - roundTripTime;
            return new Date(date.getTime() - roundTripTime);
        } else {
            return new Date();
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.stringSpec(NODEID.getName()),
                this.integerSpec("EchoCancelling"),
                this.integerSpec("IEC1107Compatible"),
                this.integerSpec(PROFILEINTERVAL.getName()),
                this.integerSpec("RequestHeader"),
                this.stringSpec("ChannelMap"),
                this.integerSpec("DataReadout"),
                this.integerSpec("ExtendedLogging"),
                this.integerSpec("VDEWCompatible"),
                this.integerSpec("ForcedDelay"),
                this.integerSpec(SERIALNUMBER.getName()),
                this.stringSpec("FirmwareVersion"),
                this.stringSpec("Software7E1"),
                this.integerSpec("MaxNrOfDaysProfileData"),
                this.stringSpec("DateFormat"));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim());
            nodeId = properties.getTypedProperty(NODEID.getName(), "");
            iEchoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getTypedProperty("IEC1107Compatible", "1").trim());
            profileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName(), "3600").trim());
            channelMap = new ChannelMap(properties.getTypedProperty("ChannelMap", "0"));
            requestHeader = Integer.parseInt(properties.getTypedProperty("RequestHeader", "1").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "0,0,0,0"));
            dataReadoutRequest = Integer.parseInt(properties.getTypedProperty("DataReadout", "0").trim());
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getTypedProperty("VDEWCompatible", "1").trim());
            forcedDelay = Integer.parseInt(properties.getTypedProperty("ForcedDelay", "0").trim());
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            iFirmwareVersion = properties.getTypedProperty("FirmwareVersion", "3.03").trim();
            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
            this.MaxNrOfDaysProfileData = Integer.parseInt(properties.getTypedProperty("MaxNrOfDaysProfileData", "0").trim());
            strDateFormat = properties.getTypedProperty("DateFormat", "yy/MM/dd").trim();
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    private int getDataReadoutRequest() {
        return dataReadoutRequest;
    }

    @Override
    public String getRegister(String name) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.getBytes());
        flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] data = flagIEC1107Connection.receiveRawData();
        return new String(data);
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        getAbba1500Registry().setRegister(name, value);
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:59 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() {
        return ("Unknown");
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty, iProtocolRetriesProperty, forcedDelay, iEchoCancelling, iIEC1107Compatible, software7E1, logger);
            abba1500Registry = new ABBA1500Registry(this, this, getDateFormat());
            abba1500Profile = new ABBA1500Profile(this, this, abba1500Registry);
            abba1500Profile.setFirmwareVersion(getIFirmwareVersion());
        } catch (ConnectionException e) {
            logger.severe("ABBA1500: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (getDataReadoutRequest() == 1)) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                // ABBA1500 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
                flagIEC1107Connection.disconnectMACWithoutBreak();
            }

            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);


            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (getDataReadoutRequest() == 1)) {
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

            if (!verifyMeterSerialNR()) {
                throw new ProtocolException("ABB A1500, connect, Wrong SerialNR!, EISerialNumber=" + serialNumber + ", MeterSerialNumber=" + getSerialNumber());
            }
        } catch (FlagIEC1107ConnectionException e) {
            throw new ProtocolConnectionException(e.getMessage(), e.getReason());
        }

        if (extendedLogging >= 1) {
            getRegistersInfo();
        }

    }

    @Override
    public String getSerialNumber() {
        RegisterValue serialInfo;
        try {
            serialInfo = readRegister(serialNumbObisCode);
            return serialInfo.getText();
        } catch (IOException e){
            throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    private boolean verifyMeterSerialNR() {
        return (serialNumber == null) ||
                ("".compareTo(serialNumber) == 0) ||
                (serialNumber.compareTo(getSerialNumber()) == 0);
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
            throw new ProtocolConnectionException(e.getMessage(), e.getReason());
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (requestHeader == 1) {
            return getAbba1500Profile().getProfileHeader().getNrOfChannels();
        } else {
            return getProtocolChannelMap().getNrOfProtocolChannels();
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (requestHeader == 1) {
            return getAbba1500Profile().getProfileHeader().getProfileInterval();
        } else {
            return profileInterval;
        }
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getDateFormat() {
        return strDateFormat;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public byte[] getDataReadout() {
        if ((dataReadout == null) && (getDataReadoutRequest() == 2)) {
            try {
                flagIEC1107Connection.disconnectMAC();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                }
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new NestedIOException(e);
                }
            } catch (IOException e) {
                getLogger().severe("getDataReadout(), error reading datareadout, " + e.toString());
            }
        }
        return dataReadout;
    }

    @Override
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();

    static {
        EXCEPTION_INFO_MAP.put("ERROR", "Request could not execute!");
        EXCEPTION_INFO_MAP.put("ERROR01", "A1500 ERROR 01, invalid command!");
        EXCEPTION_INFO_MAP.put("ERROR06", "A1500 ERROR 06, invalid command!");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    @Override
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }

    @Override
    public boolean isRequestHeader() {
        return requestHeader == 1;
    }

    @Override
    public com.energyict.protocolimpl.base.ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();
            int VZ = Math.abs(obisCode.getF());
            obisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint - VZ);

            // read the non billing register to reuse the unit in case of billingpoints...
            try {
                doReadRegister(new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 255), false);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            // read the billing point timestamp
            try {
                doReadRegister(new ObisCode(1, 1, 0, 1, 2, billingPoint - VZ), true);
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

        } // if (obisCode.getF() != 255)


        // JME:	Special case for obiscode == 1.1.0.1.0.255 (billing point):
        //		Read the date of the billing reset and apply it to the billingPointRegister as eventTime
        if (obisCode.toString().equalsIgnoreCase("1.1.0.1.0.255")) {
            RegisterValue billingPointRegister = doReadRegister(ObisCode.fromString("1.1.0.1.0.255"), false);
            int billingPoint = billingPointRegister.getQuantity().intValue();

            RegisterValue reg_date = null;
            try {
                reg_date = doReadRegister(new ObisCode(1, 1, 0, 1, 2, billingPoint), true);
                if (reg_date != null) {
                    billingPointRegister = new RegisterValue(
                            billingPointRegister.getObisCode(),
                            billingPointRegister.getQuantity(),
                            reg_date.getToTime(), // eventTime from billing point
                            billingPointRegister.getFromTime(),
                            billingPointRegister.getToTime(),
                            billingPointRegister.getReadTime(),
                            billingPointRegister.getRtuRegisterId(),
                            billingPointRegister.getText()
                    );
                }
            } catch (NoSuchRegisterException e) {
                // absorb if not exist...
            }

            return billingPointRegister;
        }

        return doReadRegister(obisCode, false);
    }

    private RegisterValue doReadRegister(ObisCode obisCode, boolean billingTimestamp) throws IOException {
        RegisterValue registerValue = findRegisterValue(obisCode);
        if (registerValue == null) {
            if (billingTimestamp) {
                registerValue = doTheReadBillingRegisterTimestamp(obisCode);
            } else {
                registerValue = doTheReadRegister(obisCode);
            }
            registerValues.add(registerValue);
        }
        return registerValue;
    }

    private byte[] readRegisterData(ObisCode obisCode) throws IOException {
        String edisNotation = obisCode.getC() + "." + obisCode.getD() + "." + obisCode.getE() + (obisCode.getF() == 255 ? "" : "*" + ProtocolUtils.buildStringDecimal(Math.abs(obisCode.getF()), 2));
        byte[] data = null;
        if (getDataReadoutRequest() == 0) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            if (edisNotation.contains("97.97.0")) {
                data = ddp.getRegisterFFStrValue("F.F").getBytes();
            } else {
                data = ddp.getRegisterStrValue(edisNotation).getBytes();
            }
        }
        return data;
    }

    private Quantity parseQuantity(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseQuantityBetweenBrackets(data, 0, 0);
    }

    private Date parseDate(byte[] data, int pos) throws IOException {
        Date date = null;
        try {
            DataParser dp = new DataParser(getTimeZone());
            VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
            String dateStr = dp.parseBetweenBrackets(data, 0, pos);
            if ("".compareTo(dateStr) == 0) {
                return null;
            }
            vts.setStrDateFormat(strDateFormat);
            vts.parse(dateStr);
            date = vts.getCalendar().getTime();
            return date;
        } catch (DataParseException e) {
            //absorb
            return null;
        }
    }

    private RegisterValue doTheReadRegister(ObisCode obisCode) throws IOException {
        try {

            byte[] data = readRegisterData(obisCode);

            if (obisCode.equals(serialNumbObisCode)) {
                String text = parseText(data);
                return new RegisterValue(obisCode, null, null, null, null, null, 0, text);
            }

            Quantity quantity = parseQuantity(data);
            Date date = parseDate(data, 1);
            Date billlingDate = null;
            RegisterValue registerValue;

            // in case of unitless AND billing register
            // find the non billing register and use that unit if the non billing register exist
            // also find the timestamp for that billingpoint and add it to the registervalue
            if (quantity.getBaseUnit().getDlmsCode() == BaseUnit.UNITLESS && obisCode.getF() != 255) {
                registerValue = findRegisterValue(ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255));
                if (registerValue != null) {
                    quantity = new Quantity(quantity.getAmount(), registerValue.getQuantity().getUnit());
                }

            }
            if (obisCode.getF() != 255) {
                registerValue = findRegisterValue(new ObisCode(1, 1, 0, 1, 2, obisCode.getF()));
                if (registerValue != null) {
                    billlingDate = registerValue.getToTime();
                }
            }

            return new RegisterValue(obisCode, quantity, date, billlingDate);
        } catch (NoSuchRegisterException | NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new ProtocolConnectionException("doTheReadRegister(), error, " + e.getMessage(), e.getReason());
        } catch (IOException e) {
            throw new IOException("doTheReadRegister(), error, " + e.getMessage());
        }
    }

    private String parseText(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        return dp.parseBetweenBrackets(data, 0, 0);
    }

    private RegisterValue doTheReadBillingRegisterTimestamp(ObisCode obisCode) throws IOException {
        try {
            byte[] data = readRegisterData(obisCode);
            Date date = parseDate(data, 0);
            return new RegisterValue(obisCode, null, null, date);
        } catch (NoSuchRegisterException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        } catch (FlagIEC1107ConnectionException e) {
            throw new ProtocolConnectionException("doTheReadBillingRegisterTimestamp(), error: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!" + e.getMessage());
        }catch (IOException e) {
            throw new IOException("doTheReadBillingRegisterTimestamp(), error: " + e.getMessage());
        }
    }

    private RegisterValue findRegisterValue(com.energyict.obis.ObisCode obisCode) {
        if (registerValues == null) {
            registerValues = new ArrayList<>();
        } else {
            for (RegisterValue r : registerValues) {
                if (r.getObisCode().equals(obisCode)) {
                    return r;
                }
            }
        }
        return null;
    }

    @Override
    public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.toString());
    }

    private void getRegistersInfo() {
        StringBuilder builder = new StringBuilder();
        if (getDataReadoutRequest() == 1) {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append(new String(getDataReadout()));
        } else {
            builder.append("******************* ExtendedLogging *******************\n");
            builder.append("All OBIS codes are translated to EDIS codes but not all codes are configured in the meter.\n");
            builder.append("It is not possible to retrieve a list with all registers in the meter. Consult the configuration of the meter.");
            builder.append("\n");
        }
        logger.info(builder.toString());
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, getDataReadoutRequest() == 1);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty, iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }

    @Override
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

    private com.energyict.protocolimpl.iec1107.abba1500.ABBA1500Registry getAbba1500Registry() {
        return abba1500Registry;
    }

    private com.energyict.protocolimpl.iec1107.abba1500.ABBA1500Profile getAbba1500Profile() {
        return abba1500Profile;
    }

    private String getIFirmwareVersion() {
        return iFirmwareVersion;
    }

    int getMaxNrOfDaysProfileData() {
        return MaxNrOfDaysProfileData;
    }

}