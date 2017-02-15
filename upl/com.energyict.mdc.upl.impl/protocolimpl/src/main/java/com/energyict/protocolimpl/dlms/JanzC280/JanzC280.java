package com.energyict.protocolimpl.dlms.JanzC280;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.IncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/11/11
 * Time: 17:07
 */
public class JanzC280 extends AbstractDLMSProtocol implements CacheMechanism, SerialNumberSupport {

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
     * The captured objects helper.
     */
    private CapturedObjectsHelper capturedObjectsHelper;

    /**
     * Array containing all load profile OBIS codes.
     */
    private ObisCode[] loadProfileObisCodes;

    private JanzStoredValues storedValues;

    public JanzC280(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
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
            IOException exception = new IOException("connect() error, " + e.getMessage(), e);
            throw exception;
        }
    }

    @Override
    public Date getTime() throws IOException {
        return getCosemObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        final Calendar newTimeToSet = Calendar.getInstance(getTimeZone());
        getCosemObjectFactory().getClock().setTimeAttr(new DateTime(newTimeToSet));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            Data data = getCosemObjectFactory().getData(OBISCODE_ACTIVE_FIRMWARE);
            firmwareVersion = AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
        }
        return firmwareVersion;
    }

    @Override
    public boolean isRequestTimeZone() {
        return false;
    }

    @Override
    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

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

    @Override
    public JanzStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new JanzStoredValues(getCosemObjectFactory(), this);
        }
        return storedValues;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = new JanzC280HHUConnection(commChannel, this.timeOut, this.retries, 300, getInfoTypeEchoCancelling());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, HHUSIGNON_METERID);
    }

    @Override
    public int getProfileInterval() throws IOException {
        Quantity interval = readRegister(ObisCode.fromString("1.0.0.8.4.255")).getQuantity();
        return interval.intValue() * 60;   //The profile interval can be 1,2,3,4,5,6,10,12,15,20,30 or 60 minutes.
    }

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
                } catch (DataAccessResultException | NoSuchRegisterException e) {
                }
            }
            this.numberOfChannels = this.enabledChannelNumbers.size();
        }
        return this.numberOfChannels;
    }

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

    ObisCode[] getLoadProfileObisCodes() throws IOException {
        if (loadProfileObisCodes == null) {
            loadProfileObisCodes = new ObisCode[getEnabledChannelNumbers().size()];
            for (int i = 0; i < getEnabledChannelNumbers().size(); i++) {
                loadProfileObisCodes[i] = ObisCode.fromString("1.0.99.1." + getEnabledChannelNumbers().get(i) + ".255");
            }
        }
        return loadProfileObisCodes;
    }

    List<Integer> getEnabledChannelNumbers() throws IOException {
        if (enabledChannelNumbers == null) {
            getNumberOfChannels();
        }
        return enabledChannelNumbers;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.ADDRESSING_MODE, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, false));
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS, false));
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION, false));
        propertySpecs.add(this.stringSpec(PROPNAME_SERVER_LOWER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS, false));
        propertySpecs.add(this.stringSpec(PROPNAME_SERVER_UPPER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS, false));
        propertySpecs.add(this.stringSpec(PROPERTY_FORCEDTOREADCACHE, PropertyTranslationKeys.DLMS_FORCE_TO_READ_CACHE, false));
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.PK_TIMEOUT, PropertyTranslationKeys.DLMS_TIMEOUT, false));
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.PK_RETRIES, PropertyTranslationKeys.DLMS_RETRIES, false));
        propertySpecs.add(this.stringSpec(DlmsProtocolProperties.SECURITY_LEVEL, PropertyTranslationKeys.DLMS_SECURITYLEVEL, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        try {
            this.forcedToReadCache = !properties.getTypedProperty(PROPERTY_FORCEDTOREADCACHE, "0").equalsIgnoreCase(Integer.toString(DEFAULT_FORCED_TO_READ_CACHE));
            this.maxRecPduSize = Integer.parseInt(properties.getTypedProperty(DlmsProtocolProperties.MAX_REC_PDU_SIZE, Integer.toString(DEFAULT_MAX_PDU_SIZE)));
            this.serverLowerMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, Integer.toString(DEFAULT_SERVER_LOWER_MAC_ADDRESS)));
            this.serverUpperMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, Integer.toString(DEFAULT_SERVER_UPPER_MAC_ADDRESS)));
            this.clientMacAddress = Integer.parseInt(properties.getTypedProperty(PROPNAME_CLIENT_MAC_ADDRESS, Integer.toString(DEFAULT_CLIENT_MAC_ADDRESS)));
            this.informationFieldSize = Integer.parseInt(properties.getTypedProperty(PROPNAME_INFORMATION_FIELD_SIZE, Integer.toString(DEFAULT_INFORMATION_FIELD_SIZE)));
            this.connectionMode = Integer.parseInt(properties.getTypedProperty(PROPNAME_CONNECTION, Integer.toString(DEFAULT_CONNECTION_MODE)));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public String getSerialNumber() {
        Data data;
        try {
            data = getCosemObjectFactory().getData(OBISCODE_SERIAL_NUMBER);
            return AXDRDecoder.decode(data.getRawValueAttr()).getVisibleString().getStr().trim();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, retries+1);
        }
    }

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

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("Contar Janz C280 register " + obisCode.toString());
    }

    @Override
    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_JanzC280.cache";
    }

}