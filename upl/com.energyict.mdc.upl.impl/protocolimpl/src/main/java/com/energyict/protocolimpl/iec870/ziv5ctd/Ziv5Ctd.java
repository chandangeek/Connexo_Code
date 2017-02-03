package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;

/**
 * @author fbo
 * @beginchanges
 * @endchanges
 */
public class Ziv5Ctd extends PluggableMeterProtocol implements SerialNumber, RegisterProtocol, SerialNumberSupport {

    static final BigDecimal MAX_PROFILE_VALUE = new BigDecimal(9999999);

    /**
     * Property keys
     */
    private static final String PK_TIMEOUT = Property.TIMEOUT.getName();
    private static final String PK_RETRIES = Property.RETRIES.getName();
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PK_FETCH_PROGRAM_PROFILE = "FetchProgramProfile";
    private static final String PK_CUMULATIVE_PROFILE = "CumulativeProfile";

    /**
     * Property Default values
     */
    private static final String PD_NODE_ID = "";
    private static final int PD_TIMEOUT = 10000;
    private static final int PD_RETRIES = 5;
    private static final int PD_ROUNDTRIP_CORRECTION = 0;
    private static final int PD_SECURITY_LEVEL = 2;
    private static final String PD_EXTENDED_LOGGING = "0";
    private static final boolean PD_CUMULATIVE_PROFILE = true;
    private final PropertySpecService propertySpecService;

    /**
     * Property values Required properties will have NO default value Optional
     * properties make use of default value
     */
    private String pAddress = null;
    private String pNodeId = PD_NODE_ID;
    private String pSerialNumber = null;
    private int pProfileInterval;
    private int pPassword;

    /* Protocol timeout fail in msec */
    private int pTimeout = PD_TIMEOUT;

    /* Max nr of consecutive protocol errors before end of communication */
    private int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    private int pCorrectTime = 0;

    private String pFetchProgramProfile = "0";
    boolean pCumulativeProfile = PD_CUMULATIVE_PROFILE;
    private String pExtendedLogging = PD_EXTENDED_LOGGING;

    LinkLayer linkLayer;
    FrameFactory frameFactory;
    private AsduFactory asduFactory;

    private RegisterFactory rFactory = null;
    private ObisCodeMapper obisCodeMapper = null;
    private TimeZone timeZone = null;
    private Logger logger = null;

    public Ziv5Ctd(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getSerialNumber() {
        try {
            return rFactory.getInfoObject47().getProductCode();
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, pRetries + 1);
        }
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
                this.integerSpec(CORRECTTIME.getName()),
                this.stringSpec(PK_EXTENDED_LOGGING),
                this.stringSpec(PK_FETCH_PROGRAM_PROFILE),
                this.stringSpec(PK_CUMULATIVE_PROFILE));
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
    public void setUPLProperties(TypedProperties p) throws InvalidPropertyException, MissingPropertyException {
        try {
            if (p.getTypedProperty(ADDRESS.getName()) != null) {
                pAddress = p.getTypedProperty(ADDRESS.getName());
            }

            if (p.getTypedProperty(NODEID.getName()) != null) {
                pNodeId = p.getTypedProperty(NODEID.getName());
            }

            if (p.getTypedProperty(SERIALNUMBER.getName()) != null) {
                pSerialNumber = p.getTypedProperty(SERIALNUMBER.getName());
            }

            if (p.getTypedProperty(PROFILEINTERVAL.getName()) != null) {
                pProfileInterval = Integer.parseInt(p.getTypedProperty(PROFILEINTERVAL.getName()));
            }

            if (p.getTypedProperty(Property.PASSWORD.getName()) != null) {
                pPassword = Integer.parseInt(p.getTypedProperty(Property.PASSWORD.getName()));
            }

            if (p.getTypedProperty(PK_TIMEOUT) != null) {
                pTimeout = Integer.parseInt(p.getTypedProperty(PK_TIMEOUT));
            }

            if (p.getTypedProperty(PK_RETRIES) != null) {
                pRetries = Integer.parseInt(p.getTypedProperty(PK_RETRIES));
            }

            if (p.getTypedProperty(ROUNDTRIPCORRECTION.getName()) != null) {
                pRountTripCorrection = Integer.parseInt(p.getTypedProperty(ROUNDTRIPCORRECTION.getName()));
            }

            if (p.getTypedProperty(CORRECTTIME.getName()) != null) {
                pCorrectTime = Integer.parseInt(p.getTypedProperty(CORRECTTIME.getName()));
            }

            if (p.getTypedProperty(PK_EXTENDED_LOGGING) != null) {
                pExtendedLogging = p.getTypedProperty(PK_EXTENDED_LOGGING);
            }

            if (p.getTypedProperty(PK_FETCH_PROGRAM_PROFILE) != null) {
                pFetchProgramProfile = p.getTypedProperty(PK_FETCH_PROGRAM_PROFILE);
            }

            if (p.getTypedProperty(PK_CUMULATIVE_PROFILE) != null) {
                pCumulativeProfile = ("1".equals(p.getTypedProperty(PK_CUMULATIVE_PROFILE)));
            }
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            TypeIdentificationFactory tif = new TypeIdentificationFactory(timeZone);
            asduFactory = new AsduFactory(Address.DEFAULT, tif);
            frameFactory = new FrameFactory(Address.DEFAULT, asduFactory);
            linkLayer = new LinkLayer(inputStream, outputStream, 0, 0, this, pRetries);
            rFactory = new RegisterFactory(this, asduFactory);
            obisCodeMapper = new ObisCodeMapper(this, rFactory);
        } catch (ConnectionException e) {
            logger.severe("Ziv5Ctd, " + e.getMessage());
            throw e;
        }

        if (logger.isLoggable(Level.INFO)) {
            String infoMsg =
                    "ZIV protocol init \n"
                            + " Address = " + pAddress + ","
                            + " Node Id = " + pNodeId + ","
                            + " SerialNr = " + pSerialNumber + ","
                            + " Psswd = " + pPassword + ","
                            + " Timeout = " + pTimeout + ","
                            + " Retries = " + pRetries + ","
                            + " Ext. Logging = " + pExtendedLogging + ","
                            + " RoundTripCorr = " + pRountTripCorrection + ","
                            + " Correct Time = " + pCorrectTime + ","
                            + " TimeZone = " + timeZone.getID();

            logger.info(infoMsg);
        }
    }

