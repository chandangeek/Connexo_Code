/*
 * Unilog.java
 *
 * Created on 10 januari 2005, 09:19
 */

package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;

/**
 * @author fbo
 * @beginchanges FB|01022005|Initial version
 * KV|23032005|Changed header to be compatible with protocol version tool
 * KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
 * @endchanges
 */
public class Unilog extends AbstractUnilog implements SerialNumberSupport {

    /**
     * Property keys specific for PPM protocol.
     */
    private static final String PK_TIMEOUT = Property.TIMEOUT.getName();
    private static final String PK_RETRIES = Property.RETRIES.getName();
    private static final String PK_FORCE_DELAY = "ForceDelay";
    private static final String PK_ECHO_CANCELLING = "EchoCancelling";
    private static final String PK_IEC1107_COMPATIBLE = "IEC1107Compatible";
    private static final String PK_CHANNEL_MAP = "ChannelMap";

    /**
     * Property Default values
     */
    private static final String PD_PASSWORD = "kamstrup";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_PROFILE_INTERVAL = 3600;
    private static final long PD_FORCE_DELAY = 170;
    private static final int PD_ECHO_CANCELING = 0;
    private static final int PD_IEC1107_COMPATIBLE = 1;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final int PD_SECURITY_LEVEL = 1;
    private static final String PD_CHANNEL_MAP = "0,0";

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    private String pAddress = null;
    private String pNodeId = null;
    private String pSerialNumber = null;
    private String pPassword = PD_PASSWORD;

    private String pChannelMap = PD_CHANNEL_MAP;
    private int pProfileInterval = PD_PROFILE_INTERVAL;
    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;
    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Delay in msec between protocol Message Sequences */
    private long pForceDelay = PD_FORCE_DELAY;
    private int pEchoCanceling = PD_ECHO_CANCELING;
    private int pIec1107Compatible = PD_IEC1107_COMPATIBLE;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;

    private MeterType meterType;
    private FlagIEC1107Connection flagIEC1107Connection = null;
    private UnilogRegistry registry = null;
    private UnilogProfile profile = null;
    private ProtocolChannelMap protocolChannelMap = null;

    private boolean software7E1;
    private static final String PK_SOFTWARE_7E1 = "Software7E1";

    private final PropertySpecService propertySpecService;

    public Unilog(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                PK_TIMEOUT,
                PK_RETRIES,
                PK_ECHO_CANCELLING,
                ROUNDTRIPCORRECTION.getName(),
                PK_SOFTWARE_7E1,
                PK_CHANNEL_MAP);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(PROFILEINTERVAL.getName()),
                this.integerSpec(PK_TIMEOUT),
                this.integerSpec(PK_RETRIES),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(PK_FORCE_DELAY),
                this.integerSpec(PK_ECHO_CANCELLING),
                this.integerSpec(PK_IEC1107_COMPATIBLE),
                this.stringSpec(PK_SOFTWARE_7E1),
                ProtocolChannelMap.propertySpec(PK_CHANNEL_MAP, false));
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
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException {
        try {
            if (properties.getTypedProperty(ADDRESS.getName()) != null) {
                pAddress = properties.getTypedProperty(ADDRESS.getName());
            }

            if (properties.getTypedProperty(NODEID.getName()) != null) {
                pNodeId = properties.getTypedProperty(NODEID.getName());
            }

            if (properties.getTypedProperty(SERIALNUMBER.getName()) != null) {
                pSerialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            }

            if (properties.getTypedProperty(PASSWORD.getName()) != null) {
                pPassword = properties.getTypedProperty(PASSWORD.getName());
            }

            if (properties.getTypedProperty(PROFILEINTERVAL.getName()) != null) {
                pProfileInterval = Integer.parseInt(properties.getTypedProperty(PROFILEINTERVAL.getName()));
            }

            if (properties.getTypedProperty(PK_TIMEOUT) != null) {
                pTimeout = Integer.parseInt(properties.getTypedProperty(PK_TIMEOUT));
            }

            if (properties.getTypedProperty(PK_RETRIES) != null) {
                pRetries = Integer.parseInt(properties.getTypedProperty(PK_RETRIES));
            }

            if (properties.getTypedProperty(PK_FORCE_DELAY) != null) {
                pForceDelay = Integer.parseInt(properties.getTypedProperty(PK_FORCE_DELAY));
            }

            if (properties.getTypedProperty(PK_ECHO_CANCELLING) != null) {
                pEchoCanceling = Integer.parseInt(properties.getTypedProperty(PK_ECHO_CANCELLING));
            }

            if (properties.getTypedProperty(PK_IEC1107_COMPATIBLE) != null) {
                pIec1107Compatible = Integer.parseInt(properties.getTypedProperty(PK_IEC1107_COMPATIBLE));
            }

            if (properties.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
                pRountTripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName()));
            }

            this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty(PK_SOFTWARE_7E1, "0"));

            if (properties.getTypedProperty(Unilog.PK_CHANNEL_MAP) != null) {
                this.pChannelMap = properties.getTypedProperty(Unilog.PK_CHANNEL_MAP);
            }
            protocolChannelMap = new ProtocolChannelMap(pChannelMap);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public String getSerialNumber() {
        try {
            String meterSerial = meterType.getReceivedIdent();
            if (meterSerial == null) {
                throw new ProtocolException("SerialNumber mismatch! configured serial = " + pSerialNumber + " meter serial unknown");
            }
            return pSerialNumber;
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        setTimeZone(timeZone);
        setLogger(logger);
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, pTimeout, pRetries, pForceDelay, pEchoCanceling, pIec1107Compatible, software7E1, logger);
            registry = new UnilogRegistry(this, this);
            profile = new UnilogProfile(this, registry);
        } catch (ConnectionException e) {
            logger.severe("Unilog: init(...), " + e.getMessage());
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            meterType = flagIEC1107Connection.connectMAC(pAddress, pPassword, PD_SECURITY_LEVEL, pNodeId);
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        }
    }

    @Override
    public int getProfileInterval() throws IOException {
        return pProfileInterval;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return profile.getProfileData(fromCalendar, toCalendar, getNumberOfChannels(), 1);
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        Date date = calendar.getTime();
        registry.setRegister("0.9.1", date);
        registry.setRegister("0.9.2", date);
    }

    @Override
    public Date getTime() throws IOException {
        Date date = (Date) registry.getRegister(registry.R_TIME_DATE);
        return new Date(date.getTime() - pRountTripCorrection);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return protocolChannelMap.getNrOfProtocolChannels();
    }

    @Override
    public String getPassword() {
        return pPassword;
    }

    @Override
    public int getNrOfRetries() {
        return pRetries;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-30 13:55:02 +0100 (Mon, 30 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() {
        return "Unknown";
    }

    @Override
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return (pIec1107Compatible == 1);
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            Object register = registry.getRegister(obisCode);
            if (register instanceof Quantity) {
                return new RegisterValue(obisCode, (Quantity) register);
            } else {
                return new RegisterValue(obisCode, register.toString());
            }
        } catch (IOException e) {
            throw new NoSuchRegisterException("Problems while reading register " + obisCode + ": " + e.getMessage());
        }
    }

}