package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.common.DLMSProtocol;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.EventLogs;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.messages.*;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile.ApolloProfileBuilder;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageTag;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 * TODO The packagename of this meter can still be changed.
 * <p>
 * Straightforward implementation of the Apollo meter for the Spanish market.
 * </p>
 * <p>
 * Copyrights EnergyICT<br/>
 * Date: 23-nov-2010<br/>
 * Time: 10:26:05<br/>
 * </p>
 */
public class ApolloMeter extends DLMSProtocol {

    /**
     * The used {@link com.energyict.genericprotocolimpl.common.CommonObisCodeProvider}
     */
    private ObisCodeProvider obisCodeProvider = new ObisCodeProvider();

    /**
     * The used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.RegisterReader}
     */
    private RegisterReader registerReader;

    /**
     * The used {@link com.energyict.genericprotocolimpl.common.StoreObject}
     */
    private StoreObject storeObject;

    /**
     * The used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloObjectFactory}
     */
    private ApolloObjectFactory apolloObjectFactory;

    /**
     * Property to indicate whether the cache (objectlist) <b>MUST</b> be read out
     */
    private boolean forcedToReadCache;

    /**
     * The password of the Rtu
     */
    private String password;

    /**
     * The serialNumber of the Rtu
     */
    private String serialNumber;

    /**
     * Fixed static string for the {@link #forcedToReadCache} property
     */
    private static final String PROP_FORCEDTOREADCACHE = "ForcedToReadCache";
    /**
     * Fixed static string for the {@link #password} property
     */
    private static final String PROPERTY_PASSWORD = "Password";

    /**
     * The used ApolloMessaging object
     */
    private ApolloMessaging apolloMessaging;

    /**
     * The used ActivityCalendarController
     */
    private ApolloActivityCalendarController activityCalendarController;

    /**
     * Handle the protocol tasks
     *
     * @throws com.energyict.cbo.BusinessException
     *
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    @Override
    protected void doExecute() throws BusinessException, SQLException, IOException {

        //TODO to complete

        try {
            // Set clock or Force clock... if necessary
            if (getCommunicationProfile().getForceClock()) {
                doForceClock();
            } else {
                verifyAndWriteClock();
            }

            if (getCommunicationProfile().getReadDemandValues()) {
                getLogger().log(Level.INFO, "Getting Default ProfileData[" + this.obisCodeProvider.getDefaultLoadProfileObisCode() + "] for meter with serialnumber: " + this.serialNumber);
                ProfileData profile = getDefaultProfileData();
                storeObject.add(profile, getMeter());
            }

            if (getCommunicationProfile().getReadMeterEvents()) {
                getLogger().log(Level.INFO, "Getting Events for meter with serialnumber: " + this.serialNumber);
                ProfileData eProfile = getMeterEvents();
                storeObject.add(eProfile, getMeter());
            }

            if (getCommunicationProfile().getReadMeterReadings()) {
                getLogger().log(Level.INFO, "Getting Daily ProfileData[" + this.obisCodeProvider.getDailyLoadProfileObisCode() + "] for meter with serialnumber: " + this.serialNumber);
                ProfileData profile = getDailyProfileData();
                storeObject.add(profile, getMeter());

                getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + this.serialNumber);
                Map<RtuRegister, RegisterValue> registerMap = getRegisterReader().readRegisters();
                storeObject.addAll(registerMap);
            }

            if (getCommunicationProfile().getSendRtuMessage()) {
                sendMeterMessages();
            }
        } finally {
            if (storeObject != null) {
                Environment.getDefault().execute(storeObject);
            }
        }

    }

    /**
     * Get the default loadProfile
     *
     * @return the fetched loadProfile
     * @throws IOException if an error occurred during the dataFetching
     */
    private ProfileData getDefaultProfileData() throws IOException {
        ProfileGeneric pg = getApolloObjectFactory().getDefaultProfile();
        return getProfileData(pg);
    }

    /**
     * Get the daily loadProfile
     *
     * @return the fetched loadProfile
     * @throws IOException if an error occurred during the dataFetching
     */
    private ProfileData getDailyProfileData() throws IOException {
        ProfileGeneric pg = getApolloObjectFactory().getGenericProfileObject(this.obisCodeProvider.getDailyLoadProfileObisCode());
        return getProfileData(pg);
    }

