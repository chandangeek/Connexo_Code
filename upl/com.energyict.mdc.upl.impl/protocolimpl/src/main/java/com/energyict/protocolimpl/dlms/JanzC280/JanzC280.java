package com.energyict.protocolimpl.dlms.JanzC280;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/11/11
 * Time: 17:07
 */
public class JanzC280 extends AbstractDLMSProtocol implements CacheMechanism {

    private static final ObisCode OBISCODE_ACTIVE_FIRMWARE = ObisCode.fromString("0.0.128.0.1.255");

    /**
     * Fixed static string for the forcedToReadCache property
     */
    private static final String PROPERTY_FORCEDTOREADCACHE = "ForcedToReadCache";

    /**
     * Property to indicate whether the cache (objectlist) <b>MUST</b> be read out
     */
    private boolean forcedToReadCache;
    private ProfileDataReader profileDataReader = null;

    /** The captured objects helper. */
	private CapturedObjectsHelper capturedObjectsHelper;

    /** Array containing all load profile OBIS codes. */
	private ObisCode[] loadProfileObisCodes;

    private JanzStoredValues storedValues;

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        this.forcedToReadCache = !properties.getProperty(PROPERTY_FORCEDTOREADCACHE, "0").equalsIgnoreCase("0");
        this.maxRecPduSize = Integer.parseInt(properties.getProperty(DlmsProtocolProperties.MAX_REC_PDU_SIZE, Integer.toString(MAX_PDU_SIZE)));   // MAX_PDU_SIZE = 200 (default value)
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
        Date dateTime = getCosemObjectFactory().getClock().getDateTime();
        return dateTime;
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
     * Override this method to control the protocolversion This method is informational only.
     *
     * @return String with protocol version
     */
    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

    /**
     * Override this method when requesting the meter firmware version is needed. This method is informational only.
     *
     * @return String with firmware version. This can also contain other important info of the meter.
     * @throws java.io.IOException thrown when something goes wrong
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown when that method is not supported
     */
    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        if (firmwareVersion == null) {
            Data data = getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE);
            firmwareVersion = AXDRDecoder.decode(data.getData()).getVisibleString().getStr().trim();
        }
        return firmwareVersion;
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link com.energyict.protocol.MeterProtocol} should be used.
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
                logger.info("Requesting historic register "+obisCode.toString());
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
            } catch (NoSuchRegisterException e){
                //2. Search for obiscode A.B.C.D.0.F
                try {
                    ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 1);
                    uo = getMeterConfig().findObject(baseObisCode);
                } catch (NoSuchRegisterException e_inner) {
                    //3. Search for obiscode A.B.C.D.1.F
                    try {
                        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 4, (byte) 0);
                        uo = getMeterConfig().findObject(baseObisCode);
                    } catch (NoSuchRegisterException e_inner_inner) {
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
     * @throws com.energyict.dialer.connection.ConnectionException
     *          thrown when a connection exception happens
     */
    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                (HHUSignOn) new JanzC280HHUConnection(commChannel, this.timeOut, this.retries, 300, getInfoTypeEchoCancelling());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.deviceId);
    }

    /**
     * Override this method to requesting the load profile integration time
     *
     * @return integration time in seconds
     * @throws com.energyict.protocol.UnsupportedException
     *                             thrown when not supported
     * @throws java.io.IOException Thrown when something goes wrong
     */
    @Override
    public int getProfileInterval() throws UnsupportedException, IOException {
        Quantity interval = readRegister(ObisCode.fromString("1.0.0.8.4.255")).getQuantity();

        return interval.intValue()*60;   //The profile interval can be 1,2,3,4,5,6,10,12,15,20,30 or 60 minutes.
    }

    /**
     * Override this method to requesting the nr of load profile channels from the meter. If not overridden, the default implementation uses the ChannelMap object to get the nr of channels. The ChannelMap object is constructed from the ChannelMap custom property containing a comma separated string. The nr of comma separated tokens is the nr of channels.
     *
     * @return nr of load profile channels
     * @throws com.energyict.protocol.UnsupportedException
     *                             thrown when not supported
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (this.numberOfChannels == -1) {
            logger.info("Loading the number of channels, looping over all load diagrams...");

            /* Loop over all configuration registers. ( there are 12 'Configuration of the load diagram' registers).
             * If the channel is in use, the register value maps the definition/name of a load profile.
             * The register value unit maps the unit of the channel.
             *
             * If the channel is idle, the register value equals 0.
             * If the channel is not supported by the meter, this loop will throw an error.
             *
             * cfr. Max 12 channels supported - a meter can support 6 channels, of which only 2 active.
            */

            for (int i = 1; i < 7; i++) {
                try {
                ObisCode obisCode = ObisCode.fromString("1.0.99.128." + i + ".255");
                    RegisterValue registerValue = readRegister(obisCode);
                    if (registerValue.getQuantity().getAmount().equals(new BigDecimal(0))) {
                    this.numberOfChannels = i - 1;
                    break;
                }
                } catch (DataAccessResultException e) {
                    this.numberOfChannels = i - 1;
                    break;
                }
            }
            if (this.numberOfChannels == -1) {
                this.numberOfChannels = 0;
            }
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
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown when not supported
     */
    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
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
            loadProfileObisCodes = new ObisCode[getNumberOfChannels()];
            for (int i = 0; i < getNumberOfChannels(); i++) {
                loadProfileObisCodes[i] = ObisCode.fromString("1.0.99.1." + (i + 1) + ".255");
            }
        }
        return loadProfileObisCodes;
    }

    @Override
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    @Override
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(DlmsProtocolProperties.ADDRESSING_MODE);
        result.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        result.add(DlmsProtocolProperties.CONNECTION);
        result.add(DlmsProtocolProperties.INFORMATION_FIELD_SIZE);
        result.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS);
        result.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS);
        result.add(PROPERTY_FORCEDTOREADCACHE);
        result.add(DlmsProtocolProperties.MAX_REC_PDU_SIZE);

        result.add(DlmsProtocolProperties.TIMEOUT);
        result.add(DlmsProtocolProperties.RETRIES);
        result.add(DlmsProtocolProperties.SECURITY_LEVEL);
        return result;
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
        } catch (IOException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        } catch (DLMSConnectionException e) {
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
        return new RegisterInfo("Contar Janz C280 register "+obisCode.toString());
    }

    /**
     * The name under which the file will be save in the OperatingSystem.
     *
     * @return the expected fileName of the cacheFile.
     */
    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId  + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_JanzC280.cache";
    }
}