    @Override
    public void connect() throws IOException {
        try {
            linkLayer.connect();
            linkLayer.requestRespond(asduFactory.createType0xB7(pPassword));
            doExtendedLogging();

        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        rFactory = null;
        obisCodeMapper = null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 6;   // always 6 channels ...
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = Calendar.getInstance(timeZone);

        Date to = c.getTime();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        Date from = c.getTime();

        return getProfileData(from, to, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        int registerAddress = 0x0b;
        if ("1".equals(pFetchProgramProfile)) {
            registerAddress = 0x0c;
        }

        Asdu a;
        if (pCumulativeProfile) {
            a = asduFactory.create0x7a(registerAddress, from, to);
        } else {
            a = asduFactory.create0x7b(registerAddress, from, to);
        }

        ApplicationFunction appFunction = new ApplicationFunction(this);
        ProfileData result = (ProfileData) appFunction.read(a);

        result.generateEvents();

        if (includeEvents) {
            Iterator ei = rFactory.getMeterEvents(from, to).iterator();
            while (ei.hasNext()) {
                result.addEvent((MeterEvent) ei.next());
            }
        }

        return result;
    }

    @Override
    public int getProfileInterval() throws IOException {
        if ("1".equals(pFetchProgramProfile))
        // programmed profile has a configurable integration time
        {
            return pProfileInterval;
        } else
        // standard profile always has an integration time of 1 hour
        {
            return 3600;
        }
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel cChannel = discoverInfo.getCommChannel();
        String nodeId = discoverInfo.getNodeId();
        int baudrate = discoverInfo.getBaudrate();

        TypedProperties p = com.energyict.protocolimpl.properties.TypedProperties.empty();
        p.setProperty(NODEID.getName(), nodeId == null ? "" : nodeId);
        setUPLProperties(p);

        init(cChannel.getInputStream(), cChannel.getOutputStream(), null, null);
        connect();
        String serialNumber = rFactory.getInfoObject47().getProductCode();
        disconnect();
        return serialNumber;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    private void doExtendedLogging() throws IOException {
        if ("1".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getExtendedLogging() + "\n");
        }
        if ("2".equals(pExtendedLogging)) {
            logger.log(Level.INFO, obisCodeMapper.getDebugLogging() + "\n");
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:46 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(int channelId) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Quantity getMeterReading(String name) throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public Date getTime() throws IOException {
        return rFactory.get48().getDate();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, pRountTripCorrection);

        CP56Time time = new CP56Time(timeZone, calendar.getTime());
        Asdu a = asduFactory.createType0xB5(time);
        ApplicationFunction appFunction = new ApplicationFunction(this);
        appFunction.read(a);
    }

    @Override
    public String getRegister(String name) {
        return null;
    }

    @Override
    public void setRegister(String name, String value) {
    }

    @Override
    public void initializeDevice() {
    }

    @Override
    public void release() {
    }

    public boolean isRequestHeader() {
        return false;
    }

    public byte[] getDataReadout() {
        return null;
    }

    void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    TimeZone getTimeZone() {
        return timeZone;
    }

    void setLogger(Logger logger) {
        this.logger = logger;
    }

}