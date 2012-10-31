package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DatabaseException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.nta.messagehandling.MeterMessages;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Abstract Protocol framework which make proper use of database pooling
 */
public abstract class AbstractGenericPoolingProtocol extends MeterMessages implements GenericProtocol {

    /**
     * Set the link to used to contact the meter
     *
     * @param link the <CODE>Link</CODE> the protocol should use to contact the meter
     */
    protected abstract void setLink(Link link);

    /**
     * Validate all required and optional properties
     */
    protected abstract void validateProperties() throws MissingPropertyException, InvalidPropertyException;

    /**
     * Check if a wakeUp needs to be called to the meter, and execute what is necessary
     */
    protected abstract void executeWakeUpSequence() throws BusinessException, IOException, SQLException;

    /**
     * Initialize some global variables.<br>
     * We assume that no time-consuming actions will take place. If this would not apply to your implementation, then please call the {@link #releaseConnectionFromPool()}
     * so proper connectionPooling can take place.
     */
    protected abstract void init() throws IOException, DLMSConnectionException;

    /**
     * Do the actual connection to the device
     */
    protected abstract void connect() throws IOException;

    /**
     * Do the actual disconnect
     */
    protected abstract void disConnect() throws IOException;

    /**
     * Create a shadow object that contains all the necessary information for this communicationSession to complete successfully.
     *
     * @return the newly created fullShadow
     */
    protected abstract CommunicationSchedulerFullProtocolShadow createFullShadow(CommunicationScheduler scheduler);

    /**
     * Add the properties to the current protocol
     *
     * @param properties properties to add
     */
    public abstract void addProperties(Properties properties);

    /**
     * Get the current IP-address of the meter
     */
    protected abstract String getTheMeterHisIpAddress() throws SQLException, BusinessException, IOException;

    /**
     * Get the local cache out of the database and give it to the protocol
     *
     * @param rtuId the id of the <CODE>Rtu</CODE>
     */
    protected abstract void fetchAndSetLocalCache(int rtuId) throws BusinessException, SQLException;

    /**
     * Verify whether the time is incorrect in the device
     *
     * @return true if the time is incorrect, false otherwise
     */
    protected abstract boolean verifyMaxTimeDifference() throws IOException;

    /**
     * Fetch and construct the ElectricityProfile
     *
     * @return the electricityProfile
     */
    protected abstract ProfileData getElectricityProfile() throws IOException;

    /**
     * Fetch an construct the eventProfile
     *
     * @return the eventProfile
     */
    protected abstract ProfileData getEventProfile() throws IOException;

    /**
     * Fetch and construct the dailyProfile
     */
    protected abstract ProfileData readDailyProfiles() throws IOException;

    /**
     * Fetch and construct the monthlyProfile
     */
    protected abstract ProfileData readMonthlyProfiles() throws IOException;

    /**
     * Fetch all the RtuRegisters
     *
     * @param rtuRegisterFullProtocolShadowList
     *
     * @return a <CODE>MeterReadingData</CODE> object which contains all the registers which are read
     */
    protected abstract MeterReadingData doReadRegisters(final List<RtuRegisterFullProtocolShadow> rtuRegisterFullProtocolShadowList) throws IOException;

    /**
     * Send the given RtuMessages
     *
     * @param rtuMessageList the RtuMessages to send to the device
     */
    protected abstract void sendMeterMessages(final List<RtuMessage> rtuMessageList) throws BusinessException, SQLException;

    /**
     * Discover all the MbusDevices. Currently we can go to the database.
     */
    protected abstract void discoverMbusDevices() throws SQLException, BusinessException;

    /**
     * Get a number of valid MbusDevices
     *
     * @return the number of valid MbusDevices
     */
    protected abstract int getValidMbusDevices();

    /**
     * Handle all valid MbusDevices
     */
    protected abstract void handleMbusMeters();

    /**
     * Verify the timeDifference and write the clock if necessary
     */
    protected abstract void verifyAndWriteClock() throws IOException;

    /**
     * Force the time to be set
     */
    protected abstract void forceClock() throws IOException;

    /**
     * @param rtuId     the ID of the <CODE>Rtu</CODE> to update the cache object from
     * @param dlmsCache the object to write to the cache
     */
    protected abstract void updateCache(final int rtuId, final Object dlmsCache) throws SQLException, BusinessException;

    /**
     * @return the object to cache for this device
     */
    protected abstract DLMSCache getDlmsCache();

    /**
     * The used <CODE>CommunicationScheduler</CODE>
     */
    private CommunicationScheduler scheduler;

    /**
     * The used <CODE>Logger</CODE>
     */
    private Logger logger;

    /**
     * This should contain all the necessary information about the meter/slavemeters/profiles/registers/message/ ... to use in the communication session
     */
    private CommunicationSchedulerFullProtocolShadow fullShadow;

    /**
     * Object which contains all the necessary data to store
     */
    private StoreObject storeObject;

    /**
     * Indication whether the time is incorrect of the device
     */
    private boolean badTime = false;

