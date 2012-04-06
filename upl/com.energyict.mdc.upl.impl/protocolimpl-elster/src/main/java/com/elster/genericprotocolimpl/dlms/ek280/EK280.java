package com.elster.genericprotocolimpl.dlms.ek280;

import com.elster.genericprotocolimpl.dlms.ek280.deployment.DeploymentDataFetcher;
import com.elster.genericprotocolimpl.dlms.ek280.deployment.DeviceDeployment;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.DeviceDiscover;
import com.elster.genericprotocolimpl.dlms.ek280.discovery.DeviceDiscoverInfo;
import com.elster.genericprotocolimpl.dlms.ek280.executors.CommunicationScheduleExecutor;
import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.ShadowList;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.StreamConnection;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.FirmwareUpdateMessaging;
import com.energyict.protocol.messaging.FirmwareUpdateMessagingConfig;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.RtuDiscoveredEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 6/06/11
 * Time: 14:53
 */
public class EK280 implements GenericProtocol, MessageProtocol, FirmwareUpdateMessaging {

    private static final String REQUEST_INVALIDDATA = "<REQUEST>INVALIDDATA</REQUEST>";
    private static final String REQUEST_OK = "<REQUEST>OK</REQUEST>";

    private Rtu rtu = null;
    private com.elster.protocolimpl.dlms.EK280 dlmsProtocol = null;
    private Link link;
    private Logger logger;
    private StoreObject storeObject = null;
    private long timeDifference = 0;
    private long startTime;
    private EK280Properties properties = null;
    private RtuDiscoveredEvent discoveredEvent = null;

    /**
     * The main execute method, called by the commserver
     *
     * @param cs     The scheduller should be null for an inbound connection
     * @param link   The link to our incoming device (containing the InputStream and the OutputStream)
     * @param logger The protocol logger
     * @throws BusinessException If there was an error different from an communication or protocol error
     * @throws SQLException      If there was an error when we read/write data to the MeteringWarehouse
     * @throws IOException       If there was a communication or protocol error
     */
    public void execute(CommunicationScheduler cs, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        this.link = link;
        this.logger = logger;
        this.startTime = new Date().getTime();

        initMdw();

        getLogger().info("Incoming TCP connection from: [" + getLinkSource() + "]. Starting discovery.");
        doDiscoverAndDeploy();

        if (rtu != null) {
            getLogger().info("Device [" + rtu + "] connected with serial number [" + rtu.getSerialNumber() + "]");

            try {
                initProtocol();
                readDiscoveryData();
                getProtocolVersions();
                verifyClockDifference();
                readBasicDeviceInfo();
                readDevice();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, e.getMessage(), e);
                failAllPendingSchedules(e.getMessage());
            } finally {
                disconnect();
            }

            getLogger().info("Session with device [" + rtu + "] with serial number [" + rtu.getSerialNumber() + "] ended. Storing data.");
            storeData();
            sendDiscoveredEvent();
        } else {
            getLogger().severe("Device is 'null'. Unable to start dlms session.");
        }

