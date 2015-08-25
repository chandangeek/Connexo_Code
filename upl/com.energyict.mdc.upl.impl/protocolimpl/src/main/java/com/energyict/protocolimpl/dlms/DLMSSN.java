/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Base class that implements the DLMS SN (short name) protocol
 * <BR>
 * <B>@beginchanges</B><BR>
 *      KV 08042003 Initial version.<BR>
 *      KV 08102003 Save dstFlag when getTime() to be used in setTime()
 *      KV 14072004 DLMSMeterConfig made multithreaded! singleton pattern implementation removed!
 *      KV 20082004 Extended with obiscode mapping for register reading + start reengineering to use cosem package
 *      KV 30082004 Reengineered to use cosem package
 *@endchanges
 */


package com.energyict.protocolimpl.dlms;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.CosemPDUConnection;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSObis;
import com.energyict.dlms.HDLC2Connection;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.SecureConnection;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.CacheMechanism;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.dlms.siemenszmd.StoredValuesImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


abstract public class DLMSSN extends PluggableMeterProtocol implements HHUEnabler, ProtocolLink, CacheMechanism {

    protected abstract String getDeviceID();

    protected abstract void buildProfileData(byte bNROfChannels, ProfileData profileData, ScalerUnit[] scalerunit, UniversalObject[] intervalList) throws IOException;

    protected abstract void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;

    protected abstract void getEventLog(ProfileData profileDate, Calendar fromCalendar, Calendar toCalendar) throws IOException;

    /**
     * @return the used {@link com.energyict.dlms.aso.SecurityProvider}
     */
    protected abstract SecurityProvider getSecurityProvider();

    /**
     * Configure the {@link ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    protected abstract ConformanceBlock configureConformanceBlock();

    private static final byte DEBUG = 0;
    protected static final int CONNECTION_MODE_HDLC = 0;
    protected static final int CONNECTION_MODE_TCPIP = 1;
    protected static final int CONNECTION_MODE_COSEMPDU = 2;
    protected static final int PROPOSED_QOS = -1;
    protected static final int PROPOSED_DLMS_VERSION = 6;
    protected static final String MAX_PDU_SIZE = "-1";
    protected static final String PROPNAME_IIAP_INVOKE_ID = "IIAPInvokeId";
    protected static final String PROPNAME_IIAP_PRIORITY = "IIAPPriority";
    protected static final String PROPNAME_IIAP_SERVICE_CLASS = "IIAPServiceClass";
    protected static final String PROPNAME_CIPHERING_TYPE = "CipheringType";
    protected static final String PROPNAME_MAX_PDU_SIZE = "MaxPduSize";
    protected static final String PROPNAME_IFORCEDELAY_BEFORE_SEND = "ForcedDelay";
    private DLMSCache dlmsCache = new DLMSCache();
    protected ApplicationServiceObject aso;
    protected ConformanceBlock conformanceBlock;
    protected SecurityContext securityContext;
    protected String firmwareVersion;

    protected String strID;
    protected String strPassword;

    protected int iHDLCTimeoutProperty;
    protected int iProtocolRetriesProperty;
    protected int iDelayAfterFailProperty;
    protected int iSecurityLevelProperty;
    protected int iRequestTimeZone;
    protected int iRoundtripCorrection;
    protected int iClientMacAddress;
    protected int iServerUpperMacAddress;
    protected int iServerLowerMacAddress;
    protected int iRequestClockObject;
    protected int datatransportSecurityLevel;
    protected int authenticationSecurityLevel;
    protected int iiapPriority;
    protected int iiapServiceClass;
    protected int iiapInvokeId;
    protected int cipheringType;
    protected int maxPduSize;
    protected String nodeId;
    private String configuredSerialNumber;
    private int iInterval = -1;
    private int iNROfIntervals = -1;
    private int extendedLogging;
    protected ProtocolChannelMap channelMap;
    protected int iForceDelay; // delay before each command is send over the line (default 100ms)

    private DLMSConnection dlmsConnection = null;
    private CosemObjectFactory cosemObjectFactory = null;
    private StoredValuesImpl storedValuesImpl = null;

    // lazy initializing
    private int iNumberOfChannels = -1;
    private int iMeterTimeZoneOffset = 255;
    private int iConfigProgramChange = -1;

    /**
     * Contains the Configuration of a DLMS meter
     */
    private DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();
    ;

