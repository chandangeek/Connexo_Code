package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.eventhandling.EventLogs;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

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
    private static final String PROPERTY_PASSWORD = "Password";

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
                Date meterTime = getTime();
                Calendar currentCalendar = Calendar.getInstance(getTimeZone());
                currentCalendar.set(Calendar.MILLISECOND, 0);
                Date currentTime = currentCalendar.getTime();
                setTimeDifference(Math.abs(currentTime.getTime() - meterTime.getTime()));
                getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + currentTime);

                //            Data clockSync = new Data(this, new ObjectReference(obisCodeProvider.getClockSynchronization().getLN()));
                //            clockSync.setValueAttr(new AXDRDateTime(currentTime));

                getApolloObjectFactory().getClock().setAXDRDateTimeAttr((new AXDRDateTime(currentTime)));
            } else {
                //TODO check this if the DLMSProtocol doesn't use the cosemObjectFactory
                verifyAndWriteClock();
            }

            if (getCommunicationProfile().getReadDemandValues()) {
                getLogger().log(Level.INFO, "Getting ProfileData for meter with serialnumber: " + this.serialNumber);
                ProfileData profile = getProfileData();
                storeObject.add(profile, getMeter());
//                getLogger().log(Level.INFO, "Currently no LoadProfile Support!");
            }

            if (getCommunicationProfile().getReadMeterEvents()) {
                //TODO complete
                getLogger().log(Level.INFO, "Currently no Event Support!");

                ProfileData eProfile = getMeterEvents();
                storeObject.add(eProfile, getMeter());
            }

            if (getCommunicationProfile().getReadMeterReadings()) {
                getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + this.serialNumber);
                Map<RtuRegister, RegisterValue> registerMap = getRegisterReader().readRegisters();
                storeObject.addAll(registerMap);
            }
        } finally {
            if (storeObject != null) {
                Environment.getDefault().execute(storeObject);
            }
        }

    }

    private ProfileData getProfileData() throws IOException {
        ProfileGeneric pg = getApolloObjectFactory().getDefaultProfile();
        ApolloProfileBuilder apb = new ApolloProfileBuilder(this, pg);

        ProfileData pd = new ProfileData();
        pd.setChannelInfos(apb.getChannelInfos());
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.MONTH, -3);
//        pg.getBuffer(fromCalendar).printDataContainer();
        pg.getBuffer();
        getLogger().info("ProfileCapturePeriod: " + pg.getCapturePeriod());
        getLogger().info("EntriesInUse: " + pg.getEntriesInUse());
        getLogger().info("NumberOfProfileChannels: " + pg.getNumberOfProfileChannels());
        getLogger().info("ProfileEntries: " + pg.getProfileEntries());
        return pd;
    }

    private ProfileData getMeterEvents() throws IOException {
        EventLogs logs = new EventLogs(this);
        ProfileData eProfile = new ProfileData();
        Calendar fromCalendar = Calendar.getInstance(getMeter().getTimeZone());
        Date lastLogReading = getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(getMeter());
		}
        fromCalendar.setTime(lastLogReading);
        eProfile.getMeterEvents().addAll(logs.getEventLog(fromCalendar));
        return eProfile;
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
     * If no securityProvider should be used, then return null and the default({@link com.energyict.genericprotocolimpl.common.LocalSecurityProvider}) will be used.
     */
    @Override
    protected SecurityProvider getSecurityProvider() {
        if ((getMeter() != null) && (password != null)) {
            getProperties().put(MeterProtocol.PASSWORD, password);
        }
        LocalSecurityProvider lsp = new LocalSecurityProvider(getProperties());
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
        return getApolloObjectFactory().getFirmwareVersion().getString();
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the implementation version
     *
     * @return a version string
     */
    public String getVersion() {
        return "$Date$";  //To change body of implemented methods use File | Settings | File Templates.
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

}
