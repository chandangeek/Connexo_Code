package com.energyict.protocolimpl.dlms.JanzC280;

import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.CacheMechanism;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/11/11
 * Time: 17:07
 */
public class JanzC280 extends AbstractDLMSProtocol implements CacheMechanism {

    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("0.0.128.0.1.255");
    private static final ObisCode OBISCODE_SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");

    private static final int DEFAULT_FORCED_TO_READ_CACHE = 0;
    private static final int DEFAULT_MAX_PDU_SIZE = 512;
    private static final int DEFAULT_SERVER_LOWER_MAC_ADDRESS = 32;
    private static final int DEFAULT_SERVER_UPPER_MAC_ADDRESS = 1;
    private static final int DEFAULT_CLIENT_MAC_ADDRESS = 16;
    private static final int DEFAULT_INFORMATION_FIELD_SIZE = 96;
    private static final int DEFAULT_CONNECTION_MODE = 0;
    private static final String HHUSIGNON_METERID = "CTR_EBOX";

    private List<Integer> enabledChannelNumbers; // This list will contain the numbers of all enabled channels

    /**
     * Fixed static string for the forcedToReadCache property
     */
    private static final String PROPERTY_FORCEDTOREADCACHE = "ForcedToReadCache";

    /**
     * Property to indicate whether the cache (objectlist) <b>MUST</b> be read out
     */
    private boolean forcedToReadCache;
    private ProfileDataReader profileDataReader = null;

    /**
     * Array containing all load profile OBIS codes.
     */
    private ObisCode[] loadProfileObisCodes;

    private JanzStoredValues storedValues;

    @Inject
    public JanzC280(OrmClient ormClient) {
        super(ormClient);
    }

