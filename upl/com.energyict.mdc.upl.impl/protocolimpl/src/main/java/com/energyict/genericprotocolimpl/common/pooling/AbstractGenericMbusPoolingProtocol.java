package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.nta.messagehandling.MbusMessages;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.*;
import com.energyict.protocol.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic Abstract MBUSProtocol framework which make proper use of database pooling
 */
public abstract class AbstractGenericMbusPoolingProtocol extends MbusMessages implements GenericProtocol {

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
     * Add the properties to the current protocol
     *
     * @param properties properties to add
     */
    public abstract void addProperties(Properties properties);

    /**
     * Fetch and construct the default MbusProfile
     *
     * @return the mbusProfile
     */
    protected abstract ProfileData getMbusProfile() throws IOException;

    /**
     * Fetch and construct the eventProfile
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
     * @return a <CODE>MeterReadingData</CODE> which contains all the read registers
     */
    protected abstract MeterReadingData doReadRegisters(final List<RtuRegisterFullProtocolShadow> rtuRegisterFullProtocolShadowList) throws IOException;

    /**
     * Send the given RtuMessages
     *
     * @param rtuMessageList the messages to send to the device
     */
    protected abstract void sendMeterMessages(final List<RtuMessage> rtuMessageList) throws BusinessException, SQLException;

    /**
     * Set a fullShadow object which was created by the master in the Mbus object
     *
     * @param mbusFullShadow the fullShadow to use for this mbus device
     */
    protected abstract void setMbusFullShadow(CommunicationSchedulerFullProtocolShadow mbusFullShadow);

    /**
     * Getter for the fullShadow object
     * @return the fullShadow object which represents the Mbus <CODE>Device</CODE> in the database
     */
    protected abstract CommunicationSchedulerFullProtocolShadow getFullShadow();

    /**
     * Getter for the actual Device
     * @return
     */
    protected abstract Device getMbusRtu();

    /**
     * The used <CODE>Logger</CODE>
     */
    private Logger logger;

    /**
     * Object which contains all the necessary data to store
     */
    private StoreObject storeObject;

    /**
     * <pre>
     * This method handles the complete taskExecution.
     * Before each dataCollection task, we will close our current databaseConnection so other threads can reuse this connection.
     */
    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {

        boolean success = false;
        ProfileData mbusProfile = null;
        ProfileData eventProfile = null;
        ProfileData dailyProfile = null;
        ProfileData monthlyProfile = null;
        MeterReadingData rtuRegisters = null;

        this.logger = logger;
        this.storeObject = new StoreObject();

        addProperties(getFullShadow().getRtuShadow().getRtuProperties());
        validateProperties();
        try {

            releaseConnectionFromPool();
            executeWakeUpSequence();

            init();

            /**
             * After 03/06/09 the events are read apart from the intervalData
             */
            if (getFullShadow().getCommunicationProfileShadow().getReadDemandValues()) {
                releaseConnectionFromPool();
                mbusProfile = getMbusProfile();
            }

            if (getFullShadow().getCommunicationProfileShadow().getReadMeterEvents()) {
                releaseConnectionFromPool();
                eventProfile = getEventProfile();
            }

            /**
             * Here we are assuming that the daily and monthly values should be read. In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to
             * indicate whether you want to read the actual registers or the daily/monthly registers ...
             */
            if (getFullShadow().getCommunicationProfileShadow().getReadMeterReadings()) {

                releaseConnectionFromPool();
                dailyProfile = readDailyProfiles();
                monthlyProfile = readMonthlyProfiles();

                getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + getFullShadow().getRtuShadow().getSerialNumber());
                rtuRegisters = doReadRegisters(getFullShadow().getRtuRegisterFullProtocolShadowList());
            }

            if (getFullShadow().getCommunicationProfileShadow().getSendRtuMessage()) {
                releaseConnectionFromPool();
                sendMeterMessages(getFullShadow().getRtuMessageList());
            }

            if (mbusProfile != null) {
                getStoreObject().add(mbusProfile, getMbusRtu());
            }
            if (dailyProfile != null) {
                getStoreObject().add(dailyProfile, getMbusRtu());
            }
            if (monthlyProfile != null) {
                getStoreObject().add(monthlyProfile, getMbusRtu());
            }
            if (eventProfile != null) {
                getStoreObject().add(eventProfile, getMbusRtu());
            }
            if (rtuRegisters != null) {
                getStoreObject().add(rtuRegisters, getMbusRtu());
            }

            success = true;
        } catch (DLMSConnectionException e) {
            log(Level.FINEST, e.getMessage());

        } catch (ClassCastException e) {
            // Mostly programmers fault if you get here ...
            log(Level.FINEST, e.getMessage());

        } catch (SQLException e) {
            log(Level.FINEST, e.getMessage());

            /** Close the connection after an SQL exception, connection will startup again if requested */
            Environment.getDefault().closeConnection();

            throw new BusinessException(e);
        } finally {
            if (success) {
                getLogger().info("Meter " + getFullShadow().getRtuShadow().getSerialNumber() + " has completely finished.");
            }
            if (getStoreObject() != null) {
                Environment.getDefault().execute(getStoreObject());
            }
        }
    }

    /**
     * Closes the current databaseConnection (if we have one)
     */
    protected void releaseConnectionFromPool() {
        Environment.getDefault().closeConnection();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    protected StoreObject getStoreObject() {
        return this.storeObject;
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }
}
