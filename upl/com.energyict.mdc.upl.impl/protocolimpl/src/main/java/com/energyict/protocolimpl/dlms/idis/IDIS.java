package com.energyict.protocolimpl.dlms.idis;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 5/09/11
 * Time: 9:37
 */
public class IDIS extends AbstractDLMSProtocol implements MessageProtocol, FirmwareUpdateMessaging {

    private static final ObisCode FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    private static final String READ_CACHE_DEFAULT_VALUE = "0";
    private static final String READCACHE_PROPERTY = "ReadCache";
    private static final String LOAD_PROFILE_OBIS_CODE_PROPERTY = "LoadProfileObisCode";
    public static final String OBISCODE_LOAD_PROFILE1 = "1.0.99.1.0.255";   //Quarterly

    private ProfileDataReader profileDataReader = null;
    private IDISMessageHandler messageHandler = null;
    private boolean readCache = false;      //Property indicating to read the cache out (useful because there's no config change state)
    private ObisCode loadProfileObisCode = ObisCode.fromString(OBISCODE_LOAD_PROFILE1);
    private IDISStoredValues storedValues = null;

    private ProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new ProfileDataReader(this);
        }
        return profileDataReader;
    }

    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    protected IDISMessageHandler getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new IDISMessageHandler(this);
        }
        return messageHandler;
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
    protected void checkCacheObjects() throws IOException {
        try {

            if (dlmsCache == null) {
                dlmsCache = new DLMSCache();
            }
            if (dlmsCache.getObjectList() == null || isReadCache()) {
                logger.info("Reading out the cache");
                requestObjectList();
                dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
            }

            if (dlmsCache.getObjectList() != null) {
                dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
            }

        } catch (IOException e) {
            IOException exception = new IOException("connect() error, " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return getProfileDataReader().getProfileData(from, to, includeEvents);
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
            getLogger().warning("Using default time zone.");
        }
        return timeZone;
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        readCache = Integer.parseInt(properties.getProperty(READCACHE_PROPERTY, READ_CACHE_DEFAULT_VALUE).trim()) == 1;
        loadProfileObisCode = ObisCode.fromString(properties.getProperty(LOAD_PROFILE_OBIS_CODE_PROPERTY, OBISCODE_LOAD_PROFILE1).trim());
    }

    private boolean isReadCache() {
        return readCache;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = getStoredValues().getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

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
        } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
            final Data register = getCosemObjectFactory().getData(obisCode);
            OctetString octetString = register.getValueAttr().getOctetString();
            if (octetString != null && octetString.stringValue() != null) {
                return new RegisterValue(obisCode, octetString.stringValue());
            }
            throw new NoSuchRegisterException();
        } else {
            throw new NoSuchRegisterException();
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    @Override
    protected List doGetOptionalKeys() {
        List<String> optional = new ArrayList<String>();
        optional.add(DlmsProtocolProperties.CLIENT_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_LOWER_MAC_ADDRESS);
        optional.add(PROPNAME_SERVER_UPPER_MAC_ADDRESS);
        optional.add(DlmsProtocolProperties.CONNECTION);
        optional.add(DlmsProtocolProperties.TIMEOUT);
        optional.add(DlmsProtocolProperties.ADDRESSING_MODE);
        optional.add(DlmsProtocolProperties.RETRIES);
        optional.add(READCACHE_PROPERTY);
        optional.add(LOAD_PROFILE_OBIS_CODE_PROPERTY);
        return optional;
    }

    @Override
    public List getRequiredKeys() {
        List<String> required = new ArrayList<String>();
        required.add(MeterProtocol.SERIALNUMBER);
        return required;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date$";
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        Data data = getCosemObjectFactory().getData(FIRMWARE_VERSION);
        return data.getString();
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode()).getCapturePeriod();
    }

    public int getReference() {
        return ProtocolLink.LN_REFERENCE;
    }

    public IDISStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new IDISStoredValues(getCosemObjectFactory(), this);
        }
        return storedValues;
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getCosemObjectFactory().getProfileGeneric(getLoadProfileObisCode()).getNumberOfProfileChannels();
    }

    public void applyMessages(List messageEntries) throws IOException {
        getMessageHandler().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessageHandler().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageHandler().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessageHandler().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessageHandler().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessageHandler().writeValue(value);
    }

    public int getGasSlotId() {
        return 0;     //E-meter has no gas slot id
    }

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        FirmwareUpdateMessagingConfig config = new FirmwareUpdateMessagingConfig();
        config.setSupportsUrls(false);
        config.setSupportsUserFileReferences(false);
        config.setSupportsUserFiles(true);
        return config;
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return new FirmwareUpdateMessageBuilder();
    }

    @Override
    public void validateSerialNumber() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}