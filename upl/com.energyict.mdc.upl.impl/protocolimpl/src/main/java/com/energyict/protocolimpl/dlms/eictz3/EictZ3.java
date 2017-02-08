package com.energyict.protocolimpl.dlms.eictz3;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.LocalSecurityProvider;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.dlms.Z3.AARQ;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.dlms.nta.eventhandling.DisconnectControlLog;
import com.energyict.protocolimpl.dlms.nta.eventhandling.EventsLog;
import com.energyict.protocolimpl.dlms.nta.eventhandling.FraudDetectionLog;
import com.energyict.protocolimpl.dlms.nta.eventhandling.MbusLog;
import com.energyict.protocolimpl.dlms.nta.eventhandling.PowerFailureLog;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * DLMS based {@link MeterProtocol} implementation for the Z3 and EpIO R2.
 * There is also a generic protocol implementation AbstractNTAProtocol.
 */
@Deprecated
public final class EictZ3 extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism, RegisterProtocol, MessageProtocol, SerialNumberSupport {

    /**
     * The name of the property containing the information field size.
     */
    private static final String PROPNAME_INFORMATION_FIELD_SIZE = "InformationFieldSize";

    /**
     * Name of the property containing the load profile OBIS code to fetch.
     */
    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";

    /**
     * The name of the property indicating the {@link DLMSConnectionMode}.
     */
    private static final String PROPNAME_CONNECTION = "Connection";

    /**
     * The name of the property containing the client addressing mode. See {@link ClientAddressingMode} for values.
     */
    private static final String PROPNAME_ADDRESSING_MODE = "AddressingMode";

    /**
     * The name of the property containing the server lower MAC address.
     */
    private static final String PROPNAME_SERVER_LOWER_MAC_ADDRESS = "ServerLowerMacAddress";

    /**
     * The name of the property containing the server qupper MAC address.
     */
    private static final String PROPNAME_SERVER_UPPER_MAC_ADDRESS = "ServerUpperMacAddress";

    /**
     * The name of the property containing the client MAC address.
     */
    private static final String PROPNAME_CLIENT_MAC_ADDRESS = "ClientMacAddress";

    /**
     * The name of the property containing the security level.
     */
    private static final String PROPNAME_SECURITY_LEVEL = SECURITYLEVEL.getName();

    /**
     * Indicates whether to request the time zone from the meter.
     */
    private static final String PROPNAME_REQUEST_TIME_ZONE = "RequestTimeZone";

    /**
     * Name of the property containing the number of retries.
     */
    private static final String PROPNAME_RETRIES = RETRIES.getName();

    /**
     * Name of the property containing the delay after failure in milliseconds.
     */
    private static final String PROPNAME_TIMEOUT = TIMEOUT.getName();

    /**
     * The maximum APDU size property name.
     */
    private static final String PROPNAME_MAX_APDU_SIZE = "MaxAPDUSize";

    /**
     * The name of the property containing the time we force a delay.
     */
    private static final String PROPNAME_FORCE_DELAY = "ForceDelay";

    /**
     * Name of the property containing the (fixed) roundtrop correction.
     */
    private static final String PROPNAME_ROUNDTRIP_CORRECTION = ROUNDTRIPCORRECTION.getName();

    /**
     * The name of the property containing the treshold in milliseconds we allow for a time request when determining the time shift. It defaults to 5 seconds (5000 ms).
     */
    private static final String PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD = "ClockSetRoundtripCorrectionTreshold";

    /**
     * Name of the property that contains the maximum number of MBus devices attached to the device. Default is 4.
     */
    private static final String PROPNAME_MAXIMUM_NUMBER_OF_MBUS_DEVICES = "MaxMbusDevices";

    /**
     * The default roundtrip correction treshold when setting the clock.
     */
    private static final int DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD = 5000;

    /**
     * The name of the property containing the maximum number of clockset tries. The algorithm will retry a when the roundtrip exceeds the value of {@link #clockSetRoundtripTreshold}, defined by the property {@value #PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD}.
     */
    private static final String PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = "MaximumNumberOfClockSetTries";

    /**
     * Default number of retries allows for 10 retries.
     */
    private static final int DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES = 10;

    /**
     * The OBIS code for the active firmware version.
     */
    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");

    /**
     * The OBIS code pointing to the serial number of the R2/Z3 itself.
     */
    private static final ObisCode OBISCODE_R2_SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");

    /**
     * The load profile format for an MBus meter depending on the physical address.
     */
    private static final MessageFormat MBUS_LOAD_PROFILE_FORMAT = new MessageFormat("0.{0}.24.3.0.255");

    /**
     * This is the default Obis code for the Epio.
     */
    private static final ObisCode OBIS_CODE_EPIO_LOAD_PROFILE = ObisCode.fromString("1.0.99.1.0.255");

    /**
     * Obis code we can use to request the RF network topology.
     */
    private static final ObisCode OBIS_CODE_NETWORK_TOPOLOGY = ObisCode.fromString("0.128.3.0.8.255");

    /**
     * Protocol status flag indicating load profile has been cleared.
     */
    private static final int CLEAR_LOADPROFILE = 0x4000;

    /**
     * Protocol status flag indicating logbook has been cleared.
     */
    private static final int CLEAR_LOGBOOK = 0x2000;

    /**
     * protocol status flag end of error.
     */
    private static final int END_OF_ERROR = 0x0400;

    /**
     * protocol status.
     */
    private static final int BEGIN_OF_ERROR = 0x0200;

    /**
     * protocol status.
     */
    private static final int VARIABLE_SET = 0x0100;

    /**
     * protocol status.
     */
    private static final int POWER_FAILURE = 0x0080;

    /**
     * protocol status.
     */
    private static final int POWER_RECOVERY = 0x0040;

    /**
     * protocol status.
     */
    private static final int DEVICE_CLOCK_SET_INCORRECT = 0x0020;

    /**
     * protocol status.
     */
    private static final int DEVICE_RESET = 0x0010;

    /**
     * protocol status.
     */
    private static final int DISTURBED_MEASURE = 0x0004;

    /**
     * protocol status.
     */
    private static final int RUNNING_RESERVE_EXHAUSTED = 0x0002;

    /**
     * protocol status.
     */
    private static final int FATAL_DEVICE_ERROR = 0x0001;
    private final PropertySpecService propertySpecService;
    private final DeviceMessageFileFinder deviceMessageFileFinder;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    /**
     * The COSEM object factory.
     */
    private final CosemObjectFactory cosemObjectFactory = new CosemObjectFactory(this);
    /**
     * The stored values implementation for the Z3.
     */
    private final StoredValuesImpl storedValuesImpl = new StoredValuesImpl(this.cosemObjectFactory);
    /**
     * The OBIS code mapper.
     */
    private final ObisCodeMapper ocm = new ObisCodeMapper(this.cosemObjectFactory);
    /**
     * The meter configuration used here is the one from the AbstractNTAProtocol meter.
     */
    private final DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
    /**
     * Contains the node address.
     */
    private String nodeAddress;
    /**
     * The device ID. This is used in the requestSAP() operation.
     */
    private String deviceId;
    /**
     * The password to be used for the device.
     */
    private String password;
    /**
     * The serial number to be used for the device.
     */
    private String serialNumber;
    /**
     * The MBus serial numbers are cached when they are requested.
     */
    private String[] mbusSerialNumbers;
    /**
     * The device serial number.
     */
    private String deviceSerialNumber;
    /**
     * Indicates the HDLC time out in milliseconds.
     */
    private int hdlcTimeout;
    /**
     * Indicates the number of retries the protocol must do before giving up.
     */
    private int protocolRetries;
    /**
     * The security level used.
     */
    private AuthenticationLevel authenticationLevel;
    /**
     * the used encryptionLevel
     */
    private EncryptionLevel encryptionLevel;
    /**
     * Indicates whether we should request the time zone from the meter.
     */
    private boolean requestTimeZone;
    /**
     * The roundtrip correction if this would be applicable.
     */
    private int roundtripCorrection;
    /**
     * The client MAC address.
     */
    private int clientMacAddress;
    /**
     * The server upper MAC address.
     */
    private int serverUpperMacAddress;
    /**
     * The server lower MAC address.
     */
    private int serverLowerMacAddress;
    /**
     * The load profile OBIS code.
     */
    private ObisCode loadProfileObisCode;
    /**
     * The DLMS connection that is used. Gets initialized in the {@link #init(InputStream, OutputStream, TimeZone, Logger)} method.
     */
    private DLMSConnection dlmsConnection;
    /**
     * The number of channels in the meter.
     */
    private int numberOfChannels = -1;
    /**
     * Indicates whether the configuration of the meter has changed. Non primitive because it can be null as well.
     */
    private int numberOfConfigurationChanges = -1;
    /**
     * Profile interval, initialized to -1 to indicate that it has not been set yet.
     */
    private int profileInterval = -1;
    /**
     * The captured objects helper.
     */
    private CapturedObjectsHelper capturedObjectsHelper;
    /**
     * Logger instance is not final as it can be set. We do default to the java.util.logging naming convention.
     */
    private Logger logger = Logger.getLogger(EictZ3.class.getName());
    /**
     * The device time zone.
     */
    private TimeZone timeZone;
    /**
     * DLMS cache, used to cache the UOL.
     */
    private DLMSCache dlmsCache = new DLMSCache();