    /**
     * <pre>
     * This method handles the complete taskExecution.
     * Before each dataCollection task, we will close our current databaseConnection so other threads can reuse this connection.
     */
    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {

        boolean success = false;
        boolean databaseException = false;
        ProfileData electricityProfile = null;
        ProfileData eventProfile = null;
        ProfileData dailyProfile = null;
        ProfileData monthlyProfile = null;
        MeterReadingData rtuRegisters = null;

        this.scheduler = scheduler;
        this.logger = logger;
        this.storeObject = new StoreObject();

        this.fullShadow = createFullShadow(this.scheduler);
        addProperties(this.fullShadow.getRtuShadow().getRtuProperties());
        validateProperties();
        try {

            releaseConnectionFromPool();
            setLink(link);
            executeWakeUpSequence();

            init();
            fetchAndSetLocalCache(this.fullShadow.getRtuShadow().getRtuId());
            connect();
            if (this.scheduler.getModemPool().getInbound()) {
                this.scheduler.getRtu().updateIpAddress(getTheMeterHisIpAddress());
            }

            // Check if the time is greater then allowed, if so then no data can be stored...
            // Don't do this when a forceClock is scheduled
            if (!this.fullShadow.getCommunicationProfileShadow().getForceClock() && !this.fullShadow.getCommunicationProfileShadow().getAdHoc()) {
                releaseConnectionFromPool();
                badTime = verifyMaxTimeDifference();
            }

            /**
             * After 03/06/09 the events are read apart from the intervalData
             */
            if (this.fullShadow.getCommunicationProfileShadow().getReadDemandValues()) {
                releaseConnectionFromPool();
                electricityProfile = getElectricityProfile();
            }

            if (this.fullShadow.getCommunicationProfileShadow().getReadMeterEvents()) {
                getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + this.fullShadow.getRtuShadow().getSerialNumber());
                releaseConnectionFromPool();
                eventProfile = getEventProfile();
            }

            /**
             * Here we are assuming that the daily and monthly values should be read. In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to
             * indicate whether you want to read the actual registers or the daily/monthly registers ...
             */
            if (this.fullShadow.getCommunicationProfileShadow().getReadMeterReadings()) {
                releaseConnectionFromPool();
                dailyProfile = readDailyProfiles();
                monthlyProfile = readMonthlyProfiles();

                if (this.fullShadow.getRtuRegisterFullProtocolShadowList().size() != 0) {
                    getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + this.fullShadow.getRtuShadow().getSerialNumber());
                    rtuRegisters = doReadRegisters(this.fullShadow.getRtuRegisterFullProtocolShadowList());
                }
            }

            if (this.fullShadow.getCommunicationProfileShadow().getSendRtuMessage()) {
                releaseConnectionFromPool();
                sendMeterMessages(this.fullShadow.getRtuMessageList());
            }

            discoverMbusDevices();
            if (getValidMbusDevices() != 0) {
                getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
                handleMbusMeters();
            }

            // Set clock or Force clock... if necessary
            if (this.fullShadow.getCommunicationProfileShadow().getForceClock()) {
                releaseConnectionFromPool();
                forceClock();
            } else {
                releaseConnectionFromPool();
                verifyAndWriteClock();
            }

            if (electricityProfile != null) {
                if (badTime) {
                    electricityProfile.markIntervalsAsBadTime();
                }
                getStoreObject().add(electricityProfile, this.scheduler.getRtu());
            }
            if (dailyProfile != null) {
                if (badTime) {
                    dailyProfile.markIntervalsAsBadTime();
                }
                getStoreObject().add(dailyProfile, this.scheduler.getRtu());
            }
            if (monthlyProfile != null) {
                if (badTime) {
                    monthlyProfile.markIntervalsAsBadTime();
                }
                getStoreObject().add(monthlyProfile, this.scheduler.getRtu());
            }
            if (eventProfile != null) {
                getStoreObject().add(eventProfile, this.scheduler.getRtu());
            }
            if (rtuRegisters != null) {
                getStoreObject().add(rtuRegisters, this.scheduler.getRtu());
            }

            success = true;

        } catch (DLMSConnectionException e) {
            log(Level.FINEST, e.getMessage());
            disConnect();
        } catch (ClassCastException e) {
            // Mostly programmers fault if you get here ...
            log(Level.FINEST, e.getMessage());
            disConnect();
        } catch (SQLException e) {
            databaseException = true;
            log(Level.FINEST, e.getMessage());
            disConnect();
            throw e; // we rethrow the exception so the ComServer can catch it and properly handle the sqlException
        } catch (DatabaseException e){
            databaseException = true;
            log(Level.FINEST, e.getMessage());
            disConnect();
            throw e; // we rethrow the exception so the ComServer can catch it and properly handle the sqlException
        } finally {
            if (success) {
                disConnect();
                getLogger().info("Meter " + this.fullShadow.getRtuShadow().getSerialNumber() + " has completely finished.");
            }

            if (!databaseException) {
                updateCache(this.scheduler.getRtu().getId(), getDlmsCache());
                if (getStoreObject() != null) {
                    Environment.getDefault().execute(getStoreObject());
                }
            }
        }
    }

    /**
     * Closes the current databaseConnection (if we have one)
     */
    protected void releaseConnectionFromPool() {
        Environment.getDefault().closeConnection();
    }

    protected CommunicationScheduler getCommunicationScheduler() {
        return this.scheduler;
    }

    public CommunicationSchedulerFullProtocolShadow getFullShadow() {
        return this.fullShadow;
    }

    public void setFullShadow(CommunicationSchedulerFullProtocolShadow fullShadow) {
        this.fullShadow = fullShadow;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    protected StoreObject getStoreObject() {
        return this.storeObject;
    }

    protected void setStoreObject(StoreObject storeObject) {
        this.storeObject = storeObject;
    }

    public boolean isBadTime() {
        return this.badTime;
    }
}