    /**
     * Get the loadProfile for the given <CODE>ProfileGeneric</CODE>
     *
     * @param profileGeneric the profileGeneric to read
     * @return the requested <CODE>ProfileData</CODE>
     * @throws IOException if an error occurred during the dataFetching
     */
    private ProfileData getProfileData(ProfileGeneric profileGeneric) throws IOException {
        ApolloProfileBuilder apb = new ApolloProfileBuilder(this, profileGeneric);

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(apb.getChannelInfos());
        if(pd.getChannelInfos().size() == 0){
            getLogger().log(Level.INFO, "No channels are found which correspond with he default Profile");
            return pd;
        }
        Calendar toCalendar = Calendar.getInstance(getTimeZone());
        Calendar fromCalendar = Calendar.getInstance(getTimeZone());
        Date lastProfileDate = apb.getLastProfileDate();
        fromCalendar.setTime(lastProfileDate);
        getLogger().log(Level.INFO, "Getting intervalData from " + fromCalendar.getTime());
        pd.setIntervalDatas(apb.getIntervalList(fromCalendar, toCalendar));
        getLogger().log(Level.FINEST, "Below are the channelInfo");
        for(ChannelInfo ci : pd.getChannelInfos()){
            getLogger().log(Level.FINEST, ci.toString());
        }
        return pd;
    }