        getLogger().info("Done.\n");

    }

    /**
     * In this method we should read all the data that's available in the device and
     * required by the customer code to finish the deployment process successfully.
     * We also fetch the installation date here and use it as last reading for the new rtu.
     */
    private void readDiscoveryData() {
        if (isDiscoverySession()) {
            getLogger().warning("Discovery session: Reading extra discovery data!");
            DeploymentDataFetcher dataFetcher = new DeploymentDataFetcher(this);
            Date installationDate = dataFetcher.readInstallationDate();
            String pdr = dataFetcher.readPdr();
            String phoneNumber = dataFetcher.readPhoneNumber();
            String meterSerial = dataFetcher.readMeterSerial();

            getLogger().warning("Discovery session: Installation date [" + installationDate + "]");
            getLogger().warning("Discovery session: PDR [" + pdr + "]");
            getLogger().warning("Discovery session: Phone number [" + phoneNumber + "]");
            getLogger().warning("Discovery session: Gas meter serial [" + meterSerial + "]");

            RtuShadow shadow = getRtu().getShadow();
            shadow.setSerialNumber(meterSerial);
            shadow.setNodeAddress(pdr);
            shadow.setPhoneNumber(phoneNumber);

            Date lastReading = shadow.getLastReading() == null ? getProperties().getChannelBackLogDate() : shadow.getLastReading();
            if (installationDate != null) {
                if (installationDate.after(lastReading)) {
                    getLogger().warning("Discovery session: Updating last reading on rtu and channels to installation date [" + installationDate + "]");
                    shadow.setLastReading(installationDate);
                    shadow.setLastLogbook(installationDate);
                    ShadowList<ChannelShadow> channelShadows = shadow.getChannelShadows();
                    for (ChannelShadow channelShadow : channelShadows) {
                        channelShadow.setLastReading(installationDate);
                    }
                } else {
                    getLogger().warning("Discovery session: Last reading limited by channel backlog property [" + getProperties().getChannelBackLog() + "].");
                }
            } else {
                getLogger().severe("Discovery session: Last reading of device is not supposed to be 'null'! Using installation date as last reading.");

            }

            // TODO: Implement the other fields that are required by the customer code. Meter caliber, type, digits ...

            try {
                rtu.update(shadow);
            } catch (SQLException e) {
                getLogger().severe("Discovery session: Received SQLException while updating rtu fields! [" + e.getMessage() + "]");
            } catch (BusinessException e) {
                getLogger().severe("Discovery session: Received BusinessException while updating rtu fields! [" + e.getMessage() + "]");
            }

            this.properties = null; // Re-init the properties with the new Rtu values
            
        }
    }

    /**
     * Generate a RtuDiscovered event when there was a new device discovered and deployed.
     * This event can be picked up later on by customer code to trigger the deployment process.
     *
     * @throws BusinessException
     * @throws SQLException
     */
    private void sendDiscoveredEvent() throws BusinessException, SQLException {
        if (isDiscoverySession()) {
            getLogger().warning("Sending new RtuDiscoveredEvent for rtu [" + getRtu() + "]");
            MeteringWarehouse.getCurrent().signalEvent(discoveredEvent);
        }
    }

    private boolean isDiscoverySession() {
        return discoveredEvent != null;
    }

    /**
     * Iterate over all the pending schedules, start them, and fail them with the given message as detail
     *
     * @param message The message that's added as detail in the AmrJournal
     */
    private void failAllPendingSchedules(String message) {
        if ((getRtu() != null) && (!getRtu().getCommunicationSchedulers().isEmpty())) {
            List<CommunicationScheduler> schedulers = getRtu().getCommunicationSchedulers();
            for (CommunicationScheduler cs : schedulers) {
                Date now = new Date();
                if ((cs != null) && (cs.getNextCommunication() != null) && (!cs.getNextCommunication().after(now))) {

                    try {

                        // Let EIServer know, we 're "starting" the schedule
                        cs.startCommunication();
                        cs.startReadingNow();

                        // Build up a AmrJournal session with all the details about the failure
                        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
                        journal.add(new AmrJournalEntry(AmrJournalEntry.DETAIL, message));
                        journal.add(new AmrJournalEntry(now, AmrJournalEntry.CONNECTTIME, getConnectTimeInSeconds()));
                        journal.add(new AmrJournalEntry(now, AmrJournalEntry.PROTOCOL_LOG, "-"));
                        journal.add(new AmrJournalEntry(now, AmrJournalEntry.TIMEDIFF, "" + getTimeDifference()));
                        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_PROTOCOLERROR));

                        // Store the journal, and let EIServer know that the schedule failed
                        cs.journal(journal);
                        cs.logFailure(new Date(), message);

                    } catch (SQLException e) {
                        getLogger().severe("Unable to fail schedule: " + e.getMessage());
                    } catch (BusinessException e) {
                        getLogger().severe("Unable to fail schedule: " + e.getMessage());
                    }

                }
            }
        }
    }

    /**
     * Read the clock and calculate the clock difference.
     * This difference is stored later on in the AmrJournal.
     *
     * @throws IOException If we could not get a valid date
     */
    private void verifyClockDifference() throws IOException {
        Date deviceTime = getDlmsProtocol().getTime();
        Date now = new Date();
        if (deviceTime == null) {
            throw new IOException("Unable to read the device time: getTime() returned 'null'.");
        }

        this.timeDifference = now.getTime() - deviceTime.getTime();
        getLogger().info("Device time: " + deviceTime + ", CommServer time: " + now + ". Time difference = " + (timeDifference / 1000) + " sec");
    }

    /**
     * Fetch all the basic device info from the rtu (fw version, channel count and profile interval)
     *
     * @throws IOException if there was an error during the fetching of the info
     */
    private void readBasicDeviceInfo() throws IOException {
        getLogger().info("Rtu firmware version: " + getDlmsProtocol().getFirmwareVersion());
        getLogger().info("Number of channels: " + getDlmsProtocol().getNumberOfChannels());
        getLogger().info("Profile interval: " + getDlmsProtocol().getProfileInterval() + " seconds");
    }

    /**
     * Read the protocol versions for both the generic and normal dlms protocol
     */
    private void getProtocolVersions() {
        getLogger().info("Generic protocol version: " + getVersion());
        getLogger().info("DLMS protocol version: " + getDlmsProtocol().getProtocolVersion());
    }

    private void doDiscoverAndDeploy() {
        DeviceDiscoverInfo deviceDiscoverInfo = null;
        DeviceDiscover deviceDiscover = new DeviceDiscover(this);
        DeviceDeployment deployment = new DeviceDeployment(this);
        try {
            deviceDiscoverInfo = deviceDiscover.discoverDevice();
            this.rtu = deployment.deployDevice(deviceDiscoverInfo);
            this.properties = null; // Re-init the properties with the new Rtu
            if (deployment.isDeployed()) {
                this.discoveredEvent = new RtuDiscoveredEvent(getRtu());
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }
        if ((deviceDiscoverInfo != null) && (deviceDiscoverInfo.getParameters() != null)) {
            sendResponse(deployment);
        }
    }

    private void sendResponse(DeviceDeployment deployment) {
        String response = deployment.isInvalidData() ? REQUEST_INVALIDDATA : REQUEST_OK;
        try {
            getLink().getOutputStream().write(response.getBytes());
            getLink().getOutputStream().flush();
        } catch (IOException e) {
            getLogger().severe("Unable to acknowledge the discovery REQUEST: " + e.getMessage());
        }
    }

    private void disconnect() {
        try {
            getDlmsProtocol().disconnect();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void storeData() throws BusinessException, SQLException {
        try {
            Environment.getDefault().execute(getStoreObject());
        } catch (BusinessException e) {
            e.printStackTrace();
            getLogger().severe("Unable to store data! [" + e.getMessage() + "]");
            throw e;
        } catch (SQLException e) {
            getLogger().severe("Unable to store data! [" + e.getMessage() + "]");
            throw e;
        }
    }

    private String getLinkSource() {
        StreamConnection sc = getLink().getStreamConnection();
        if ((sc != null) && (sc.getSocket() != null) && sc.getSocket().getRemoteSocketAddress() != null) {
            return sc.getSocket().getRemoteSocketAddress().toString();
        }
        return "-";
    }

    private void initProtocol() throws IOException {
        if (getRtu() != null && getLink() != null && getLogger() != null) {
            getDlmsProtocol().getRequiredKeys();
            getDlmsProtocol().getOptionalKeys();
            getDlmsProtocol().setProperties(getProperties().getProtocolProperties());
            getDlmsProtocol().init(getLink().getInputStream(), getLink().getOutputStream(), getRtu().getDeviceTimeZone(), getLogger());
            getDlmsProtocol().connect();
        } else {
            throw new IOException("Protocol needs a link, logger and rtu!");
        }
    }

    private void initMdw() {
        if (MeteringWarehouse.getCurrent() == null) {
            MeteringWarehouse.createBatchContext();
        }
    }

    private void readDevice() {
        List<CommunicationScheduler> schedulers = getRtu().getCommunicationSchedulers();
        for (CommunicationScheduler scheduler : schedulers) {
            getLogger().info("executing schedule" + scheduler.toString());
            new CommunicationScheduleExecutor(this).execute(scheduler);
        }
    }

    public long getTimeDifference() {
        return timeDifference;
    }

    public Link getLink() {
        return link;
    }

    public StoreObject getStoreObject() {
        if (storeObject == null) {
            storeObject = new StoreObject();
        }
        return storeObject;
    }

    /**
     * Get the provided protocol logger, or create a new one if 'null'
     *
     * @return A logger that we should use in the protocol
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Rtu getRtu() {
        return rtu;
    }

    /**
     * This is the protocol version, automatically updated after every commit by svn
     *
     * @return The version
     */
    public String getVersion() {
        return "$Date: 2011-12-14 14:17:02 +0100 (wo, 14 dec 2011) $";
    }

    public void addProperties(Properties properties) {
        getProperties().addProperties(properties);
    }

    public List getRequiredKeys() {
        List requiredKeys = getDlmsProtocol().getRequiredKeys();
        requiredKeys.addAll(getProperties().getRequiredKeys());
        return requiredKeys;
    }

    public List getOptionalKeys() {
        List optionalKeys = getDlmsProtocol().getOptionalKeys();
        optionalKeys.addAll(getProperties().getOptionalKeys());
        return optionalKeys;
    }

    public com.elster.protocolimpl.dlms.EK280 getDlmsProtocol() {
        if (dlmsProtocol == null) {
            dlmsProtocol = new com.elster.protocolimpl.dlms.EK280();
        }
        return dlmsProtocol;
    }

    public void applyMessages(List messageEntries) throws IOException {
        getDlmsProtocol().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    /**
     * If EIServer asks the list of categories and messages,
     * just pass the question through to the underlying dlms protocol
     *
     * @return The list of MessageCategorySpecs
     */
    public List<MessageCategorySpec> getMessageCategories() {
        return getDlmsProtocol().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getDlmsProtocol().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getDlmsProtocol().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getDlmsProtocol().writeValue(value);
    }

    public String getConnectTimeInSeconds() {
        return "" + ((System.currentTimeMillis() - startTime) / 1000);
    }

    public EK280Properties getProperties() {
        if (properties == null) {
            properties = new EK280Properties();
            properties.addProperties(new PropertiesFetcher(getLogger()).getPropertiesForRtu(getRtu()));
        }
        return properties;
    }

    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        return getDlmsProtocol().getFirmwareUpdateMessagingConfig();
    }

    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return getDlmsProtocol().getFirmwareUpdateMessageBuilder();
    }
}