    @Override
    public void validateSerialNumber() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber) == 0)) {
            return;
        }
        Data data = getCosemObjectFactory().getData(OBISCODE_SERIAL_NUMBER);
        String meterSerialNumber = AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
        if (meterSerialNumber.compareTo(serialNumber) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn=" + meterSerialNumber + ", configured sn=" + serialNumber);
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        this.forcedToReadCache = !properties.getProperty(PROPERTY_FORCEDTOREADCACHE, "0").equalsIgnoreCase(Integer.toString(DEFAULT_FORCED_TO_READ_CACHE));
        this.maxRecPduSize = Integer.parseInt(properties.getProperty(DlmsProtocolProperties.MAX_REC_PDU_SIZE, Integer.toString(DEFAULT_MAX_PDU_SIZE)));
        this.serverLowerMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, Integer.toString(DEFAULT_SERVER_LOWER_MAC_ADDRESS)));
        this.serverUpperMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, Integer.toString(DEFAULT_SERVER_UPPER_MAC_ADDRESS)));
        this.clientMacAddress = Integer.parseInt(properties.getProperty(PROPNAME_CLIENT_MAC_ADDRESS, Integer.toString(DEFAULT_CLIENT_MAC_ADDRESS)));
        this.informationFieldSize = Integer.parseInt(properties.getProperty(PROPNAME_INFORMATION_FIELD_SIZE, Integer.toString(DEFAULT_INFORMATION_FIELD_SIZE)));
        this.connectionMode = Integer.parseInt(properties.getProperty(PROPNAME_CONNECTION, Integer.toString(DEFAULT_CONNECTION_MODE)));
    }

    @Override
    protected InvokeIdAndPriorityHandler buildInvokeIdAndPriorityHandler() throws IOException {
        try {
            InvokeIdAndPriority iiap = new InvokeIdAndPriority();
            iiap.setPriority(this.iiapPriority);
            iiap.setServiceClass(this.iiapServiceClass);
            iiap.setTheInvokeId(this.iiapInvokeId);
            return new IncrementalInvokeIdAndPriorityHandler(iiap);
        } catch (DLMSConnectionException e) {
            getLogger().info("Some configured properties are invalid. " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Check the cached objects, update them if necessary (indicated by the iConfig value)
     *
     * @throws java.io.IOException when the communication with the meter failed
     */
    @Override
    protected void checkCacheObjects() throws IOException {
        try {
            if (dlmsCache == null) {
                dlmsCache = new DLMSCache();
            }
            if (dlmsCache.getObjectList() == null || forcedToReadCache) {
                logger.info(forcedToReadCache ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
                requestObjectList();
                dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
            } else if (dlmsCache.getObjectList() != null) {
                logger.info("Cache exist, will not be read!");
                dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Override this method when requesting time from the meter is needed.
     *
     * @return Date object with the metertime
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    /**
     * Override this method when setting the time in the meter is needed
     *
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public void setTime() throws IOException {
        final Calendar newTimeToSet = Calendar.getInstance(getTimeZone());
        getCosemObjectFactory().getClock().setTimeAttr(new DateTime(newTimeToSet));
    }

    /**
     * Override this method to control the protocolversion.
     *
     * @return String with protocol version
     */
    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    /**
     * Override this method when requesting the meter firmware version is needed. This method is informational only.
     *
     * @return String with firmware version. This can also contain other important info of the meter.
     * @throws java.io.IOException thrown when something goes wrong
     * @throws com.energyict.mdc.protocol.api.UnsupportedException
     *                             Thrown when that method is not supported
     */
    @Override
    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            Data data = getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE);
            firmwareVersion = AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
        }
        return firmwareVersion;
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link MeterProtocol} should be used.
     *
     * @return true is the {@link java.util.TimeZone} is read from the device
     */
    public boolean isRequestTimeZone() {
        return false;
    }

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    public int getRoundTripCorrection() {
        return 0;
    }

    /**
     * Getter for the type of reference used in the DLMS protocol. This can be
     * {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE or {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE
     *
     * @return {@link com.energyict.dlms.ProtocolLink}.SN_REFERENCE for short name or
     *         {@link com.energyict.dlms.ProtocolLink}.LN_REFERENCE for long name
     */
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    /**
     * Override this method when requesting an obiscode mapped register from the meter.
     *
     * @param obisCode obiscode rmapped register to request from the meter
     * @return RegisterValue object
     * @throws java.io.IOException thrown when somethiong goes wrong
     */
    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            if (obisCode.getF() != 255) {
                logger.info("Requesting historic register " + obisCode.toString());
                HistoricalValue historicalValue = getStoredValues().getHistoricalValue(obisCode);
                return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
            }

            /** For implementation efficiency reason, only the first object of each unique type is present in the Object list.
             *  1. Search exact obiscode
             *  2. Search for obiscode A.B.C.D.1.F
             *  3. Search for obiscode A.B.C.D.0.F
             *  4. Search for obiscode A.B.1.D.E.F
             *  5. Throw noSuchRegister exception
             *
             **/

            UniversalObject uo;
            try {
                //1. Search exact obiscode
                uo = getMeterConfig().findObject(obisCode);
            } catch (NotInObjectListException e) {
                //2. Search for obiscode A.B.C.D.0.F
                try {
                    ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 1);
                    uo = getMeterConfig().findObject(baseObisCode);
                } catch (NotInObjectListException e_inner) {
                    //3. Search for obiscode A.B.C.D.1.F
                    try {
                        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 0);
                        uo = getMeterConfig().findObject(baseObisCode);
                    } catch (NotInObjectListException e_inner_inner) {
                        //4. Search for obiscode A.B.1.D.E.F
                        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 2, (byte) 1);
                        uo = getMeterConfig().findObject(baseObisCode);
                    }
                }
            }

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

            throw new NoSuchRegisterException();
        } catch (final Exception e) {
            throw new NoSuchRegisterException();
        }
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.StoredValues} object
     *
     * @return the {@link com.energyict.dlms.cosem.StoredValues} object
     */
    public JanzStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new JanzStoredValues(getCosemObjectFactory(), this);
        }
        return storedValues;
    }

    /**
     * Used by the framework
     *
     * @param commChannel communication channel object
     * @param datareadout enable or disable data readout
     * @throws ConnectionException
     *          thrown when a connection exception happens
     */
    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                new JanzC280HHUConnection(commChannel, this.timeOut, this.retries, 300, getInfoTypeEchoCancelling());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, HHUSIGNON_METERID);
    }

    /**
     * Override this method to requesting the load profile integration time
     *
     * @return integration time in seconds
     * @throws com.energyict.mdc.protocol.api.UnsupportedException
     *                             thrown when not supported
     * @throws java.io.IOException Thrown when something goes wrong
     */
    @Override
    public int getProfileInterval() throws IOException {
        Quantity interval = readRegister(ObisCode.fromString("1.0.0.8.4.255")).getQuantity();

        return interval.intValue() * 60;   //The profile interval can be 1,2,3,4,5,6,10,12,15,20,30 or 60 minutes.
    }

    /**
     * Override this method to requesting the nr of load profile channels from the meter. If not overridden, the default implementation uses the ChannelMap object to get the nr of channels. The ChannelMap object is constructed from the ChannelMap custom property containing a comma separated string. The nr of comma separated tokens is the nr of channels.
     *
     * @return nr of load profile channels
     * @throws com.energyict.mdc.protocol.api.UnsupportedException
     *                             thrown when not supported
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public int getNumberOfChannels() throws IOException {
        if (this.numberOfChannels == -1) {
            logger.info("Loading the number of channels, looping over all load diagrams...");
            this.enabledChannelNumbers =  new ArrayList<>();

            /* Loop over all configuration registers. ( there are 12 'Configuration of the load diagram' registers).
             * If the channel is in use, the register value maps the definition/name of a load profile.
             * The register value unit maps the unit of the channel.
             *
             * If the channel is idle, the register value equals 0.
             * If the channel is not supported by the meter, this loop will throw an error.
             *
             * cfr. Max 12 channels supported - a meter can support 6 channels, of which only 2 active.
            */

            for (int i = 1; i < 13; i++) {
                try {
                    ObisCode obisCode = ObisCode.fromString("1.0.99.128." + i + ".255");
                    RegisterValue registerValue = readRegister(obisCode);
                    if (!registerValue.getQuantity().getAmount().equals(new BigDecimal(0))) {
                        this.enabledChannelNumbers.add(new Integer(i));
                    }
                } catch (DataAccessResultException e) {
                } catch (NoSuchRegisterException e) {
                }
            }
            this.numberOfChannels = this.enabledChannelNumbers.size();
        }
        return this.numberOfChannels;
    }

    /**
     * Override this method to request the load profile from the meter from to.
     *
     * @param from          request from
     * @param to            request to
     * @param includeEvents eneble or disable requesting of meterevents
     * @return ProfileData object
     * @throws java.io.IOException Thrown when something goes wrong
     * @throws com.energyict.mdc.protocol.api.UnsupportedException
     *                             Thrown when not supported
     */
    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileDataReader().getProfileData(from, to, includeEvents);
    }

    private ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new ProfileDataReader(this);
        }
        return profileDataReader;
    }

    public ObisCode[] getLoadProfileObisCodes() throws IOException {
        if (loadProfileObisCodes == null) {
            loadProfileObisCodes = new ObisCode[getEnabledChannelNumbers().size()];
            for (int i = 0; i < getEnabledChannelNumbers().size(); i++) {
                loadProfileObisCodes[i] = ObisCode.fromString("1.0.99.1." + getEnabledChannelNumbers().get(i) + ".255");
            }
        }
        return loadProfileObisCodes;
    }

    public List<Integer> getEnabledChannelNumbers() throws IOException {
        if (enabledChannelNumbers == null) {
            getNumberOfChannels();
        }
        return enabledChannelNumbers;
    }

    @Override
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOptionalKeys() {
        return Arrays.asList(
                DlmsProtocolProperties.ADDRESSING_MODE,
                DlmsProtocolProperties.CLIENT_MAC_ADDRESS,
                DlmsProtocolProperties.CONNECTION,
                PROPNAME_SERVER_LOWER_MAC_ADDRESS,
                PROPNAME_SERVER_UPPER_MAC_ADDRESS,
                PROPERTY_FORCEDTOREADCACHE,
                DlmsProtocolProperties.TIMEOUT,
                DlmsProtocolProperties.RETRIES,
                DlmsProtocolProperties.SECURITY_LEVEL);
    }

    /**
     * Disconnect, stop the association session
     * Only disconnectMAC is needed - releaseAssociation is not supported by meter & should not be sent.
     *
     * @throws java.io.IOException when the communication with the meter failed
     */
    @Override
    public void disconnect() throws IOException {
        try {
            if (getDLMSConnection() != null) {
                getDLMSConnection().disconnectMAC();
            }
        } catch (IOException | DLMSConnectionException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     *
     * @param obisCode obiscode of the register to lookup
     * @return RegisterInfo object
     * @throws java.io.IOException thrown when somethiong goes wrong
     */
    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("Contar Janz C280 register " + obisCode.toString());
    }

    /**
     * The name under which the file will be save in the OperatingSystem.
     *
     * @return the expected fileName of the cacheFile.
     */
    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_JanzC280.cache";
    }
}
