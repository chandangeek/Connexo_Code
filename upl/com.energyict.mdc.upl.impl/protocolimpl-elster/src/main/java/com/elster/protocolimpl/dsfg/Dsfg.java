package com.elster.protocolimpl.dsfg;

import com.elster.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.*;

import com.elster.protocolimpl.dsfg.connection.DsfgConnection;
import com.elster.protocolimpl.dsfg.objects.AbstractObject;
import com.elster.protocolimpl.dsfg.profile.ArchiveRecordConfig;
import com.elster.protocolimpl.dsfg.profile.DsfgProfile;
import com.elster.protocolimpl.dsfg.register.DsfgRegisterReader;
import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.properties.CharPropertySpec;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * ProtocolImplementation for DSfG devices. <br>
 * <br>
 *
 * <b>General Description:</b><br>
 * <br>
 * <br>
 * <b>Data interface:</b><br>
 * <li>Optical interface according to IEC1107 <li>Internal GSM modem <br>
 * <br>
 * <b>Additional information:</b><br>
 *
 * @author gh
 * @since 5-mai-2010
 *
 */
@SuppressWarnings({"unused"})
public class Dsfg extends PluggableMeterProtocol implements RegisterProtocol, ProtocolLink {

    /**
     * time zone of device
     */
    private TimeZone timeZone;
    /**
     * reference to logger
     */
    private Logger logger;
    /**
     * class of dsfg protocol
     */
    private DsfgConnection connection;
    /**
     * profile class
     */
    private DsfgProfile profile = null;
    /* interval of profile in sec */
    private int profileInterval = 3600;

    private DsfgRegisterReader dsfgRegisterReader = null;

    /**
     * password from given properties
     */
    private String strPassword;

    /**
     * compatibility properties..., currently not used
     */
    private int protocolRetriesProperty;
    /**
     * instance letter off registration instance
     */
    private String registrationInstance = "";
    /**
     * instance letter of archive (in registration instance)
     */
    private String archiveInstance = "";
    /**
     * mapping of archive boxes to channels
     */
    private String channelMap = "";
    /**
     * factory for common data objects
     */
    private DsfgObjectFactory objectFactory = null;

    /**
     * archive structure definition
     */
    private ArchiveRecordConfig archiveStructure = null;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    public Dsfg(PropertySpecService propertySpecService, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timezone, Logger logger) throws IOException {
        connection = new DsfgConnection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-10-30 12:00:00 +0100$";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(PASSWORD.getName(), false, PropertyTranslationKeys.DSFG_PASSWORD, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(TIMEOUT.getName(), false, PropertyTranslationKeys.DSFG_TIMEOUT, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(RETRIES.getName(), false, PropertyTranslationKeys.DSFG_RETRIES, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PROFILEINTERVAL.getName(), false, PropertyTranslationKeys.DSFG_PROFILE_INTERVAL, this.propertySpecService::integerSpec).finish(),
                new CharPropertySpec("RegistrationInstance", true, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ[^", this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DSFG_REGISTRATION_INSTANCE).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DSFG_REGISTRATION_INSTANCE_DESCRIPTION).format()),
                new CharPropertySpec("ArchiveInstance", true, "abcdefghijklmnopqrstuvwxyz", this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DSFG_REGISTRATION_INSTANCE).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DSFG_REGISTRATION_INSTANCE_DESCRIPTION).format()),
                UPLPropertySpecFactory.specBuilder("ChannelMap", true, PropertyTranslationKeys.DSFG_CHANNEL_MAP, this.propertySpecService::stringSpec).finish());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            protocolRetriesProperty = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "5").trim());

            /* DSfG specific properties */
            registrationInstance = properties.getTypedProperty("RegistrationInstance", "0").substring(0, 1).toUpperCase();
            archiveInstance = properties.getTypedProperty("ArchiveInstance", "0").substring(0, 1).toLowerCase();

            try {
                channelMap = properties.getTypedProperty("ChannelMap", "");
                archiveStructure = new ArchiveRecordConfig(archiveInstance, channelMap);
            } catch (Exception e) {
                throw new InvalidPropertyException(" validateProperties, ChannelMap is not valid (" + channelMap + ")");
            }

            profileInterval = Integer.parseInt(properties.getTypedProperty("ProfileInterval", "3600"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public void connect() throws IOException {
        connection.connect();
        connection.signon(strPassword);

        // verify device type
        AbstractObject meterTypeObj = getObjectFactory().getMeterTypeObject();
        String meterType = meterTypeObj.getValue();
        getLogger().info("-- Type of device: " + meterType);
    }

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getObjectFactory().getSoftwareVersionObject().getValue();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getProfileObject().getNumberOfChannels();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        getLogger().info("getProfileData(" + from + "," + to + ")");
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(getProfileObject().buildChannelInfos());
        getProfileObject().setInterval(profileInterval);
        profileData.setIntervalDatas(getProfileObject().getIntervalData(from, to));
        return profileData;
    }

    @Override
    public int getProfileInterval() {
        /* interval time of archive can't be easily read out as a value */
		return profileInterval;
    }

    @Override
    public Date getTime() throws IOException {
        return getObjectFactory().getClockObject().getDateTime();
    }

    @Override
    public void initializeDevice() {
    }

    @Override
    public void release() {
    }

    @Override
    public void setTime() throws IOException {
        /* It is not possible to set clock ! */
        throw new IOException("Clock not setable in dsfg devices!");
    }

    protected DsfgObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new DsfgObjectFactory(this);
        }
        return this.objectFactory;
    }

    protected DsfgProfile getProfileObject() {
        if (this.profile == null) {
            this.profile = new DsfgProfile(this, archiveStructure);
        }
        return this.profile;
    }

    @Override
    public String getRegister(String arg0) throws NoSuchRegisterException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException("Dsfg devices have no register to read out!");
    }

    @Override
    public void setRegister(String arg0, String arg1) throws NoSuchRegisterException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException("Register setting currently not supported!");

    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) {
        return new RegisterInfo("");
    }

    @Override
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return getRegisterReader().getRegisterValue(obisCode, new Date());
    }

    private DsfgRegisterReader getRegisterReader() {
        if (dsfgRegisterReader == null) {
            dsfgRegisterReader = new DsfgRegisterReader(this);
        }
        return this.dsfgRegisterReader;
    }

    @Override
    public byte[] getDataReadout() {
        return null;
    }

    @Override
    public DsfgConnection getDsfgConnection() {
        return connection;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public int getNrOfRetries() {
        return protocolRetriesProperty;
    }

    @Override
    public String getPassword() {
        return strPassword;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean isIEC1107Compatible() {
        return false;
    }

    @Override
    public boolean isRequestHeader() {
        return false;
    }

    @Override
    public String getArchiveInstance() {
        return this.archiveInstance;
    }

    @Override
    public String getRegistrationInstance() {
        return this.registrationInstance;
    }

    @Override
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

}