    // Added for MeterProtocol interface implementation
    private Logger logger = null;
    private TimeZone timeZone = null;
    private Properties properties = null;

    // filled in when getTime is invoked!
    private int dstFlag; // -1=unknown, 0=not set, 1=set
    int addressingMode;
    int connectionMode;

    /**
     * Creates a new instance of DLMSSN, empty constructor
     */
    public DLMSSN() {

    } // public DLMSSN(...)

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    /**
     * initializes the receiver
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;

        dstFlag = -1;
        iNumberOfChannels = -1; // Lazy initializing
        iMeterTimeZoneOffset = 255; // Lazy initializing
        iConfigProgramChange = -1; // Lazy initializing
        iInterval = -1;
        iNROfIntervals = -1;

        cosemObjectFactory = new CosemObjectFactory(this);
        storedValuesImpl = new StoredValuesImpl(cosemObjectFactory);
        initDLMSConnection(inputStream, outputStream);
    }

    protected byte[] getSystemIdentifier() {
        return null;
    }

    /**
     * Starts the DLMS connection
     *
     * @throws IOException when the connection failed
     */
    protected void initDLMSConnection(InputStream inputStream, OutputStream outputStream) throws IOException {
        DLMSConnection connection;
        try {
            switch (connectionMode) {
                case CONNECTION_MODE_HDLC:
                    connection = new HDLC2Connection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress,
                            iServerLowerMacAddress, iServerUpperMacAddress, addressingMode, -1, -1);
                    break;
                case CONNECTION_MODE_TCPIP:
                    connection = new TCPIPConnection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress, getLogger());
                    break;
                case CONNECTION_MODE_COSEMPDU:
                    connection = new CosemPDUConnection(inputStream, outputStream, iHDLCTimeoutProperty, iForceDelay, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress);
                    break;
                default:
                    throw new IOException("Unable to initialize dlmsConnection, connection property unknown: " + connectionMode);
            }
        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }

        SecurityProvider localSecurityProvider = getSecurityProvider();
        securityContext = new SecurityContext(datatransportSecurityLevel, authenticationSecurityLevel, 0, getSystemIdentifier(), localSecurityProvider, this.cipheringType);
        this.conformanceBlock = configureConformanceBlock();
        if (this.conformanceBlock == null) {
            if (getReference() == ProtocolLink.SN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
            } else if (getReference() == ProtocolLink.LN_REFERENCE) {
                this.conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
            } else {
                throw new InvalidPropertyException("Invalid reference method, only 0 and 1 are allowed.");
            }
        }

        XdlmsAse xdlmsAse = new XdlmsAse(isCiphered() ? localSecurityProvider.getDedicatedKey() : null, true, PROPOSED_QOS, PROPOSED_DLMS_VERSION, this.conformanceBlock, maxPduSize);
        aso = new ApplicationServiceObject(xdlmsAse, this, securityContext, getContextId());
        dlmsConnection = new SecureConnection(aso, connection);
        InvokeIdAndPriorityHandler iiapHandler = buildInvokeIdAndPriorityHandler();
        this.dlmsConnection.setInvokeIdAndPriorityHandler(iiapHandler);
    }

    /**
     * Returns a boolean whether or not there's encryption used
     *
     * @return boolean
     */
    protected boolean isCiphered() {
        return (getContextId() == AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING) || (getContextId() == AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING);
    }

    /**
     * Getter for the context ID
     *
     * @return the context ID
     */
    protected int getContextId() {
        if (getReference() == ProtocolLink.LN_REFERENCE) {
            return (this.datatransportSecurityLevel == 0) ? AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING :
                    AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
        } else if (getReference() == ProtocolLink.SN_REFERENCE) {
            return (this.datatransportSecurityLevel == 0) ? AssociationControlServiceElement.SHORT_NAME_REFERENCING_NO_CIPHERING :
                    AssociationControlServiceElement.SHORT_NAME_REFERENCING_WITH_CIPHERING;
        } else {
            throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
        }
    }

    /**
     * Creates an InvokeIdAndPriorityHandler using the defined properties
     *
     * @return the constructed object
     * @throws DLMSConnectionException if some of the properties aren't valid
     */
    protected InvokeIdAndPriorityHandler buildInvokeIdAndPriorityHandler() throws IOException {
        try {
            InvokeIdAndPriority iiap = new InvokeIdAndPriority();
            iiap.setPriority(this.iiapPriority);
            iiap.setServiceClass(this.iiapServiceClass);
            iiap.setTheInvokeId(this.iiapInvokeId);
            return new NonIncrementalInvokeIdAndPriorityHandler(iiap);
        } catch (DLMSConnectionException e) {
            getLogger().info("Some configured properties are invalid. " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Subclasses may override
     *
     * @return the current profileinterval in seconds
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        if (iInterval == -1) {
            iInterval = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCapturePeriod();
        }
        return iInterval;
    } // public int getProfileInterval() throws UnsupportedException, IOException

    /**
     * this implementation throws UnSupportedException. Subclasses may override
     *
     * @return the number of channels
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (iNumberOfChannels == -1) {
            meterConfig.setCapturedObjectList(getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjectsAsUniversalObjects());
            iNumberOfChannels = meterConfig.getNumberOfChannels();
        }
        return iNumberOfChannels;
    } // public int getNumberOfChannels()  throws IOException

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }

    private ScalerUnit getMeterDemandRegisterScalerUnit(int iChannelNR) throws IOException {
        ObisCode obisCode = meterConfig.getMeterDemandObject(iChannelNR).getObisCode();
        return getCosemObjectFactory().getCosemObject(obisCode).getScalerUnit();
        //return doGetMeterReadingScalerUnit(uo.getBaseName(), uo.getScalerAttributeOffset());
    }

    @Override
    public ApplicationServiceObject getAso() {
        return aso;
    }

    /**
     * Creates an association session
     *
     * @throws IOException when the communication with the meter failed
     */
    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();
                checkCacheObjects();
                validateSerialNumber();
            }

        } catch (DLMSConnectionException e) {
            IOException exception = new IOException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Check the cached objects, update them if necessary (indicated by the iConfig value)
     *
     * @throws IOException when the communication with the meter failed
     */
    protected void checkCacheObjects() throws IOException {
        try { // conf program change and object list stuff
            int iConf;

            if (dlmsCache == null) {
                dlmsCache = new DLMSCache();
            }

            if (dlmsCache.getObjectList() != null) {
                meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                try {
                    iConf = requestConfigurationProgramChanges();
                } catch (IOException e) {
                    iConf = -1;
                    logger.severe("DLMSSN Configuration change count not accessible, request object list.");
                    requestObjectList();
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                }

                if (iConf != dlmsCache.getConfProgChange()) {
                    logger.severe("DLMSSN configuration changed, request object list.");
                    requestObjectList();           // request object list again from rtu
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                }
            } else { // Cache not exist
                logger.info("DLMSSN cache does not exist, request object list.");
                requestObjectList();
                try {
                    iConf = requestConfigurationProgramChanges();
                    dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
                    dlmsCache.setConfProgChange(iConf);  // set new configuration program change
                } catch (IOException e) {
                    iConf = -1;
                }
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }

    }

    /*
    *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
    */

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        Iterator it;

        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString() + " " + uo.getObisCode().getDescription() + " - ShortName : " + uo.getBaseName() + " (0x" + Integer.toHexString(uo.getBaseName()) + ") - ClassId : " + uo.getClassID() + "\n");
        }

        if (getDeviceID().compareTo("EIT") != 0) {
            // all billing points values...
            strBuff.append("********************* Objects captured into billing points *********************\n");
            it = getCosemObjectFactory().getStoredValues().getProfileGeneric().getCaptureObjects().iterator();
            while (it.hasNext()) {
                CapturedObject capturedObject = (CapturedObject) it.next();
                strBuff.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription() + " (billing point)\n");
            }
        }

        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getCaptureObjects().iterator();
        while (it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject) it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription() + " (load profile)\n");
        }

        return strBuff.toString();
    }

    /**
     * Disconnect, stop the association session
     *
     * @throws IOException when the communication with the meter failed
     */
    public void disconnect() throws IOException {
        try {
            if ((this.aso != null) && (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED)) {
                this.aso.releaseAssociation();
            }
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        } catch (DLMSConnectionException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    /**
     * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        Calendar calendar = null;
        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                calendar = ProtocolUtils.getCalendar(false, requestTimeZone());
            } else if (dstFlag == 1) {
                calendar = ProtocolUtils.getCalendar(true, requestTimeZone());
            } else {
                throw new IOException("setTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            calendar = ProtocolUtils.initCalendar(false, getTimeZone());
        }

        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        doSetTime(calendar);
    } // public void setTime() throws IOException


    private void doSetTime(Calendar calendar) throws IOException {
        //byte[] responseData;
        byte[] byteTimeBuffer = new byte[14];
        int i;

//        byteTimeBuffer[0]=1;  This caused an extra 0x01 in the requestBuffer
//      DLMS code has changed (read -> corrected) which causes this to be obsolete

        byteTimeBuffer[0] = AxdrType.OCTET_STRING.getTag();
        byteTimeBuffer[1] = 12; // length
        byteTimeBuffer[2] = (byte) (calendar.get(calendar.YEAR) >> 8);
        byteTimeBuffer[3] = (byte) calendar.get(calendar.YEAR);
        byteTimeBuffer[4] = (byte) (calendar.get(calendar.MONTH) + 1);
        byteTimeBuffer[5] = (byte) calendar.get(calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(calendar.DAY_OF_WEEK);
        byteTimeBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteTimeBuffer[7] = (byte) calendar.get(calendar.HOUR_OF_DAY);
        byteTimeBuffer[8] = (byte) calendar.get(calendar.MINUTE);
        byteTimeBuffer[9] = (byte) calendar.get(calendar.SECOND);
        byteTimeBuffer[10] = (byte) 0xFF;
        byteTimeBuffer[11] = (byte) 0x80;
        byteTimeBuffer[12] = 0x00;

        if (isRequestTimeZone()) {
            if (dstFlag == 0) {
                byteTimeBuffer[13] = 0x00;
            } else if (dstFlag == 1) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                throw new IOException("doSetTime(), dst flag is unknown! setTime() before getTime()!");
            }
        } else {
            if (getTimeZone().inDaylightTime(calendar.getTime())) {
                byteTimeBuffer[13] = (byte) 0x80;
            } else {
                byteTimeBuffer[13] = 0x00;
            }
        }

        getCosemObjectFactory().getGenericWrite((short) meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_TIME).write(byteTimeBuffer);

    } // private void doSetTime(Calendar calendar)

    /**
     * Method that requests the time/date in the remote meter.
     *
     * @return Date representing the time/date of the remote meter.
     * @throws IOException
     */
    public Date getTime() throws IOException {
        Clock clock = getCosemObjectFactory().getClock();
        Date date = clock.getDateTime();
        dstFlag = clock.getDstFlag();
        return date;
    } // public Date getTime() throws IOException

    private boolean requestDaylightSavingEnabled() throws IOException {
        return getCosemObjectFactory().getClock().isDsEnabled();
    } // private boolean requestDaylightSavingEnabled() throws IOException

    /**
     * This method requests for the COSEM object list in the remote meter. A list is byuild with LN and SN references.
     * This method must be executed before other request methods.
     *
     * @throws IOException
     */
    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationSN().getBuffer());
    } // public void requestObjectList() throws IOException


    protected void validateSerialNumber() throws IOException {
        if ((configuredSerialNumber == null) || ("".compareTo(configuredSerialNumber) == 0)) {
            return;
        }
        String sn = (String) getSerialNumber();
        if ((sn != null) && (sn.compareTo(configuredSerialNumber) == 0)) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + sn + ", configured sn=" + configuredSerialNumber);
    }

    public String getConfiguredSerialNumber() {
        return configuredSerialNumber;
    }

    /**
     * This method requests for the COSEM object Logical Name register.
     *
     * @throws IOException
     */
    private String requestLNREG() throws IOException {
        return getCosemObjectFactory().getData(DLMSCOSEMGlobals.LNREG_OBJECT_SN).getString();
    } // public String requestLNREG() throws IOException


    /**
     * This method requests for the COSEM object Logical Name register.
     *
     * @throws IOException
     */
    private String requestAttribute(int iBaseName, int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).getDataContainer().toString();
    } // public void requestAttribute(int iBaseName,int iOffset) throws IOException


    /**
     * This method requests for the version string.
     *
     * @return String representing the version.
     * @throws IOException
     */
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        UniversalObject uo = meterConfig.getVersionObject();
        return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
    } // public String getFirmwareVersion()

    /**
     * Return the serialNumber of the device.
     *
     * @return the serialNumber of the device.
     * @throws IOException if an error occurs during the read
     */
    public String getSerialNumber() throws IOException {
        UniversalObject uo = meterConfig.getSerialNumberObject();
        return getCosemObjectFactory().getGenericRead(uo.getBaseName(), uo.getValueAttributeOffset()).getString();
    }

    /**
     * This method requests for the NR of intervals that can be stored in the memory of the remote meter.
     *
     * @return NR of intervals that can be stored in the memory of the remote meter.
     * @throws IOException
     */
    private int getNROfIntervals() throws IOException {
        if (iNROfIntervals == -1) {
            iNROfIntervals = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getProfileEntries();
        } // if (iNROfIntervals == -1)
        return iNROfIntervals;
    } // private int getNROfIntervals() throws IOException


    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        int iNROfIntervals = getNROfIntervals();
        int iInterval = getProfileInterval() / 60;
        Calendar fromCalendar = ProtocolUtils.getCalendar(getTimeZone());
        fromCalendar.add(Calendar.MINUTE, (-1) * iNROfIntervals * iInterval);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(lastReading);
        return doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getTimeZone()), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        toCalendar.setTime(to);
        return doGetProfileData(fromCalendar, toCalendar, includeEvents);
    }

    private ProfileData doGetProfileData(Calendar fromCalendar, Calendar toCalendar, boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte) getNumberOfChannels();     //GN |13052008| otherwise this stays at -1
        return doGetDemandValues(fromCalendar, toCalendar, bNROfChannels, includeEvents);
    }

    private ProfileData doGetDemandValues(Calendar fromCalendar, Calendar toCalendar, byte bNROfChannels, boolean includeEvents) throws IOException {
        ProfileData profileData;
        ScalerUnit[] scalerunit;

        UniversalObject[] intervalList = getCosemObjectFactory().getLoadProfile().getProfileGeneric().getBufferAsUniversalObjects(fromCalendar, toCalendar);
        profileData = new ProfileData();
        scalerunit = new ScalerUnit[bNROfChannels];
        for (int i = 0; i < bNROfChannels; i++) {
            scalerunit[i] = getMeterDemandRegisterScalerUnit(i);
            ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getDeviceID() + "_channel_" + i, scalerunit[i].getEisUnit());
            if (scalerunit[i].getEisUnit().isUndefined()) {
                channelInfo.setMultiplier(new BigDecimal(new BigInteger("1"), -scalerunit[i].getEisUnit().getScale()));
            }

            if (DEBUG >= 1) {
                System.out.println("KV_DEBUG> " + meterConfig.getChannelObject(i).toStringCo());
            }

            if (meterConfig.getChannelObject(i).isCapturedObjectCumulative()) {

                if (meterConfig.getChannelObject(i).isCapturedObjectPulses()) {
                    if (channelMap != null) {
                        channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null) ? channelMap.getProtocolChannel(i).getWrapAroundValue() : BigDecimal.valueOf(Long.MAX_VALUE));
                    } else {
                        channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(Long.MAX_VALUE));
                        if (DEBUG >= 1) {
                            System.out.println("KV_DEBUG> channel " + i + " is cumulative 64 bit");
                        }
                    }
                } else {
                    if (channelMap != null) {
                        channelInfo.setCumulativeWrapValue((channelMap.getProtocolChannel(i) != null) ? channelMap.getProtocolChannel(i).getWrapAroundValue() : BigDecimal.valueOf(2 ^ 32));
                    } else {
                        channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(0xFFFFFFFFl));
                        if (DEBUG >= 1) {
                            System.out.println("KV_DEBUG> channel " + i + " is cumulative 32 bit");
                        }
                    }
                }
            }
            profileData.addChannel(channelInfo);
        }

        buildProfileData(bNROfChannels, profileData, scalerunit, intervalList);

        if (includeEvents) {
            getEventLog(profileData, fromCalendar, toCalendar);
            // Apply the events to the channel statusvalues
            profileData.applyEvents(getProfileInterval() / 60);
        }

        return profileData;

    } // private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException


    /**
     * This method can be used to set a specific attribute in anremote meter object.
     *
     * @param str     Index to the Long name OBIS reference.
     * @param sOffset Offset to the attribute.
     * @param data    Byte array to send.
     * @throws IOException
     */
    private void setValue(String str, short sOffset, byte[] data) throws IOException {
        DLMSObis dlmsObis = new DLMSObis(str);
        getCosemObjectFactory().getGenericWrite((short) meterConfig.getObject(dlmsObis).getBaseName(), (dlmsObis.getOffset() - 1) * 8).write(data);
    } // public void setValue(...) throws IOException


    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see #validateProperties(java.util.Properties)
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        this.properties = properties;
        validateProperties(properties);
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            // KV 19012004 get the serialNumber
            configuredSerialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
            addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "-1"));
            connectionMode = Integer.parseInt(properties.getProperty("Connection", "0")); // 0=HDLC, 1= TCP/IP, 2=cosemPDUconnection
            if (properties.getProperty("ChannelMap", "").equalsIgnoreCase("")) {
                channelMap = null;
            } else {
                channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap"));
            }
            String[] securityLevel = properties.getProperty("SecurityLevel", "1").split(":");
            this.authenticationSecurityLevel = Integer.parseInt(securityLevel[0]);
            if (securityLevel.length == 2) {
                this.datatransportSecurityLevel = Integer.parseInt(securityLevel[1]);
            } else if (securityLevel.length == 1) {
                this.datatransportSecurityLevel = 0;
            } else {
                throw new IllegalArgumentException("SecurityLevel property contains an illegal value " + properties.getProperty("SecurityLevel", "1"));
            }
            iiapInvokeId = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_INVOKE_ID, "0"));
            iiapPriority = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_PRIORITY, "1"));
            iiapServiceClass = Integer.parseInt(properties.getProperty(PROPNAME_IIAP_SERVICE_CLASS, "1"));
            cipheringType = Integer.parseInt(properties.getProperty(PROPNAME_CIPHERING_TYPE, Integer.toString(CipheringType.GLOBAL.getType())));
            maxPduSize = Integer.parseInt(properties.getProperty(PROPNAME_MAX_PDU_SIZE, MAX_PDU_SIZE));
            iForceDelay = Integer.parseInt(properties.getProperty(PROPNAME_IFORCEDELAY_BEFORE_SEND, "100"));
            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(" validateProperties, NumberFormatException, " + e.getMessage());
        }

    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name <br>
     * @return the register value
     * @throws IOException             <br>
     * @throws UnsupportedException    <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {

        DLMSObis ln = new DLMSObis(name);

        if (ln.isLogicalName()) {
            String str = requestAttribute(meterConfig.getObject(ln).getBaseName(), (short) ((ln.getOffset() - 1) * 8));
            return str;
        } else if (name.compareTo("PROGRAM_CONF_CHANGES") == 0) {
            return String.valueOf(requestConfigurationProgramChanges());
        } else if (name.compareTo("GET_CLOCK_OBJECT") == 0) {
            requestClockObject();
            return null;
        } else {
            throw new NoSuchRegisterException("DLMS,getRegister, register " + name + " does not exist.");
        }

    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException             <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException    <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("RequestClockObject");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("AddressingMode");
        result.add("EventIdIndex");
        result.add("ChannelMap");
        result.add("Connection");
        result.add(PROPNAME_CIPHERING_TYPE);
        result.add(PROPNAME_IIAP_INVOKE_ID);
        result.add(PROPNAME_IIAP_PRIORITY);
        result.add(PROPNAME_IIAP_SERVICE_CLASS);
        result.add(PROPNAME_MAX_PDU_SIZE);
        result.add(PROPNAME_IFORCEDELAY_BEFORE_SEND);
        return result;
    }

    private void requestClockObject() {
        if (iRequestClockObject == 1) {
            try {
                logger.severe("DLMSSN Clock time                       : " + getTime());
            } catch (IOException e) {
                logger.severe("time attribute error");
            }
            //try{logger.severe ("DLMSSN Clock time_zone                  : "+requestTimeZone());}catch(IOException e){logger.severe ("time_zone attribute error");}
            try {
                logger.severe("DLMSSN Clock time_zone                  : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_TIME_ZONE));
            } catch (IOException e) {
                logger.severe("time_zone attribute error");
            }
            try {
                logger.severe("DLMSSN Clock status                     : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_STATUS));
            } catch (IOException e) {
                logger.severe("status attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_begin     : " + requestAttributeString(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_BEGIN));
            } catch (IOException e) {
                logger.severe("DS begin attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_end       : " + requestAttributeString(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_END));
            } catch (IOException e) {
                logger.severe("DS end attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_savings_deviation : " + requestAttributeLong(meterConfig.getClockSN(), DLMSCOSEMGlobals.TIME_DS_DEVIATION));
            } catch (IOException e) {
                logger.severe("DS deviation attribute error");
            }
            try {
                logger.severe("DLMSSN Clock daylight_saving_enabled    : " + requestDaylightSavingEnabled());
            } catch (IOException e) {
                logger.severe("DS enebled attribute error");
            }

        } // if (iRequestClockObject == 1)

    } // private void requestClockObject()

    public int requestConfigurationProgramChanges() throws IOException {
        if (iConfigProgramChange == -1) {
            iConfigProgramChange = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }
        return iConfigProgramChange;
    } // public int requestConfigurationProgramChanges() throws IOException

    protected int requestTimeZone() throws IOException {
        if (iMeterTimeZoneOffset == 255) {
            iMeterTimeZoneOffset = getCosemObjectFactory().getClock().getTimeZone();
        }
        return iMeterTimeZoneOffset;
    } // protected int requestTimeZone() throws IOException

    private long requestAttributeLong(int iBaseName, int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).getValue();
    } // private long requestAttributeLong(int iBaseName,int iOffset) throws IOException

    private String requestAttributeString(int iBaseName, int iOffset) throws IOException {
        return getCosemObjectFactory().getGenericRead(iBaseName, iOffset).toString();
    } // private String requestAttributeString(int iBaseName,int iOffset) throws IOException

    public boolean isRequestTimeZone() {
        return (iRequestTimeZone != 0);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    public Object getCache() {
        return dlmsCache;
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
            RtuDLMS rtu = new RtuDLMS(rtuid);
            try {
                return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            } catch (NotFoundException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new com.energyict.cbo.BusinessException("invalid RtuId!");
        }
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.contentChanged()) {
                //System.out.println("KV_DEBUG>> rtuid="+rtuid+", "+new Date()+" update cache="+dc.getObjectList()+", confchange="+dc.getConfProgChange()+", ischanged="+dc.isChanged()); // KV_DEBUG
                RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid);
                RtuDLMS rtu = new RtuDLMS(rtuid);
                rtuCache.saveObjectList(dc.getObjectList());
                rtu.setConfProgChange(dc.getConfProgChange());
            }
        } else {
            throw new com.energyict.cbo.BusinessException("invalid RtuId!");
        }
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + strID + "_" + strPassword + "_" + configuredSerialNumber + "_" + iServerUpperMacAddress + "_DLMSSN.cache";
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                (HHUSignOn) new IEC1107HHUConnection(commChannel, iHDLCTimeoutProperty, iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, nodeId);
    }

    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

    public void release() throws IOException {

    }

    /**
     * Getter for property meterConfig.
     *
     * @return Value of property meterConfig.
     */
    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Getter for property cosemObjectFactory.
     *
     * @return Value of property cosemObjectFactory.
     */
    public com.energyict.dlms.cosem.CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public int getReference() {
        return ProtocolLink.SN_REFERENCE;
    }

    public StoredValues getStoredValues() {
        return (StoredValues) storedValuesImpl;
    }

    public void setDLMSConnection(DLMSConnection connection) {
        this.dlmsConnection = connection;
    }

    public Properties getProperties() {
        return this.properties;
    }
} // public class DLMSSN

