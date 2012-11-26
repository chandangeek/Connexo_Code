package com.elster.protocolimpl.dsfg;

import com.elster.protocolimpl.dsfg.connection.DsfgConnection;
import com.elster.protocolimpl.dsfg.objects.AbstractObject;
import com.elster.protocolimpl.dsfg.profile.ArchiveRecordConfig;
import com.elster.protocolimpl.dsfg.profile.DsfgProfile;
import com.elster.protocolimpl.dsfg.register.DsfgRegisterReader;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * ProtocolImplementation for DSfG devices. <br>
 * <br>
 * <p/>
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

    /**
     * initialization -> create connection class
     */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timezone, Logger logger) throws IOException {
        connection = new DsfgConnection(inputStream, outputStream);
        this.timeZone = timezone;
        this.logger = logger;
    }

    public String getProtocolVersion() {
        String rev = "$Revision: 99 $" + " - "
                + "$Date: 2010-07-22 09:25:00 +0200 (do, 22 jul 2010) $";
        return "Revision "
                + rev.substring(rev.indexOf("$Revision: ")
                + "$Revision: ".length(), rev.indexOf("$ -"))
                + "at "
                + rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(),
                rev.indexOf("$Date: ") + "$Date: ".length() + 19);
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
     * /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    @SuppressWarnings("unchecked")
    public List getRequiredKeys() {
        List result = new ArrayList();
        result.add("RegistrationInstance");
        result.add("ArchiveInstance");
        result.add("ChannelMap");
        return result;
    }

    /**
     * List of optional keys
     *
     * @return a list of strings
     */
    @SuppressWarnings("unchecked")
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");

        List result2 = doGetOptionalKeys();
        if (result2 != null) {
            result.addAll(result2);
        }
        return result;
    }

    /**
     * enable derived class to add more keys
     *
     * @return a list of keys (Strings)
     */
    @SuppressWarnings("unchecked")
    protected List doGetOptionalKeys() {
        return null;
    }

    /**
     * set the protocol specific properties
     *
     * @param properties - properties to use
     */
    public void setProperties(Properties properties)
            throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }

    public void connect() throws IOException {
        connection.connect();
        connection.signon(strPassword);

        // verify device type
        AbstractObject meterTypeObj = getObjectFactory().getMeterTypeObject();
        String meterType = meterTypeObj.getValue();
        getLogger().info("-- Type of device: " + meterType);

        // DataBlock db = new DataBlock(getRegistrationInstance(),
        // 'A', 'J', 'V', new String[] {"caaa","czzz"});
        // @SuppressWarnings("unused")
        // DataBlock in = getDsfgConnection().request(db);
    }

    public void disconnect() throws IOException {
    }

    public String getFirmwareVersion() throws IOException {
        return getObjectFactory().getSoftwareVersionObject().getValue();
    }

    public int getNumberOfChannels() throws IOException {
        return getProfileObject().getNumberOfChannels();
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

        getLogger().info("getProfileData(" + from + "," + to + ")");

        ProfileData profileData = new ProfileData();

        profileData.setChannelInfos(getProfileObject().buildChannelInfos());

        profileData.setIntervalDatas(getProfileObject().getIntervalData(from,
                to));

        return profileData;
    }

    public int getProfileInterval() throws IOException {
        /* interval time of archive can't be easily read out as a value */
        return 3600;
    }

    public Date getTime() throws IOException {
        return getObjectFactory().getClockObject().getDateTime();
    }

    public void initializeDevice() throws IOException {
    }

    public void release() throws IOException {
    }

    public void setTime() throws IOException {
        /* It is not possible to set clock ! */
        throw new IOException("Clock not setable in dsfg devices!");
    }

    // *******************************************************************************************
    // * C l a s s i m p l e m e n t a t i o n c o d e
    // *******************************************************************************************/

    /**
     * Validate certain protocol specific properties
     *
     * @param properties - The properties fetched from the Device
     * @throws MissingPropertyException - in case of an error
     * @throws InvalidPropertyException - in case of an error
     */
    @SuppressWarnings({"unchecked"})
    private void validateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        try {
            for (Object o : getRequiredKeys()) {
                String key = (String) o;
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            protocolRetriesProperty = Integer.parseInt(properties.getProperty(
                    "Retries", "5").trim());

            /* DSfG specific properties */
            registrationInstance = properties.getProperty(
                    "RegistrationInstance", "0").toUpperCase().substring(0, 1);
            if (!"ABCDEFGHIJKLMNOPQRSTUVWXYZ[^".contains(registrationInstance)) {
                throw new InvalidPropertyException(
                        " validateProperties, RegistrationInstance ("
                                + registrationInstance
                                + ") out of Range (A-Z).");
            }

            archiveInstance = properties.getProperty("ArchiveInstance", "0")
                    .toLowerCase().substring(0, 1);
            if (!"abcdefghijklmnopqrstuvwxyz".contains(archiveInstance)) {
                throw new InvalidPropertyException(
                        " validateProperties, ArchiveInstance ("
                                + archiveInstance + ") out of Range (a-y).");
            }

            try {
                channelMap = properties.getProperty("ChannelMap", "");
                archiveStructure = new ArchiveRecordConfig(archiveInstance,
                        channelMap);
            } catch (Exception e) {
                throw new InvalidPropertyException(
                        " validateProperties, ChannelMap is not valid ("
                                + channelMap + ")");
            }

            doValidateProperties(properties);
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(
                    " validateProperties, NumberFormatException, "
                            + e.getMessage());
        }
    }

    /**
     * Getter for the ObjectFactory
     *
     * @return the current ObjectFactory
     */
    protected DsfgObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new DsfgObjectFactory(this);
        }
        return this.objectFactory;
    }

    /**
     * Getter for dsfg profile object
     *
     * @return DsfgProfile object
     */
    protected DsfgProfile getProfileObject() {
        if (this.profile == null) {
            this.profile = new DsfgProfile(this, archiveStructure);
        }
        return this.profile;
    }

    public void doValidateProperties(Properties properties) {
    }


    public String getRegister(String arg0) throws IOException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException(
                "Dsfg devices have no register to read out!");
    }

    public void setRegister(String arg0, String arg1) throws IOException {
        /* dsfg register instances have no register values ! */
        throw new NoSuchRegisterException(
                "Register setting currently not supported!");

    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
        return getRegisterReader().getRegisterValue(obisCode, new Date());
    }

    private DsfgRegisterReader getRegisterReader() {
        if (dsfgRegisterReader == null) {
            dsfgRegisterReader = new DsfgRegisterReader(this);
        }
        return this.dsfgRegisterReader;
    }


    // *******************************************************************************************
    // *
    // * Interface ProtocolLink
    // *
    // *******************************************************************************************/
    public byte[] getDataReadout() {
        return null;
    }

    public DsfgConnection getDsfgConnection() {
        return connection;
    }

    public Logger getLogger() {
        return logger;
    }

    public int getNrOfRetries() {
        return protocolRetriesProperty;
    }

    public String getPassword() {
        return strPassword;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return false;
    }

    public boolean isRequestHeader() {
        return false;
    }

    public String getArchiveInstance() {
        return this.archiveInstance;
    }

    public String getRegistrationInstance() {
        return this.registrationInstance;
    }

    // *******************************************************************************************
    // *
    // * not yet used methods
    // *
    // *******************************************************************************************/
    public Object fetchCache(int arg0) throws SQLException, BusinessException {
        return null;
    }

    public Object getCache() {
        return null;
    }

    public void updateCache(int arg0, Object arg1) throws SQLException,
            BusinessException {
    }

    public void setCache(Object arg0) {
    }

    // *******************************************************************************************
    // *
    // * deprecicated methods
    // *
    // *******************************************************************************************/
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

}