    /**
     * The {@link ClientAddressingMode} used.
     */
    private ClientAddressingMode addressingMode;

    /**
     * The {@link DLMSConnectionMode} used.
     */
    private DLMSConnectionMode connectionMode;

    /**
     * The information field size.
     */
    private int informationFieldSize;

    /**
     * The maximum APDU size.
     */
    private int maximumAPDUSize = -1;

    /**
     * Number of milliseconds to force a delay.
     */
    private int forceDelay = 0;

    /**
     * The roundtrip treshold we allow when setting the clock. This is in milliseconds.
     */
    private int clockSetRoundtripTreshold;

    /**
     * The number of tries allowed when trying to set the clock and the roundtrip takes too long according to {@link #clockSetRoundtripTreshold}.
     */
    private int numberOfClocksetTries;

    /**
     * The maximum number of MBus devices.
     */
    private int maximumNumberOfMBusDevices;

    /**
     * The firmware version.
     */
    private String firmwareVersion;

    /**
     * This one indicates if we have set the properties yet. It is used to make sure {@link HasDynamicProperties#setUPLProperties(TypedProperties)} is called before {@link #init(InputStream, OutputStream, TimeZone, Logger)},
     * as one would assume the sequence to be the other way around.
     */
    private transient boolean propertiesSet = false;

    /**
     * Indication whether global or dedicated ciphering is used
     */
    private int cipheringType;

    /**
     * The used ApplicationServiceObject for proper handling the DLMS connection
     */
    private ApplicationServiceObject aso;

    public EictZ3(PropertySpecService propertySpecService, DeviceMessageFileFinder deviceMessageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        this.propertySpecService = propertySpecService;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    public final DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    @Override
    public final void init(final InputStream inputStream, final OutputStream outputStream, final TimeZone timeZone, final Logger logger) throws IOException {
        if (!propertiesSet) {
            throw new IllegalStateException("You have to call setProperties before calling init, otherwise this protocol will not work correctly.");
        }

        this.timeZone = timeZone;

        if (logger != null) {
            this.logger = logger;
        }

        ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
        XdlmsAse xDlmsAse = new XdlmsAse(null, true, -1, 6, cb, 1200);
        //TODO the dataTransport encryptionType should be a property (although currently only 0 is described by DLMS)
        SecurityContext sc = new SecurityContext(this.encryptionLevel.getEncryptionValue(), this.authenticationLevel.getAuthenticationValue(), 0, "EIT12345".getBytes(), getSecurityProvider(), this.cipheringType);

        this.aso = buildApplicationServiceObject(xDlmsAse, sc);

        try {
            logger.info("Initializing DLMS connection...");

            if (this.connectionMode == DLMSConnectionMode.HDLC) {
                logger.info("Using DLMS/HDLC, addressing mode of [" + addressingMode.getNumberOfBytes() + "] bytes.");

                this.dlmsConnection = new HDLC2Connection(inputStream, outputStream, this.hdlcTimeout, this.forceDelay, this.protocolRetries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode.getNumberOfBytes(), this.informationFieldSize, 5);
            } else {
                logger.info("Using DLMS/IP...");

                this.dlmsConnection = new TCPIPConnection(inputStream, outputStream, this.hdlcTimeout, this.forceDelay, this.protocolRetries, this.clientMacAddress, this.serverLowerMacAddress, getLogger());
            }

            this.dlmsConnection.setIskraWrapper(1);
        } catch (final DLMSConnectionException e) {
            // JDK 5 and predecessors apparently cannot init an IOException using String, Exception, so let's do this verbosely then...
            throw new IOException("Got a DLMS connection error when initializing the connection, error message was [" + e.getMessage() + "]", e);
        }
    }

    /**
     * Construct the desired {@link com.energyict.dlms.aso.ApplicationServiceObject}
     *
     * @param xDlmsAse the {@link com.energyict.dlms.aso.XdlmsAse} to use
     * @param sc       the {@link com.energyict.dlms.aso.SecurityContext} to use
     * @return the newly create ApplicationServiceObject
     */
    protected ApplicationServiceObject buildApplicationServiceObject(XdlmsAse xDlmsAse, SecurityContext sc) {
        return new ApplicationServiceObject(xDlmsAse, this, sc,
                (this.encryptionLevel.getEncryptionValue() == 0) ? AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING :
                        AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING);
    }

    /**
     * @return the current securityProvider (currently only LocalSecurityProvider is available)
     */
    public SecurityProvider getSecurityProvider() {
        Properties props = new Properties();
        props.put(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, this.authenticationLevel.getAuthenticationValue());
        props.put(LocalSecurityProvider.DATATRANSPORTKEY, this.encryptionLevel.getEncryptionValue());
        props.put(PASSWORD.getName(), password);
        return new LocalSecurityProvider(props);
    }

    /**
     * Returns the {@link CapturedObjectsHelper}.
     *
     * @return The {@link CapturedObjectsHelper}.
     * @throws IOException If something goes wrong.
     */
    private CapturedObjectsHelper getCapturedObjectsHelper() throws IOException {
        if (this.capturedObjectsHelper == null) {
            logger.info("Initializing the CapturedObjectsHelper using the generic profile, profile OBIS code is [" + this.getLoadprofileObisCode().toString() + "]");

            final ProfileGeneric profileGeneric = getCosemObjectFactory().getProfileGeneric(this.getLoadprofileObisCode());
            this.capturedObjectsHelper = profileGeneric.getCaptureObjectsHelper();

            logger.info("Done, load profile [" + this.getLoadprofileObisCode() + "] has [" + this.capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects...");
        }

        return this.capturedObjectsHelper;
    }

    @Override
    public final int getNumberOfChannels() throws IOException {
        if (this.numberOfChannels == -1) {
            logger.info("Loading the number of channels, looping over the captured objects...");

            this.numberOfChannels = this.getCapturedObjectsHelper().getNrOfchannels();

            logger.info("Got [" + this.numberOfChannels + "] channels in load profile (out of [" + this.capturedObjectsHelper.getNrOfCapturedObjects() + "] captured objects)");
        }

        return this.numberOfChannels;
    }

    @Override
    public final int getProfileInterval() throws IOException {
        if (this.profileInterval == -1) {
            logger.info("Requesting the profile interval from the meter...");

            final ProfileGeneric profileGeneric = this.getCosemObjectFactory().getProfileGeneric(this.getLoadprofileObisCode());
            this.profileInterval = profileGeneric.getCapturePeriod();

            logger.info("Profile interval is [" + this.profileInterval + "]");
        }

        return this.profileInterval;
    }

    /**
     * Returns the load profile obis code.
     *
     * @return The load profile obis code.
     * @throws IOException If an IO error occurs during the load profile determination.
     */
    private ObisCode getLoadprofileObisCode() throws IOException {
        if (this.loadProfileObisCode == null) {
            logger.info("No specific obis code has been specified, trying to determine it...");

            final int mbusPhysicalAddress = this.getMBusPhysicalAddress();

            if (mbusPhysicalAddress != -1) {
                logger.info("Determined MBus physical address : [" + mbusPhysicalAddress + "], determining obis code to use for the load profile...");

                final int obisCodeId = mbusPhysicalAddress + 1;
                this.loadProfileObisCode = ObisCode.fromString(MBUS_LOAD_PROFILE_FORMAT.format(new Object[]{obisCodeId}));

                logger.info("Using Obis code [" + this.loadProfileObisCode + "] for load profile...");
            } else {
                logger.info("Not an MBus meter, using EpIO default load profile OBIS code [" + OBIS_CODE_EPIO_LOAD_PROFILE + "]");

                this.loadProfileObisCode = OBIS_CODE_EPIO_LOAD_PROFILE;
            }
        } else {
            logger.info("Load profile OBIS code : [" + this.loadProfileObisCode + "]");
        }

        return this.loadProfileObisCode;
    }

    @Override
    public final ProfileData getProfileData(final boolean includeEvents) throws IOException {
        // As the meter has been invented in 2009, the from date is set fixed to 1st of January of 2009, which seems like a sensible default.
        final Calendar fromCalendar = ProtocolUtils.getCleanCalendar(this.timeZone);

        fromCalendar.set(Calendar.YEAR, 2009);
        fromCalendar.set(Calendar.MONTH, 0);
        fromCalendar.set(Calendar.DATE, 1);

        return this.getProfileData(fromCalendar, ProtocolUtils.getCalendar(this.timeZone), includeEvents);
    }

    @Override
    public final ProfileData getProfileData(final Date lastReading, final boolean includeEvents) throws IOException {
        final Calendar fromCalendar = ProtocolUtils.getCleanCalendar(this.timeZone);

        fromCalendar.setTime(lastReading);

        return this.getProfileData(fromCalendar, ProtocolUtils.getCalendar(this.timeZone), includeEvents);
    }

    @Override
    public final ProfileData getProfileData(final Date fromDate, final Date toDate, final boolean includeEvents) throws IOException {
        final Calendar from = ProtocolUtils.getCleanCalendar(this.timeZone);
        from.setTime(fromDate);

        final Calendar to = ProtocolUtils.getCleanCalendar(this.timeZone);
        to.setTime(toDate);

        return this.getProfileData(from, to, includeEvents);
    }

    /**
     * Gets the profile data starting from the date indicated by fromCalendar and ending at toCalendar.
     *
     * @param from          The starting point.
     * @param to            The ending point (this is allowed to be null).
     * @param includeEvents Indicates whether the logbook should be included or not.
     * @return The profile data for the given period.
     * @throws IOException if an error occurs during the device communication.
     */
    private ProfileData getProfileData(final Calendar from, final Calendar to, final boolean includeEvents) throws IOException {
        logger.info("Loading profile data starting at [" + from + "], ending at [" + to + "], " + (includeEvents ? "" : "not") + " including events");

        final ProfileData profileData = new ProfileData();

        final ProfileGeneric profileGeneric = this.getCosemObjectFactory().getProfileGeneric(this.getLoadprofileObisCode());
        final DataContainer datacontainer = profileGeneric.getBuffer(from, to);

        logger.info("Building channel information...");

        for (int i = 0; i < this.getNumberOfChannels(); i++) {
            final ScalerUnit scalerUnit = this.getRegisterScalerUnit(i);

            logger.info("Scaler unit for channel [" + i + "] is [" + scalerUnit + "]");

            final ChannelInfo channelInfo = new ChannelInfo(i, "EICTZ3_CH_" + i, scalerUnit.getEisUnit());

            final CapturedObject channelCapturedObject = getCapturedObjectsHelper().getProfileDataChannelCapturedObject(i);

            if (ParseUtils.isObisCodeCumulative(channelCapturedObject.getLogicalName().getObisCode())) {
                logger.info("Indicating that channel [" + i + "] is cumulative...");

                channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
            }

            profileData.addChannel(channelInfo);
        }

        logger.info("Building profile data...");

        final Object[] loadProfileEntries = datacontainer.getRoot().element;

        if (loadProfileEntries.length == 0) {
            logger.log(Level.INFO, "There are no entries in the load profile, nothing to build...");
        } else {
            logger.log(Level.INFO, "Got [" + datacontainer.getRoot().element.length + "] entries in the load profile, building profile data...");

            for (int i = 0; i < loadProfileEntries.length; i++) {
                logger.info("Processing interval [" + i + "]");

                final DataStructure intervalData = datacontainer.getRoot().getStructure(i);

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Mapping interval end time...");
                }

                Calendar calendar = ProtocolUtils.initCalendar(false, this.timeZone);
                calendar = this.mapIntervalEndTimeToCalendar(calendar, intervalData, (byte) 0);

                final int eiStatus = this.map2IntervalStateBits(intervalData.getInteger(1));
                final int protocolStatus = intervalData.getInteger(1);

                final IntervalData data = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()), eiStatus, protocolStatus);

                logger.info("Adding channel data.");

                for (int j = 0; j < getCapturedObjectsHelper().getNrOfCapturedObjects(); j++) {
                    if (getCapturedObjectsHelper().isChannelData(j)) {
                        data.addValue(new Integer(intervalData.getInteger(j)));
                    }
                }

                profileData.addInterval(data);
            }
        }