    /**
     * Get the events from the Device
     *
     * @return a ProfileData object with only the events written
     * @throws IOException
     */
    private ProfileData getMeterEvents() throws IOException {
        EventLogs logs = new EventLogs(this);
        ProfileData eProfile = new ProfileData();
        Calendar fromCalendar = Calendar.getInstance(getMeter().getTimeZone());
        Date lastLogReading = getMeter().getLastLogbook();
        if (lastLogReading == null) {
            lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getMeter());
        }
        fromCalendar.setTime(lastLogReading);
        eProfile.getMeterEvents().addAll(logs.getEventLog(fromCalendar));
        return eProfile;
    }

    /**
     * Fetch the meter's time
     *
     * @return
     * @throws IOException
     */
    @Override
    public Date getTime() throws IOException {
        try {
            return getApolloObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the Clock object." + e);
        }
    }

    /**
     * Force the clock of the device to the system time
     *
     * @throws IOException
     */
    public void doForceClock() throws IOException {
        Date meterTime = getTime();
        Calendar currentCalendar = Calendar.getInstance(getTimeZone());
        currentCalendar.set(Calendar.MILLISECOND, 0);
        Date currentTime = currentCalendar.getTime();
        setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
        getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);
        setClock(currentTime);
    }

    /**
     * Set the meter's clock to a certain time
     *
     * @param currentTime - the given time to set
     * @throws IOException if forcing the clock failed
     */
    @Override
    public void setClock(Date currentTime) throws IOException {
        try {
            getApolloObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(currentTime));
        } catch (IOException e) {
            log(Level.FINEST, e.getMessage());
            throw new IOException("Could not set the Clock object." + e);
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced.<br>
     * <br>
     * <p/>
     * The AM100 module does not have the checkConfigParameter in his objectlist, thus to prevent reading the
     * objectlist each time we read the device, we will go for the following approach:<br>
     * 1/ check if the cache exists, if it does exist, go to step 2, if not go to step 3    <br>
     * 2/ is the custom property {@link #forcedToReadCache} enabled? If yes then go to step 3, else exit    <br>
     * 3/ readout the objectlist    <br>
     *
     * @throws java.io.IOException
     */
    @Override
    protected void checkCacheObjects() throws IOException {
//        if ((((DLMSCache) getCache()).getObjectList() == null) || forcedToReadCache) {
//            log(Level.INFO, forcedToReadCache ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
//            requestConfiguration();
//            ((DLMSCache) getCache()).saveObjectList(getMeterConfig().getInstantiatedObjectList());
//        } else {
//            log(Level.INFO, "Cache exist, will not be read!");
//        }
    }

    /**
     * Request Association buffer list out of the meter.
     *
     * @throws IOException if something fails during the request or the parsing of the buffer
     */
    @Override
    protected void requestConfiguration() throws IOException {
//        try {
//            // We retrieve the AssociationLNObject with clientAddress 0, this should return all objects ...
//            getMeterConfig().setInstantiatedObjectList(getApolloObjectFactory().getAssociationLnObject(0).getBuffer());
//        } catch (IOException e) {
//            log(Level.FINEST, e.getMessage());
//            throw new IOException("Requesting configuration failed." + e);
//        }
    }

    /**
     * Return the SystemTitle to be used in the DLMS association request.
     * For the AM100-R we use the SerialNumber of the device
     *
     * @return the SystemTitle
     */
    @Override
    protected byte[] getSystemIdentifier() {
//        return this.serialNumber.getBytes();
//        return "EIT12345".getBytes();
        return null;
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    @Override
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.XdlmsAse} which is used for the DLMS association.
     *
     * @return the xdlmsAse, if null is returned then the default values will be used
     */
    @Override
    protected XdlmsAse configureXdlmsAse() {
        return new XdlmsAse(null, true, -1, 6, configureConformanceBlock(), 1200);
    }

    /**
     * Configure the {@link com.energyict.dlms.InvokeIdAndPriority} bitString which is used during DLMS communication.
     *
     * @return the invokeIdAndPriority bitString, if null is returned then the default value({@link com.energyict.genericprotocolimpl.common.DLMSProtocol#buildDefaultInvokeIdAndPriority()}) will be used
     */
    @Override
    protected InvokeIdAndPriority configureInvokeIdAndPriority() {
        try {
            return buildDefaultInvokeIdAndPriority();
        } catch (DLMSConnectionException e) {
            log(Level.FINEST, e.getMessage());
            // if we can't get it, then return null so the default should be used
            return null;
        }
    }

    /**
     * Define a list of REQUIRED properties, other then the ones configure in {@link #getRequiredKeys()}.
     * These properties can be used specifically for the protocol
     *
     * @return the properties list
     */
    @Override
    protected List<String> doGetRequiredKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Define a list of OPTIONAL properties, other then the ones configured in {@linkplain #getOptionalKeys()}.
     * These properties can be used specifically for the protocol
     *
     * @return the properties list
     */
    @Override
    protected List<String> doGetOptionalKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Configuration of the protocol specific Required and Optional properties
     */
    @Override
    protected void doValidateProperties() {

        if ((getMeter() != null) && (!getMeter().getSerialNumber().equals(""))) {
            this.serialNumber = getMeter().getSerialNumber();
        } else {
            this.serialNumber = "";
        }

        this.forcedToReadCache = !getProperties().getProperty(PROP_FORCEDTOREADCACHE, "0").equalsIgnoreCase("0");
        if ((getMeter() != null) && (!getMeter().getPassword().equals(""))) {
            this.password = getMeter().getPassword();
        } else if (getMeter() == null) {
            this.password = getProperties().getProperty(PROPERTY_PASSWORD, "");
        }
    }

    /**
     * Build the logic to provide the desired {@link com.energyict.dlms.aso.SecurityProvider}.
     * If no securityProvider should be used, then return null and the default({@link com.energyict.dlms.aso.LocalSecurityProvider}) will be used.
     */
    @Override
    protected SecurityProvider getSecurityProvider() {
        if ((getMeter() != null) && (password != null)) {
            getProperties().put(MeterProtocol.PASSWORD, password);
        }
        NTASecurityProvider lsp = new NTASecurityProvider(getProperties());
        return lsp;
    }

    /**
     * Implement functionality right AFTER the DLMS association has been established
     *
     * @throws java.io.IOException
     */
    @Override
    protected void doConnect() throws IOException {
        verifyMeterSerialNumber();
        log(Level.INFO, "FirmwareVersion: " + getFirmWareVersion());
    }

    /**
     * @return the current FirmwareVersion
     */
    private String getFirmWareVersion() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(getApolloObjectFactory().getFirmwareVersion().getString());
        try {
            String acor = getApolloObjectFactory().getFirmwareVersion().getAttrbAbstractDataType(-1).getOctetString().stringValue();
            builder.append(" - ACOR : ").append(acor);
        } catch (IOException e) {
            // absorb
        }
        try {
            String mcor = getApolloObjectFactory().getFirmwareVersion().getAttrbAbstractDataType(-2).getOctetString().stringValue();
            builder.append(" - MCOR : ").append(mcor);
        } catch (IOException e) {
            // absorb
        }
        return builder.toString();

//        return getApolloObjectFactory().getActiveFirmwareIdACOR().getString();
    }

    /**
     * Checks if the serialnumber from the device matches the one configured in EIServer
     *
     * @throws IOException if it doesn't match
     */
    private void verifyMeterSerialNumber() throws IOException {
        String serial = getSerialNumber();
        if (!(this.serialNumber.equalsIgnoreCase("")) && (!this.serialNumber.equals(serial))) {
            throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + serial);
        }
    }

    /**
     * Get the serialNumber from the device
     *
     * @return the serialnumber from the device
     * @throws IOException we couldn't read the serialnumber
     */
    public String getSerialNumber() throws IOException {
        try {
            return getApolloObjectFactory().getSerialNumber().getString();
        } catch (IOException e) {
            log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the serialnumber of the meter." + e);
        }
    }

    /**
     * Implement functionality right BEFORE the DLMS association is released
     */
    @Override
    protected void doDisconnect() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Implement functionality right AFTER the initializeGlobals method
     *
     * @throws java.io.IOException
     * @throws com.energyict.cbo.BusinessException
     *
     * @throws java.sql.SQLException
     */
    @Override
    protected void doInit() throws SQLException, BusinessException, IOException {
        this.storeObject = new StoreObject();
    }

    /**
     * Read the register using your custom define ObisCodeMapper
     *
     * @param obisCode - the obisCode from the register to read
     * @return the read RegisterValue
     * @throws java.io.IOException
     */
    @Override
    protected RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterReader().read(obisCode);  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        return getApolloMessaging().getMessageCategories();
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        String rev = "$Revision$" + " - " + "$Date$";
        String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
                + rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
        return manipulated;
    }

    /**
     * Check if the {@link java.util.TimeZone} is read from the DLMS device, or if the
     * {@link java.util.TimeZone} from the {@link com.energyict.protocol.MeterProtocol} should be used.
     *
     * @return true is the {@link java.util.TimeZone} is read from the device
     */
    public boolean isRequestTimeZone() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the round trip correction.
     *
     * @return the value of the round trip correction
     */
    public int getRoundTripCorrection() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
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
     * Getter for the {@link com.energyict.dlms.cosem.StoredValues} object
     *
     * @return the {@link com.energyict.dlms.cosem.StoredValues} object
     */
    public StoredValues getStoredValues() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the RegisterReader
     *
     * @return the used RegisterReader
     */
    protected RegisterReader getRegisterReader() {
        if (this.registerReader == null) {
            this.registerReader = new RegisterReader(this);
        }
        return this.registerReader;
    }

    /**
     * @return the storeObject
     */
    public StoreObject getStoreObject() {
        return this.storeObject;
    }

    /**
     * Getter for the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloObjectFactory}
     *
     * @return the used {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloObjectFactory}
     */
    public ApolloObjectFactory getApolloObjectFactory() {
        if (this.apolloObjectFactory == null) {
            this.apolloObjectFactory = new ApolloObjectFactory(this);
        }
        return this.apolloObjectFactory;
    }

    /**
     * Send all the meterMessages
     *
     * @throws BusinessException
     * @throws SQLException
     */
    private void sendMeterMessages() throws BusinessException, SQLException {
        MessageExecutor me = new MessageExecutor(this);
        for (RtuMessage rm : getMeter().getPendingMessages()) {
            me.doMessage(rm);
        }
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        return getApolloMessaging().writeTag(msgTag);
    }

    /**
     * @return the used <CODE>ApolloMessaging</CODE> object
     */
    public ApolloMessaging getApolloMessaging() {
        if (this.apolloMessaging == null) {
            this.apolloMessaging = new ApolloMessaging(this);
        }
        return this.apolloMessaging;
    }

    public ApolloActivityCalendarController getActivityCalendarController() {
        if (this.activityCalendarController == null) {
            this.activityCalendarController = new ApolloActivityCalendarController(this);
        }
        return this.activityCalendarController;
    }
}