        if (includeEvents) {
            logger.info("Requested to include meter events, loading...");

            profileData.setMeterEvents(this.getMeterEvents(from, to));
        }

        return profileData;
    }

    /**
     * Loads the meter events from the meter and returns them.
     *
     * @param from The start date.
     * @param to   The end date.
     * @return The meter events for the given period.
     * @throws IOException If an IO error occurs during the communication.
     */
    private List<MeterEvent> getMeterEvents(final Calendar from, final Calendar to) throws IOException {
        logger.info("Fetching meter events from [" + (from != null ? from.getTime() : "Not specified") + "] to [" + (to != null ? to.getTime() : "Not specified") + "]");

        final List<MeterEvent> events = new ArrayList<>();

        final DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getEventLogObject().getObisCode()).getBuffer(from, to);
        final DataContainer dcControlLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getControlLogObject().getObisCode()).getBuffer(from, to);
        final DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getPowerFailureLogObject().getObisCode()).getBuffer(from, to);
        final DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getFraudDetectionLogObject().getObisCode()).getBuffer(from, to);
        final DataContainer dcMbusEventLog = getCosemObjectFactory().getProfileGeneric(getMeterConfig().getMbusEventLogObject().getObisCode()).getBuffer(from, to);

        final EventsLog standardEvents = new EventsLog(getTimeZone(), dcEvent);
        final FraudDetectionLog fraudDetectionEvents = new FraudDetectionLog(getTimeZone(), dcFraudDetection);
        final DisconnectControlLog disconnectControl = new DisconnectControlLog(getTimeZone(), dcControlLog);
        final MbusLog mbusLogs = new MbusLog(getTimeZone(), dcMbusEventLog);
        final PowerFailureLog powerFailure = new PowerFailureLog(getTimeZone(), dcPowerFailure);

        events.addAll(standardEvents.getMeterEvents());
        events.addAll(fraudDetectionEvents.getMeterEvents());
        events.addAll(disconnectControl.getMeterEvents());
        events.addAll(mbusLogs.getMeterEvents());
        events.addAll(powerFailure.getMeterEvents());

        return events;
    }

    private Calendar mapIntervalEndTimeToCalendar(final Calendar cal, final DataStructure intervalData, final byte btype) throws IOException {
        final Calendar calendar = (Calendar) cal.clone();

        if (intervalData.getOctetString(0).getArray()[0] != -1) {
            calendar.set(Calendar.YEAR, ((intervalData.getOctetString(0).getArray()[0] & 0xff) << 8) | ((intervalData.getOctetString(0).getArray()[1] & 0xff)));
        }

        if (intervalData.getOctetString(0).getArray()[2] != -1) {
            calendar.set(Calendar.MONTH, (intervalData.getOctetString(0).getArray()[2] & 0xff) - 1);
        }

        if (intervalData.getOctetString(0).getArray()[3] != -1) {
            calendar.set(Calendar.DAY_OF_MONTH, (intervalData.getOctetString(0).getArray()[3] & 0xff));
        }

        if (intervalData.getOctetString(0).getArray()[5] != -1) {
            calendar.set(Calendar.HOUR_OF_DAY, (intervalData.getOctetString(0).getArray()[5] & 0xff));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
        }

        if (btype == 0) {
            if (intervalData.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, ((intervalData.getOctetString(0).getArray()[6] & 0xff) / (getProfileInterval() / 60)) * (getProfileInterval() / 60));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            calendar.set(Calendar.SECOND, 0);
        } else {
            if (intervalData.getOctetString(0).getArray()[6] != -1) {
                calendar.set(Calendar.MINUTE, (intervalData.getOctetString(0).getArray()[6] & 0xff));
            } else {
                calendar.set(Calendar.MINUTE, 0);
            }

            if (intervalData.getOctetString(0).getArray()[7] != -1) {
                calendar.set(Calendar.SECOND, (intervalData.getOctetString(0).getArray()[7] & 0xff));
            } else {
                calendar.set(Calendar.SECOND, 0);
            }
        }

        // if DST, add 1 hour
        if (intervalData.getOctetString(0).getArray()[11] != -1) {
            if ((intervalData.getOctetString(0).getArray()[11] & (byte) 0x80) == 0x80) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
        }

        return calendar;
    }

    /**
     * Maps the given protocol status to interval state bits (eistatus). (bitset).
     *
     * @param protocolStatus The protocol status to map.
     * @return The mapped eistatus.
     */
    private int map2IntervalStateBits(final int protocolStatus) {
        int eiStatus = 0;

        if ((protocolStatus & CLEAR_LOADPROFILE) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & CLEAR_LOGBOOK) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & END_OF_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & BEGIN_OF_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & VARIABLE_SET) != 0) {
            eiStatus |= IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatus & DEVICE_CLOCK_SET_INCORRECT) != 0) {
            eiStatus |= IntervalStateBits.SHORTLONG;
        }

        // Commented out as we don't want SL flags the whole summer long.
        // NTA requires this apparently although the device uses UTC.

        /*
           * if ((protocolStatus & SEASONAL_SWITCHOVER) != 0) eiStatus |= IntervalStateBits.SHORTLONG;
           */

        if ((protocolStatus & FATAL_DEVICE_ERROR) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & DISTURBED_MEASURE) != 0) {
            eiStatus |= IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & POWER_FAILURE) != 0) {
            eiStatus |= IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & POWER_RECOVERY) != 0) {
            eiStatus |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & DEVICE_RESET) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & RUNNING_RESERVE_EXHAUSTED) != 0) {
            eiStatus |= IntervalStateBits.OTHER;
        }

        return eiStatus;
    }

    @Override
    public final Quantity getMeterReading(final String name) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public final Quantity getMeterReading(final int channelId) throws IOException {
        throw new UnsupportedException();
    }

    /**
     * Gets the scaler unit for the given channel.
     *
     * @param channelId The ID of the channel for which to fetch the scaler.
     * @return The scaler.
     * @throws IOException If an IO error occurs while communicating with the meter.
     */
    private ScalerUnit getRegisterScalerUnit(final int channelId) throws IOException {
        ScalerUnit unit;

        if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.REGISTER.getClassId()) {
            unit = this.getCosemObjectFactory().getRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        } else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            unit = this.getCosemObjectFactory().getDemandRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        } else if (getCapturedObjectsHelper().getProfileDataChannelCapturedObject(channelId).getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            unit = this.getCosemObjectFactory().getExtendedRegister(getCapturedObjectsHelper().getProfileDataChannelObisCode(channelId)).getScalerUnit();
        } else {
            throw new IllegalArgumentException("EictZ3, getRegisterScalerUnit(), invalid channelId, " + channelId);
        }

        if (unit.getUnitCode() == 0) {
            logger.info("Channel [" + channelId + "] has a scaler unit with unit code [0], using a unitless scalerunit.");

            unit = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
        }

        return unit;
    }

    public final void setTime() throws IOException {
        logger.info("Setting the time of the remote device, first requesting the device's time.");

        final Clock clock = this.getCosemObjectFactory().getClock();

        boolean timeAdjusted = false;
        int currentTry = 1;

        while (!timeAdjusted && (currentTry <= this.numberOfClocksetTries)) {
            logger.info("Requesting clock for adjustment");

            final long startTime = System.currentTimeMillis();

            final Date deviceTime = clock.getDateTime();

            final long endTime = System.currentTimeMillis();

            if (endTime - startTime <= this.clockSetRoundtripTreshold) {
                final long roundtripCorrection = (endTime - startTime) / 2;

                // The time that arrives here, has to be corrected with the
                // roundtripcorrection, as this would be the time on the device
                // at this time.
                final long timeDifference = System.currentTimeMillis() - (deviceTime.getTime() + roundtripCorrection);

                logger.info("Time difference is [" + timeDifference + "] miliseconds (using roundtrip time of [" + roundtripCorrection + "] milliseconds)");

                // Now if the time difference can be corrected using a shift of
                // the time, correct it, otherwise do a setClock.
                if (Math.abs(timeDifference / 1000) <= Clock.MAX_TIME_SHIFT_SECONDS) {
                    logger.info("Time difference can be corrected using a time shift, invoking.");

                    clock.shiftTime((int) (timeDifference / 1000));
                } else {
                    logger.info("Time difference is too big to be corrected using a time shift, setting absolute date and time.");

                    final Date date = new Date(System.currentTimeMillis() + roundtripCorrection);

                    final Calendar newTimeToSet = Calendar.getInstance();
                    newTimeToSet.setTime(date);

                    this.setDeviceTime(newTimeToSet);
                }

                timeAdjusted = true;
            } else {
                logger.info("Roundtrip to the device took [" + (endTime - startTime) + "] milliseconds, which exceeds the treshold of [" + this.clockSetRoundtripTreshold + "] milliseconds, retrying");
            }

            currentTry++;
        }

        // Exceeded tries without an adjust, this means the roundtrip time was
        // not acceptable.
        if (!timeAdjusted) {
            logger.log(Level.WARNING, "Cannot set time, did not have a roundtrip that took shorter than [" + this.clockSetRoundtripTreshold + "] milliseconds. Not setting clock.");
        }
    }

    /**
     * Sets the device time using the DLMS set on the date_time of the clock object. This is absolute setting of time, which is used in case of a force clock, or in case the time exceeds the limits of the shift_time method (which is a quarter of an hour either back or forward shift).
     *
     * @param newTime The new time to set.
     */
    private void setDeviceTime(final Calendar newTime) throws IOException {
        // Calendar calendar =
        // Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        newTime.add(Calendar.MILLISECOND, roundtripCorrection);
        final byte[] byteTimeBuffer = new byte[14];

        byteTimeBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (newTime.get(Calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) newTime.get(Calendar.YEAR);
        byteTimeBuffer[4] = (byte) (newTime.get(Calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) newTime.get(Calendar.DAY_OF_MONTH);
        byte bDOW = (byte) newTime.get(Calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) newTime.get(Calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) newTime.get(Calendar.MINUTE);
        byteTimeBuffer[9] = (byte) newTime.get(Calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0xFF;
        byteTimeBuffer[11] = (byte) 0x80; // 0x80;
        byteTimeBuffer[12] = (byte) 0x00; // 0x00;
        if (timeZone.inDaylightTime(newTime.getTime())) {
            byteTimeBuffer[13] = (byte) 0x80; // 0x00;
        } else {
            byteTimeBuffer[13] = (byte) 0x00; // 0x00;
        }

        getCosemObjectFactory().writeObject(ObisCode.fromString("0.0.1.0.0.255"), 8, 2, byteTimeBuffer);
    }

    @Override
    public Date getTime() throws IOException {
        final Clock clock = getCosemObjectFactory().getClock();
        return clock.getDateTime();
    }

    /**
     * Checks if the device configuration has changed.
     *
     * @return True if the device configuration has changed.
     * @throws IOException If an error occurs during the device communication.
     */
    private int requestConfigurationProgramChanges() throws IOException {
        if (this.numberOfConfigurationChanges == -1) {
            logger.info("Asking meter if the configuration has changed since the last check...");

            this.numberOfConfigurationChanges = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }

        return this.numberOfConfigurationChanges;
    }

    @Override
    public ApplicationServiceObject getAso() {
        return null;      //Not used
    }

    @Override
    public final void connect() throws IOException {
        logger.info("Connecting to EpIO / Z3, connecting MAC...");

        try {
            this.getDLMSConnection().connectMAC();
        } catch (final DLMSConnectionException e) {
            logger.log(Level.SEVERE, "Cannot connect MAC over DLMS connection [mode : " + this.connectionMode + "] due to a DLMS connection error [" + e.getMessage() + "]", e);
            // JDK 5 and predecessors apparently cannot init an IOException using String, Exception, so let's do this verbosely then...
            final IOException ioException = new IOException("DLMS connection error when connecting MAC, message was [" + e.getMessage() + "]", e);
            throw ioException;
        }

        logger.info("Done, connected MAC, now associating...");

        // The AARQ constructor does the work, so it is in fact useful,
        // although unused...
        @SuppressWarnings("unused")
        AARQ aarq = null;

        if (this.maximumAPDUSize == -1) {
            aarq = new AARQ(this.authenticationLevel.getAuthenticationValue(), this.password, getDLMSConnection());
        } else {
            aarq = new AARQ(this.authenticationLevel.getAuthenticationValue(), this.password, this.getDLMSConnection(), this.maximumAPDUSize);
        }

        logger.info("Done, associated. Checking if we need to update our cache...");

        boolean needToLoadObjectList = false;

        if ((this.dlmsCache != null) && (this.dlmsCache.getObjectList() != null) && (this.dlmsCache.getObjectList().length > 0)) {
            logger.info("We have a DLMS cache for this device, checking if it is still valid.");

            // Set the object list in the meter config.
            this.meterConfig.setInstantiatedObjectList(this.dlmsCache.getObjectList());

            final int deviceConfigurationChanges = this.requestConfigurationProgramChanges();

            logger.info("Device configuration changes : [" + deviceConfigurationChanges + "]");

            if (this.dlmsCache.getConfProgChange() == deviceConfigurationChanges) {
                logger.info("It is still valid, using it...");
            } else {
                logger.info("It is not valid anymore, configuration changes from the device [" + deviceConfigurationChanges + "] does not match the one in the cache [" + this.dlmsCache.getConfProgChange() + "]");

                needToLoadObjectList = true;
            }
        } else {
            logger.info("No cache for this device.");
            needToLoadObjectList = true;
        }

        if (needToLoadObjectList) {
            logger.info("Requesting meter object list...");

            this.requestObjectList();

            logger.info("Updating cache...");

            this.dlmsCache.saveObjectList(this.meterConfig.getInstantiatedObjectList());

            final int deviceConfigurationChanges = this.requestConfigurationProgramChanges();

            logger.info("Device configuration changes : [" + deviceConfigurationChanges + "]");

            this.dlmsCache.setConfProgChange(deviceConfigurationChanges);
        }

        logger.info("Verifying device serial number...");

        if (!this.verifyMeterSerialNumber()) {
            throw new IllegalStateException("Serial number reported by meter [" + this.getDeviceSerialNumber() + "] does not match the one configured in EIServer [" + this.serialNumber + "], please correct.");
        } else {
            logger.info("Serial number is valid.");
        }
    }

    /**
     * Verifies the EIServer serial number against the device serial number.
     *
     * @throws IOException If an IO error occurs during the device communication.
     */
    private boolean verifyMeterSerialNumber() throws IOException {
        if ((this.serialNumber == null) || this.serialNumber.trim().equals("")) {
            logger.info("There was no serial number configured in EIServer, assuming the configuration is valid...");

            return true;
        }

        boolean matches = this.getDeviceSerialNumber().toLowerCase().trim().equals(this.serialNumber.toLowerCase().trim());

        if (!matches) {
            logger.info("The serial number does not match the one of the EpIO itself, checking if it is an MBus device proxied by the EpIO...");

            if (this.mbusSerialNumbers == null) {
                logger.info("Requesting MBus serial numbers from the device...");

                this.mbusSerialNumbers = this.requestMBusSerialNumbers();

                logger.info("Received [" + this.mbusSerialNumbers.length + "] serial numbers for MBus devices proxied by the EpIO, validating...");

                for (final String serialNumber : this.mbusSerialNumbers) {
                    if ((serialNumber != null) && serialNumber.toLowerCase().trim().equals(this.serialNumber)) {
                        matches = true;
                        break;
                    }
                }
            }
        }

        return matches;
    }

    /**
     * Returns the node address as it is filled in in EIServer.
     *
     * @return The node address as it is filled in in EIServer.
     */
    private int getNodeAddress() {
        if ((this.nodeAddress == null) || this.nodeAddress.trim().equals("")) {
            return -1;
        } else {
            try {
                return Integer.valueOf(this.nodeAddress);
            } catch (final NumberFormatException e) {
                this.logger.log(Level.WARNING, "A node address was configured for device with serial ID [" + this.deviceId + "], but could not parse it as it was probably not numeric ! (error was [" + e.getMessage() + "]", e);

                return -1;
            }
        }
    }

    /**
     * This method uses two behaviours, depending on the fact whether you have filled in a Node Address or not. If the node address > 0, then node address - 1 is returned. If the node address is 0, we try to find the MBus meter based on the serial number. If the node address is 0, this means no MBus meter is meant. The method returns -1 if no MBus device with the given serial number can be found.
     *
     * @return The MBus meter index.
     * @throws IOException If an IO error occurs during the fetch of the MBus serial numbers.
     */
    private int getMBusPhysicalAddress() throws IOException {
        if (this.getNodeAddress() == 0) {
            throw new IllegalStateException("Cannot determine MBus index as the node address is explicitly set to 0, meaning that the destination is the EpIO/Z3, and not an MBus meter.");
        } else if (this.getNodeAddress() > 0) {
            this.logger.info("Node address has been set in EIServer to be [" + this.nodeAddress + "], using this hardcoded value minus one and not using meter configuration");
            return this.getNodeAddress() - 1;
        } else {
            this.logger.info("Node address was not set in EIServer, using serial number to probe for MBus device index...");

            if (this.mbusSerialNumbers == null) {
                logger.info("Do not have MBus serial numbers yet, requesting from meter...");

                this.mbusSerialNumbers = this.requestMBusSerialNumbers();
            }

            for (int i = 0; i < this.maximumNumberOfMBusDevices; i++) {
                if ((mbusSerialNumbers[i] != null) && mbusSerialNumbers[i].trim().equals(this.serialNumber)) {
                    return i;
                }
            }

            return -1;
        }
    }

    /**
     * Requests the serial numbers of the remote device.
     *
     * @return The requested serial numbers.
     * @throws IOException If an IO error should occur when requesting the MBus serial numbers from the meter.
     */
    private String[] requestMBusSerialNumbers() throws IOException {
        final String[] serialNumbers = new String[this.maximumNumberOfMBusDevices];

        for (int i = 0; i < this.maximumNumberOfMBusDevices; i++) {
            final String mbusSerialNumber = getMBusDeviceSerialNumberFromMeter(i);
            serialNumbers[i] = mbusSerialNumber;
        }

        return serialNumbers;
    }

    /**
     * Gets the serial number of the MBus device on the given index.
     *
     * @param mbusPhysicalAddress The index of the sought after MBus device.
     * @return The serial number of the device, <code>null</code> if there is no such device.
     * @throws IOException If an IO error occurs during the query for the MBus serial number.
     */
    private String getMBusDeviceSerialNumberFromMeter(final int mbusPhysicalAddress) throws IOException {
        logger.info("Requesting serial number for MBus device, physical address [" + mbusPhysicalAddress + "]");

        final UniversalObject serialNumberObject = this.meterConfig.getMbusSerialNumber(mbusPhysicalAddress);

        try {
            final String serialNumber = this.cosemObjectFactory.getGenericRead(serialNumberObject).getString();

            logger.info("Device says serial number is [" + serialNumber + "] for MBus device on physical address [" + mbusPhysicalAddress + "]");

            return serialNumber;
        } catch (final DataAccessResultException e) {
            if (e.getCode() == DataAccessResultCode.OBJECT_UNDEFINED) {
                logger.info("No MBus device on physical address [" + mbusPhysicalAddress + "]");

                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public final void disconnect() throws IOException {
        try {
            if (this.dlmsConnection != null) {
                getDLMSConnection().disconnectMAC();
            } else {
                logger.log(Level.WARNING, "Cannot disconnect because the DLMS connection is null !");
            }
        } catch (final DLMSConnectionException e) {
            logger.log(Level.SEVERE, "DLMS connection error when disconnecting from device : [" + e.getMessage() + "]", e);

            // JDK 5 and predecessors apparently cannot init an IOException using String, Exception, so let's do this verbosely then...
            final IOException ioException = new IOException("DLMS connection error when disconnecting from device : [" + e.getMessage() + "]", e);
            throw ioException;
        }
    }

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references. This method must be executed before other request methods.
     *
     * @throws IOException
     */
    private void requestObjectList() throws IOException {
        logger.info("Requesting object list from device and updating meter config...");

        this.meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    }

    private String requestAttribute(final short sIC, final byte[] LN, final byte bAttr) throws IOException {
        return this.doRequestAttribute(sIC, LN, bAttr).print2strDataContainer();
    }

    private DataContainer doRequestAttribute(final int classId, final byte[] ln, final int lnAttr) throws IOException {
        final DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln), DLMSUtils.attrLN2SN(lnAttr), classId).getDataContainer();
        return dc;
    }

    @Override
    public final String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:25 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public final String getFirmwareVersion() throws IOException {
        if (this.firmwareVersion == null) {
            this.firmwareVersion = AXDRDecoder.decode(this.getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE).getRawValueAttr()).getOctetString().stringValue();
        }

        return this.firmwareVersion;
    }

    @Override
    public String getSerialNumber() {
        try {
            UniversalObject uo = getMeterConfig().getSerialNumberObject();
            return getCosemObjectFactory().getGenericRead(uo).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, protocolRetries + 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpecOfMaxLength(ADDRESS.getName(), 16),
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.stringSpec(PROPNAME_SECURITY_LEVEL),
                this.integerSpec(PROPNAME_REQUEST_TIME_ZONE),
                this.integerSpec(PROPNAME_CLIENT_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS),
                this.integerSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS),
                this.stringSpec(NODEID.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec(PROPNAME_ADDRESSING_MODE),
                this.integerSpec(PROPNAME_CONNECTION),
                new ObisCodePropertySpec(PROPNAME_LOAD_PROFILE_OBIS_CODE, false),
                this.integerSpec(PROPNAME_INFORMATION_FIELD_SIZE),
                this.integerSpec(PROPNAME_MAXIMUM_NUMBER_OF_MBUS_DEVICES),
                this.stringSpec(PROPNAME_MAX_APDU_SIZE),
                this.stringSpec(PROPNAME_FORCE_DELAY),
                this.stringSpec(PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD),
                this.stringSpec(PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES),
                this.integerSpec("CipheringType", CipheringType.GLOBAL.getType(), CipheringType.DEDICATED.getType()));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec stringSpecOfMaxLength(String name, int length) {
        return this.spec(name, () -> this.propertySpecService.stringSpecOfMaximumLength(length));
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, Integer... validValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, this.propertySpecService::integerSpec)
                .addValues(validValues)
                .markExhaustive()
                .finish();
    }

    @Override
    public final void setUPLProperties(final TypedProperties properties) throws InvalidPropertyException {
        try {
            this.deviceId = properties.getTypedProperty(ADDRESS.getName());
            this.password = properties.getTypedProperty(PASSWORD.getName());
            this.hdlcTimeout = Integer.parseInt(properties.getTypedProperty(PROPNAME_TIMEOUT, "10000").trim());
            this.protocolRetries = Integer.parseInt(properties.getTypedProperty(PROPNAME_RETRIES, "5").trim());

            /* the format of the securityLevel is changed, now authenticationSecurityLevel and dataTransportSecurityLevel are in one */
            String securityLevel = properties.getTypedProperty(PROPNAME_SECURITY_LEVEL, "1").trim();
            if (securityLevel.contains(":")) {
                this.authenticationLevel = AuthenticationLevel.getByPropertyValue(Integer.parseInt(securityLevel.substring(0, securityLevel.indexOf(":"))));
                this.encryptionLevel = EncryptionLevel.getByPropertyValue(Integer.parseInt(securityLevel.substring(securityLevel.indexOf(":") + 1)));
            } else {
                this.authenticationLevel = AuthenticationLevel.getByPropertyValue(Integer.parseInt(securityLevel));
                this.encryptionLevel = EncryptionLevel.getByPropertyValue(0);
            }

            this.requestTimeZone = Integer.parseInt(properties.getTypedProperty(PROPNAME_REQUEST_TIME_ZONE, "0").trim()) != 0;
            this.roundtripCorrection = Integer.parseInt(properties.getTypedProperty(PROPNAME_ROUNDTRIP_CORRECTION, "0").trim());
            this.clientMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_CLIENT_MAC_ADDRESS, "1").trim());
            this.serverUpperMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "17").trim());
            this.serverLowerMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "17").trim());
            this.nodeAddress = properties.getTypedProperty(NODEID.getName(), "");
            this.serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            this.addressingMode = ClientAddressingMode.getByPropertyValue(Integer.parseInt(properties.getTypedProperty(PROPNAME_ADDRESSING_MODE, "-1")));
            this.connectionMode = DLMSConnectionMode.getByPropertyValue(Integer.parseInt(properties.getTypedProperty(PROPNAME_CONNECTION, "0")));
            this.loadProfileObisCode = properties.hasValueFor(PROPNAME_LOAD_PROFILE_OBIS_CODE) ? ObisCode.fromString(properties.getTypedProperty(PROPNAME_LOAD_PROFILE_OBIS_CODE)) : null;
            this.informationFieldSize = Integer.parseInt(properties.getTypedProperty(PROPNAME_INFORMATION_FIELD_SIZE, "-1"));
            this.maximumNumberOfMBusDevices = Integer.parseInt(properties.getTypedProperty(PROPNAME_MAXIMUM_NUMBER_OF_MBUS_DEVICES, "4"));
            // the NTA meters normally use the global keys to encrypt
            this.cipheringType = Integer.parseInt(properties.getTypedProperty("CipheringType", Integer.toString(CipheringType.GLOBAL.getType())));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }

        try {
            this.maximumAPDUSize = Integer.parseInt(properties.getTypedProperty(PROPNAME_MAX_APDU_SIZE, "-1"));
        } catch (final NumberFormatException e) {
            this.maximumAPDUSize = -1;
        }

        try {
            this.forceDelay = Integer.parseInt(properties.getTypedProperty(PROPNAME_FORCE_DELAY, "0"));
        } catch (final NumberFormatException e) {
            logger.log(Level.WARNING, "Cannot interpret property [" + PROPNAME_FORCE_DELAY + "] because it is not numeric, defaulting to [" + this.forceDelay + "]");
        }

        try {
            this.clockSetRoundtripTreshold = Integer.parseInt(properties.getTypedProperty(PROPNAME_CLOCKSET_ROUNDTRIP_CORRECTION_THRESHOLD, String.valueOf(DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD)));
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, "Cannot parse the number of roundtrip correction probes to be done, setting to default value of [" + DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD + "]", e);
            this.clockSetRoundtripTreshold = DEFAULT_CLOCKSET_ROUNDTRIP_CORRECTION_TRESHOLD;
        }

        try {
            this.numberOfClocksetTries = Integer.parseInt(properties.getTypedProperty(PROPNAME_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES, String.valueOf(DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES)));
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, "Cannot parse the number of clockset tries to a numeric value, setting to default value of [" + DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES + "]", e);
            this.numberOfClocksetTries = DEFAULT_MAXIMUM_NUMBER_OF_CLOCKSET_TRIES;
        }

        this.propertiesSet = true;
    }

    @Override
    public String getRegister(final String name) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        final DLMSObis ln = new DLMSObis(name);
        if (ln.isLogicalName()) {
            if (classSpecified) {
                return requestAttribute(ln.getDLMSClass(), ln.getLN(), (byte) ln.getOffset());
            } else {
                final UniversalObject uo = getMeterConfig().getObject(ln);
                return getCosemObjectFactory().getGenericRead(uo).getDataContainer().print2strDataContainer();
            }
        } else if (name.contains("-")) { // you get a from/to
            final DLMSObis ln2 = new DLMSObis(name.substring(0, name.indexOf("-")));
            if (ln2.isLogicalName()) {
                final String from = name.substring(name.indexOf("-") + 1, name.indexOf("-", name.indexOf("-") + 1));
                final String to = name.substring(name.indexOf(from) + from.length() + 1);
                if (ln2.getDLMSClass() == 7) {
                    return getCosemObjectFactory().getProfileGeneric(getMeterConfig().getObject(ln2).getObisCode()).getBuffer(convertStringToCalendar(from), convertStringToCalendar(to)).print2strDataContainer();
                } else {
                    throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " is not a profile.");
                }
            } else {
                throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " does not exist.");
            }
        } else {
            throw new NoSuchRegisterException("GenericGetSet,getRegister, register " + name + " does not exist.");
        }
    }

    private Calendar convertStringToCalendar(final String strDate) {
        final Calendar cal = Calendar.getInstance(getTimeZone());
        cal.set(Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))) & 0xFFFF, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/"))) & 0xFF) - 1, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))) & 0xFF, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())) & 0xFF);
        return cal;
    }

    @Override
    public void setRegister(final String name, final String value) throws IOException {
        boolean classSpecified = false;
        if (name.indexOf(':') >= 0) {
            classSpecified = true;
        }
        final DLMSObis ln = new DLMSObis(name);
        if ((ln.isLogicalName()) && (classSpecified)) {
            getCosemObjectFactory().getGenericWrite(ObisCode.fromByteArray(ln.getLN()), ln.getOffset(), ln.getDLMSClass()).write(convert(value));
        } else {
            throw new NoSuchRegisterException("GenericGetSet, setRegister, register " + name + " does not exist.");
        }
    }

    /**
     * Converts the given string.
     *
     * @param s The string.
     * @return
     */
    private byte[] convert(final String s) {
        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("String length is not a modulo 2 hex representation!");
        } else {
            final byte[] data = new byte[s.length() / 2];

            for (int i = 0; i < (s.length() / 2); i++) {
                data[i] = (byte) Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16);
            }

            return data;
        }
    }

    @Override
    public void initializeDevice() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public Serializable getCache() {
        return this.dlmsCache;
    }

    @Override
    public final void setCache(Serializable cacheObject) {
        if (!(cacheObject instanceof DLMSCache)) {
            throw new IllegalArgumentException("This protocol expects a cache object of type [" + DLMSCache.class.getName() + "], you provided an object of type [" + cacheObject + "], which is not compatible with this implementation !");
        }
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public final String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.password + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_DLMSEICTZ3.cache";
    }

    @Override
    public final void release() {
        // Not implemented for this protocol.
    }

    @Override
    public final void enableHHUSignOn(final SerialCommunicationChannel commChannel) {
        this.enableHHUSignOn(commChannel, false);
    }

    @Override
    public final void enableHHUSignOn(final SerialCommunicationChannel commChannel, final boolean datareadout) {
        final HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, this.hdlcTimeout, this.protocolRetries, 300, 0);

        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);

        this.getDLMSConnection().setHHUSignOn(hhuSignOn, this.nodeAddress);
    }

    @Override
    public final byte[] getHHUDataReadout() {
        return this.getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    @Override
    public final Logger getLogger() {
        return this.logger;
    }

    @Override
    public final DLMSMeterConfig getMeterConfig() {
        return this.meterConfig;
    }

    @Override
    public final int getReference() {
        return LN_REFERENCE;
    }

    @Override
    public final int getRoundTripCorrection() {
        return this.roundtripCorrection;
    }

    @Override
    public final TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public final boolean isRequestTimeZone() {
        return this.requestTimeZone;
    }

    /**
     * Getter for property cosemObjectFactory.
     *
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    @Override
    public final StoredValues getStoredValues() {
        return this.storedValuesImpl;
    }

    @Override
    public final RegisterValue readRegister(final ObisCode obisCode) throws IOException {
        try {

            final UniversalObject uo = getMeterConfig().findObject(obisCode);
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                final Register register = getCosemObjectFactory().getRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister register = getCosemObjectFactory().getDemandRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
                return new RegisterValue(obisCode, register.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
                final Disconnector register = getCosemObjectFactory().getDisconnector(obisCode);
                return new RegisterValue(obisCode, "" + register.getState());
            }

            return this.ocm.getRegisterValue(obisCode);

        } catch (final Exception e) {
            throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
        }
    }

    @Override
    public final RegisterInfo translateRegister(final ObisCode obisCode) {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public final List<MessageCategorySpec> getMessageCategories() {
        final List<MessageCategorySpec> categories = new ArrayList<>();

        // Firmware.
        //categories.add(this.getFirmwareMessages());

        // Disconnect control.
        final MessageCategorySpec catDisconnect = new MessageCategorySpec("Disconnect Control");

        MessageSpec msgSpec = addConnectControl("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, false);
        catDisconnect.addMessageSpec(msgSpec);
        msgSpec = addConnectControl("Connect", RtuMessageConstant.CONNECT_LOAD, false);
        catDisconnect.addMessageSpec(msgSpec);
        msgSpec = addConnectControlMode("ConnectControl mode", RtuMessageConstant.CONNECT_CONTROL_MODE, false);
        catDisconnect.addMessageSpec(msgSpec);

        // MBus messages.
        final MessageCategorySpec catMbusSetup = new MessageCategorySpec("Mbus setup");

        msgSpec = addNoValueMsg("Decommission", RtuMessageConstant.MBUS_DECOMMISSION, false);
        catMbusSetup.addMessageSpec(msgSpec);
        msgSpec = addEncryptionkeys("Set Encryption keys", RtuMessageConstant.MBUS_ENCRYPTION_KEYS, false);
        catMbusSetup.addMessageSpec(msgSpec);

        categories.add(catDisconnect);
        categories.add(catMbusSetup);

        return categories;
    }

    private MessageSpec addConnectControl(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addConnectControlMode(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.CONNECT_MODE, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addNoValueMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addEncryptionkeys(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_OPEN_KEY, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.MBUS_TRANSFER_KEY, false));
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public final String writeMessage(final Message msg) {
        return msg.write(this);
    }

    @Override
    public final String writeTag(final MessageTag msgTag) {
        final StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (final Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            final MessageAttribute att = it.next();
            if ((att.getValue() == null) || (att.getValue().isEmpty())) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            builder.append("/>");
            return builder.toString();
        }
        builder.append(">");
        // c. sub elements
        for (final Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            final MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                final String value = writeValue((MessageValue) elt);
                if ((value == null) || (value.isEmpty())) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public final String writeValue(final MessageValue msgValue) {
        return msgValue.getValue();
    }

    @Override
    public void applyMessages(final List messageEntries) {
        // Not implemented for this protocol.
    }

    private void importMessage(final String message, final DefaultHandler handler) throws ImportMessageException {
        try {

            final byte[] bai = message.getBytes();
            final InputStream i = new ByteArrayInputStream(bai);

            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (final ParserConfigurationException | SAXException thrown) {
            thrown.printStackTrace();
            throw new ImportMessageException(thrown);
        } catch (final IOException thrown) {
            thrown.printStackTrace();
            throw new ImportMessageException(thrown);
        }
    }

    private Array convertUnixToDateTimeArray(final String strDate) throws IOException {
        try {
            final Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTimeInMillis(Long.parseLong(strDate) * 1000);
            final byte[] dateBytes = new byte[5];
            dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
            dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
            dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
            dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
            dateBytes[4] = (byte) 0xFF;
            final OctetString date = OctetString.fromByteArray(dateBytes);
            final byte[] timeBytes = new byte[4];
            timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
            timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
            timeBytes[2] = (byte) 0x00;
            timeBytes[3] = (byte) 0x00;
            final OctetString time = OctetString.fromByteArray(timeBytes);

            final Array dateTimeArray = new Array();
            dateTimeArray.addDataType(time);
            dateTimeArray.addDataType(date);
            return dateTimeArray;
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + strDate + " to a long value");
        }
    }

    private byte[] convertStringToByte(final String string) throws IOException {
        try {
            final byte[] b = new byte[string.length() / 2];
            int offset = 0;
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) Integer.parseInt(string.substring(offset, offset += 2), 16);
            }
            return b;
        } catch (final NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("String " + string + " can not be formatted to byteArray");
        }
    }

    /**
     * Indicates whether the message concerns an EpIO upgrade message.
     *
     * @param messageContents The contents of the message.
     * @return <code>true</code> if the message contents concern a firmware upgrade.
     */
    private boolean isEpIOFirmwareUpgrade(final String messageContents) {
        return (messageContents != null) && messageContents.contains("<FirmwareUpdate>");
    }

    /**
     * Upgrades the remote device using the image specified.
     *
     * @param image The new image to push to the remote device.
     * @throws IOException If an IO error occurs during the upgrade.
     */
    private void upgradeDevice(final byte[] image) throws IOException {
        logger.info("Upgrading EpIO with new firmware image of size [" + image.length + "] bytes");

        final ImageTransfer imageTransfer = this.getCosemObjectFactory().getImageTransfer();

        logger.info("Converting received image to binary using a Base64 decoder...");

        final Base64EncoderDecoder decoder = new Base64EncoderDecoder();
        final byte[] binaryImage = decoder.decode(new String(image));

        logger.info("Commencing upgrade...");

        imageTransfer.upgrade(binaryImage);
        imageTransfer.imageActivation();

        logger.info("Upgrade has finished successfully...");
    }

    @Override
    public final MessageResult queryMessage(final MessageEntry messageEntry) {
        if (isEpIOFirmwareUpgrade(messageEntry.getContent())) {
            logger.info("Received a firmware upgrade message, using firmware message builder...");

            final FirmwareUpdateMessageBuilder builder = new FirmwareUpdateMessageBuilder(deviceMessageFileFinder, deviceMessageFileExtractor);

            try {
                builder.initFromXml(messageEntry.getContent());
            } catch (final SAXException e) {
                logger.log(Level.SEVERE, "Cannot process firmware upgrade message due to an XML parsing error [" + e.getMessage() + "]", e);

                // Set the message failed.
                return MessageResult.createFailed(messageEntry);
            } catch (final IOException e) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE, "Got an IO error when loading firmware message content [" + e.getMessage() + "]", e);
                }

                return MessageResult.createFailed(messageEntry);
            }

            // We requested an inlined file...
            if (builder.getUserFileContent() != null) {
                logger.info("Pulling out user file and dispatching to the device...");

                final byte[] upgradeFileData = builder.getUserFileContent().getBytes();

                if (upgradeFileData.length > 0) {
                    try {
                        this.upgradeDevice(upgradeFileData);
                    } catch (final IOException e) {
                        if (logger.isLoggable(Level.SEVERE)) {
                            logger.log(Level.SEVERE, "Caught an IO error when trying upgrade [" + e.getMessage() + "]", e);
                        }
                    }
                } else {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Length of the upgrade file is not valid [" + upgradeFileData.length + " bytes], failing message.");
                    }

                    return MessageResult.createFailed(messageEntry);
                }
            } else {
                logger.log(Level.WARNING, "The message did not contain a user file to use for the upgrade, message fails...");

                return MessageResult.createFailed(messageEntry);
            }

            logger.info("Upgrade message has been processed successfully, marking message as successfully processed...");

            return MessageResult.createSuccess(messageEntry);
        } else {
            final MessageHandler messageHandler = new MessageHandler();
            boolean success = false;
            try {
                importMessage(messageEntry.getContent(), messageHandler);

                final boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
                final boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
                final boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
                final boolean decommission = messageHandler.getType().equals(RtuMessageConstant.MBUS_DECOMMISSION);
                final boolean mbusEncryption = messageHandler.getType().equals(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);

                if (connect) {

                    getLogger().log(Level.INFO, "Handling MbusMessage " + messageEntry + ": Connect");

                    if (!"".equals(messageHandler.getConnectDate())) { // use the
                        // disconnectControlScheduler

                        final Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
                        final SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMBusPhysicalAddress()).getObisCode());

                        final ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMBusPhysicalAddress()).getObisCode());
                        final byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
                        final Structure scriptStruct = new Structure();
                        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
                        scriptStruct.addDataType(new Unsigned16(2)); // method '2'
                        // is the
                        // 'remote_connect'
                        // method

                        sasConnect.writeExecutedScript(scriptStruct);
                        sasConnect.writeExecutionTime(executionTimeArray);

                    } else { // immediate connect
                        final Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMBusPhysicalAddress()).getObisCode());
                        connector.remoteReconnect();
                    }

                    success = true;

                } else if (disconnect) {

                    getLogger().log(Level.INFO, "Handling MbusMessage " + messageEntry + ": Disconnect");

                    if (!messageHandler.getDisconnectDate().equals("")) { // use the
                        // disconnectControlScheduler

                        final Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
                        final SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMBusPhysicalAddress()).getObisCode());

                        final ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMBusPhysicalAddress()).getObisCode());
                        final byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
                        final Structure scriptStruct = new Structure();
                        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
                        scriptStruct.addDataType(new Unsigned16(1)); // method '1'
                        // is the
                        // 'remote_disconnect'
                        // method

                        sasDisconnect.writeExecutedScript(scriptStruct);
                        sasDisconnect.writeExecutionTime(executionTimeArray);

                    } else { // immediate disconnect
                        final Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMBusPhysicalAddress()).getObisCode());
                        connector.remoteDisconnect();
                    }

                    success = true;
                } else if (connectMode) {

                    getLogger().log(Level.INFO, "Handling message " + messageEntry + ": ConnectControl mode");
                    final String mode = messageHandler.getConnectControlMode();

                    if (mode != null) {
                        try {
                            final int modeInt = Integer.parseInt(mode);

                            if ((modeInt >= 0) && (modeInt <= 6)) {
                                final Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMBusPhysicalAddress()).getObisCode());
                                connectorMode.writeControlMode(new TypeEnum(modeInt));

                            } else {
                                throw new IOException("Mode is not a valid entry for message " + messageEntry + ", value must be between 0 and 6");
                            }

                        } catch (final NumberFormatException e) {
                            e.printStackTrace();
                            throw new IOException("Mode is not a valid entry for message " + messageEntry);
                        }
                    } else {
                        // should never get to the else, can't leave message empty
                        throw new IOException("Message " + messageEntry + " can not be empty");
                    }

                    success = true;
                } else if (decommission) {

                    getLogger().log(Level.INFO, "Handling MbusMessage " + messageEntry + ": Decommission MBus device");

                    final MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMBusPhysicalAddress()).getObisCode());
                    mbusClient.deinstallSlave();

                    success = true;
                } else if (mbusEncryption) {

                    getLogger().log(Level.INFO, "Handling MbusMessage " + messageEntry + ": Set encryption keys");

                    final String openKey = messageHandler.getOpenKey();
                    final String transferKey = messageHandler.getTransferKey();

                    final MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMBusPhysicalAddress()).getObisCode());

                    if (openKey == null) {
                        mbusClient.setEncryptionKey("");
                    } else if (transferKey != null) {
                        mbusClient.setEncryptionKey(convertStringToByte(openKey));
                        mbusClient.setTransportKey(convertStringToByte(transferKey));
                    } else {
                        throw new IOException("Transfer key may not be empty when setting the encryption keys.");
                    }

                    success = true;
                } else { // unknown message
                    success = false;
                }

                if (success) {
                    getLogger().log(Level.INFO, "Message " + messageEntry + " has finished.");
                    return MessageResult.createSuccess(messageEntry);
                } else {
                    getLogger().log(Level.INFO, "Message " + messageEntry + " has failed.");
                    return MessageResult.createFailed(messageEntry);
                }
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Caught an IO error while querying message [" + messageEntry.getTrackingId() + "], message was [" + e.getMessage() + "]", e);

                return MessageResult.createFailed(messageEntry);
            } catch (final ImportMessageException e) {
                logger.log(Level.SEVERE, "Caught an import error while querying message [" + messageEntry.getTrackingId() + "], message was [" + e.getMessage() + "]", e);

                return MessageResult.createFailed(messageEntry);
            }
        }
    }

    /**
     * Fetches the device serial number from the device itself.
     *
     * @return The device serial number from the device itself.
     * @throws IOException If an error occurs during the communication with the device.
     */
    private String getDeviceSerialNumber() throws IOException {
        if (this.deviceSerialNumber == null) {
            this.deviceSerialNumber = this.getCosemObjectFactory().getData(OBISCODE_R2_SERIAL_NUMBER).getString();
        }

        return this.deviceSerialNumber;
    }

    /**
     * Returns the CyNet RF network topology. This is a really specific method that will poll a specific register on the EpIO (in which the current
     * RF topology is stored).
     *
     * @return The CyNet RF network topology. The string has one line per node in the topology, and each line is manufacturerID and routing address,
     * separated by a comma.
     */
    public final String getRFNetworkTopology() throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Requesting RF network topology from Z3.");
        }

        try {
            final Data cosemTopology = this.getCosemObjectFactory().getData(OBIS_CODE_NETWORK_TOPOLOGY);

            if (cosemTopology != null) {
                final StringBuilder stringBuilder = new StringBuilder();
                final DataStructure root = (cosemTopology).getDataContainer().getRoot();

                for (int i = 0; i < root.element.length; i++) {
                    final DataStructure topologyEntry = (DataStructure) root.element[i];

                    final String manufacturerId = Long.toHexString((((Integer) topologyEntry.element[0]).intValue() & 0xFFFFFFFFl));
                    final String routingAddress = Long.toHexString((((Integer) topologyEntry.element[1]).intValue() & 0xFFFFFFFFl));

                    stringBuilder.append(manufacturerId).append(',').append(routingAddress).append("\n");
                }

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Got routing table [" + stringBuilder.toString() + "] from the Z3.");
                }

                return stringBuilder.toString();
            } else {
                logger.log(Level.WARNING, "Query for OBIS code [" + OBIS_CODE_NETWORK_TOPOLOGY + "] did not yield any result, assuming no RF available.");
            }

            // The EpIO did not return anything when requested for the particular register, so we do neither.
            return null;
        } catch (final DataAccessResultException e) {
            if (e.getCode() == DataAccessResultCode.OBJECT_UNDEFINED) {
                // No such register.
                logger.log(Level.INFO, "The EpIO says there is no register with Obis code [" + OBIS_CODE_NETWORK_TOPOLOGY + "], assuming it is not acting as an RF master.");
            }

            return null;
        }
    }

    private class ImportMessageException extends Exception {
        private ImportMessageException(Throwable cause) {
            super(cause);
        }
    }

}